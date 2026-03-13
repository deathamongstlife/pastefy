package cc.allyapps.pastely.websocket;

import cc.allyapps.pastely.websocket.model.*;
import com.google.gson.Gson;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Manages a single collaborative editing room for a paste.
 * Handles user connections, operations, and state synchronization.
 */
public class CollaborationRoom {
    private static final int MAX_USERS = 100;
    private static final int MAX_OPERATION_HISTORY = 1000;
    private static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

    private final String sessionId;
    private final String pasteId;
    private final Map<String, UserConnection> users;
    private final List<TextOperation> operationHistory;
    private int currentVersion;
    private String currentContent;
    private final Instant createdAt;
    private Instant lastActivityAt;
    private final Gson gson;

    public CollaborationRoom(String sessionId, String pasteId, String initialContent) {
        this.sessionId = sessionId;
        this.pasteId = pasteId;
        this.users = new ConcurrentHashMap<>();
        this.operationHistory = new CopyOnWriteArrayList<>();
        this.currentVersion = 0;
        this.currentContent = initialContent != null ? initialContent : "";
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
        this.gson = new Gson();
    }

    public boolean addUser(UserConnection userConnection) {
        if (users.size() >= MAX_USERS) {
            return false;
        }

        users.put(userConnection.getConnectionId(), userConnection);
        lastActivityAt = Instant.now();

        System.out.println("User " + userConnection.getUser().getUsername() +
                         " joined room " + sessionId + " (total: " + users.size() + ")");

        return true;
    }

    public void removeUser(String connectionId) {
        UserConnection removed = users.remove(connectionId);
        if (removed != null) {
            lastActivityAt = Instant.now();
            System.out.println("User " + removed.getUser().getUsername() +
                             " left room " + sessionId + " (remaining: " + users.size() + ")");
        }
    }

    public UserConnection getUser(String connectionId) {
        return users.get(connectionId);
    }

    public void broadcastCursor(String connectionId, CursorPosition position) {
        UserConnection user = users.get(connectionId);
        if (user == null) {
            return;
        }

        user.setLastCursorPosition(position);
        WebSocketMessage message = WebSocketMessage.cursorUpdate(user.getUserId(), position);
        broadcast(message, connectionId);
    }

    public synchronized boolean applyOperation(String connectionId, TextOperation operation) {
        UserConnection user = users.get(connectionId);
        if (user == null) {
            return false;
        }

        try {
            // Transform operation against any concurrent operations
            List<TextOperation> concurrentOps = operationHistory.stream()
                .filter(op -> op.getVersion() >= operation.getVersion())
                .collect(Collectors.toList());

            TextOperation transformedOp = OperationalTransform.transformAgainstMultiple(operation, concurrentOps);
            transformedOp.setVersion(currentVersion);
            transformedOp.setUserId(user.getUserId());

            // Apply operation to document
            String newContent = OperationalTransform.applyOperation(currentContent, transformedOp);
            currentContent = newContent;
            currentVersion++;

            // Add to history
            operationHistory.add(transformedOp);
            if (operationHistory.size() > MAX_OPERATION_HISTORY) {
                operationHistory.remove(0);
            }

            // Broadcast to other users
            WebSocketMessage message = WebSocketMessage.editBroadcast(user.getUserId(), transformedOp);
            broadcast(message, connectionId);

            user.updateActivity();
            lastActivityAt = Instant.now();

            return true;
        } catch (Exception e) {
            System.err.println("Error applying operation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void sendToUser(String connectionId, WebSocketMessage message) {
        UserConnection user = users.get(connectionId);
        if (user != null && user.getChannel().isOpen()) {
            try {
                String json = gson.toJson(message);
                WebSockets.sendText(json, user.getChannel(), null);
            } catch (Exception e) {
                System.err.println("Error sending message to user: " + e.getMessage());
            }
        }
    }

    public void broadcast(WebSocketMessage message, String excludeConnectionId) {
        String json = gson.toJson(message);
        users.values().forEach(user -> {
            if (!user.getConnectionId().equals(excludeConnectionId) && user.getChannel().isOpen()) {
                try {
                    WebSockets.sendText(json, user.getChannel(), null);
                } catch (Exception e) {
                    System.err.println("Error broadcasting to user: " + e.getMessage());
                }
            }
        });
    }

    public void broadcastToAll(WebSocketMessage message) {
        String json = gson.toJson(message);
        users.values().forEach(user -> {
            if (user.getChannel().isOpen()) {
                try {
                    WebSockets.sendText(json, user.getChannel(), null);
                } catch (Exception e) {
                    System.err.println("Error broadcasting to user: " + e.getMessage());
                }
            }
        });
    }

    public List<UserInfo> getActiveUsers() {
        return users.values().stream()
            .map(conn -> {
                UserInfo info = new UserInfo(conn.getUser(), conn.getConnectionId());
                info.setCursor(conn.getLastCursorPosition());
                return info;
            })
            .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    public boolean isIdle() {
        long idleTime = Instant.now().toEpochMilli() - lastActivityAt.toEpochMilli();
        return idleTime > IDLE_TIMEOUT_MS;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPasteId() {
        return pasteId;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public String getCurrentContent() {
        return currentContent;
    }

    public int getUserCount() {
        return users.size();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void cleanup() {
        List<String> disconnected = new ArrayList<>();
        users.forEach((connectionId, user) -> {
            if (!user.getChannel().isOpen()) {
                disconnected.add(connectionId);
            }
        });

        disconnected.forEach(this::removeUser);
    }
}
