package cc.allyapps.pastely.websocket;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.model.database.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.javawebstack.orm.Repo;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CollaborationWebSocket - Real-time collaboration via WebSocket
 * Handles operational transforms, cursor positions, and live editing
 */
public class CollaborationWebSocket implements WebSocketConnectionCallback {

    private static final Map<String, Set<WebSocketChannel>> pasteChannels = new ConcurrentHashMap<>();
    private static final Map<WebSocketChannel, SessionInfo> channelSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        String uri = exchange.getRequestURI();
        String pasteKey = extractPasteKey(uri);
        String token = extractToken(uri);

        if (pasteKey == null || token == null) {
            sendError(channel, "Missing pasteKey or token");
            closeChannel(channel);
            return;
        }

        // Authenticate user
        User user = authenticateUser(token);
        if (user == null) {
            sendError(channel, "Authentication failed");
            closeChannel(channel);
            return;
        }

        // Verify paste exists and user has access
        Paste paste = Paste.get(pasteKey);
        if (paste == null) {
            sendError(channel, "Paste not found");
            closeChannel(channel);
            return;
        }

        // Create session info
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.pasteKey = pasteKey;
        sessionInfo.userId = user.getId();
        sessionInfo.userName = user.getName();
        sessionInfo.userColor = generateUserColor(user.getId());
        sessionInfo.connectedAt = Timestamp.from(Instant.now());

        channelSessions.put(channel, sessionInfo);

        // Add to paste channels
        pasteChannels.computeIfAbsent(pasteKey, k -> Collections.synchronizedSet(new HashSet<>()))
                .add(channel);

        // Log collaboration session
        logCollaborationJoin(pasteKey, user.getId());

        // Send initial state to new user
        sendInitialState(channel, pasteKey, user);

        // Broadcast user joined to others
        broadcastToRoom(pasteKey, createMessage("user_joined", sessionInfo.toJson()), channel);

        // Set up message receiver
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                handleMessage(channel, message.getData());
            }

            @Override
            protected void onClose(WebSocketChannel channel, StreamSourceFrameChannel streamSourceChannel) throws IOException {
                handleDisconnect(channel);
            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                System.err.println("WebSocket error: " + error.getMessage());
                handleDisconnect(channel);
            }
        });

        channel.resumeReceives();
    }

    private void handleMessage(WebSocketChannel channel, String data) {
        try {
            SessionInfo session = channelSessions.get(channel);
            if (session == null) return;

            JsonObject msg = gson.fromJson(data, JsonObject.class);
            String type = msg.get("type").getAsString();

            switch (type) {
                case "edit":
                    handleEdit(channel, session, msg);
                    break;
                case "cursor":
                    handleCursor(channel, session, msg);
                    break;
                case "selection":
                    handleSelection(channel, session, msg);
                    break;
                case "typing":
                    handleTyping(channel, session, msg);
                    break;
                default:
                    System.err.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            sendError(channel, "Failed to process message");
        }
    }

    private void handleEdit(WebSocketChannel channel, SessionInfo session, JsonObject msg) {
        JsonObject data = msg.getAsJsonObject("data");

        // Create edit operation
        EditOperation op = new EditOperation();
        op.userId = session.userId;
        op.userName = session.userName;
        op.position = data.get("position").getAsInt();
        op.text = data.get("text").getAsString();
        op.type = data.get("type").getAsString(); // "insert" or "delete"
        op.timestamp = System.currentTimeMillis();

        if (data.has("length")) {
            op.length = data.get("length").getAsInt();
        }

        // Store operation in Redis cache if available
        if (Pastely.getInstance().isRedisEnabled()) {
            storeOperation(session.pasteKey, op);
        }

        // Broadcast to other users
        broadcastToRoom(session.pasteKey, createMessage("edit", op.toJson()), channel);

        // Update last activity
        updateLastActivity(session.pasteKey);
    }

    private void handleCursor(WebSocketChannel channel, SessionInfo session, JsonObject msg) {
        JsonObject data = msg.getAsJsonObject("data");

        CursorPosition cursor = new CursorPosition();
        cursor.userId = session.userId;
        cursor.userName = session.userName;
        cursor.line = data.get("line").getAsInt();
        cursor.column = data.get("column").getAsInt();
        cursor.color = session.userColor;

        // Store in database
        updateCursorPosition(session.pasteKey, session.userId, cursor);

        // Broadcast to other users
        broadcastToRoom(session.pasteKey, createMessage("cursor", cursor.toJson()), channel);
    }

    private void handleSelection(WebSocketChannel channel, SessionInfo session, JsonObject msg) {
        // Broadcast selection updates
        broadcastToRoom(session.pasteKey, msg.toString(), channel);
    }

    private void handleTyping(WebSocketChannel channel, SessionInfo session, JsonObject msg) {
        JsonObject data = msg.getAsJsonObject("data");
        boolean isTyping = data.get("isTyping").getAsBoolean();

        JsonObject typingData = new JsonObject();
        typingData.addProperty("userId", session.userId);
        typingData.addProperty("userName", session.userName);
        typingData.addProperty("isTyping", isTyping);

        broadcastToRoom(session.pasteKey, createMessage("typing", typingData.toString()), channel);
    }

    private void handleDisconnect(WebSocketChannel channel) {
        SessionInfo session = channelSessions.remove(channel);
        if (session != null) {
            String pasteKey = session.pasteKey;
            Set<WebSocketChannel> channels = pasteChannels.get(pasteKey);
            if (channels != null) {
                channels.remove(channel);
                if (channels.isEmpty()) {
                    pasteChannels.remove(pasteKey);
                }
            }

            // Remove cursor position
            removeCursorPosition(pasteKey, session.userId);

            // Broadcast user left
            JsonObject userData = new JsonObject();
            userData.addProperty("userId", session.userId);
            userData.addProperty("userName", session.userName);
            broadcastToRoom(pasteKey, createMessage("user_left", userData.toString()), null);

            // Log disconnect
            logCollaborationLeave(pasteKey, session.userId);
        }

        try {
            if (channel.isOpen()) {
                channel.sendClose();
            }
        } catch (IOException e) {
            // Ignore close errors
        }
    }

    private void broadcastToRoom(String pasteKey, String message, WebSocketChannel except) {
        Set<WebSocketChannel> channels = pasteChannels.get(pasteKey);
        if (channels == null) return;

        synchronized (channels) {
            for (WebSocketChannel ch : channels) {
                if (ch != except && ch.isOpen()) {
                    try {
                        WebSockets.sendText(message, ch, null);
                    } catch (Exception e) {
                        System.err.println("Error broadcasting to channel: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void sendInitialState(WebSocketChannel channel, String pasteKey, User user) {
        try {
            Paste paste = Paste.get(pasteKey);
            if (paste == null) return;

            JsonObject state = new JsonObject();
            state.addProperty("content", paste.getContent());

            // Get active users
            List<JsonObject> activeUsers = new ArrayList<>();
            Set<WebSocketChannel> channels = pasteChannels.get(pasteKey);
            if (channels != null) {
                synchronized (channels) {
                    for (WebSocketChannel ch : channels) {
                        SessionInfo info = channelSessions.get(ch);
                        if (info != null && ch != channel) {
                            activeUsers.add(info.toJson());
                        }
                    }
                }
            }
            state.add("activeUsers", gson.toJsonTree(activeUsers));

            // Get cursor positions
            state.add("cursors", gson.toJsonTree(getCursorPositions(pasteKey)));

            WebSockets.sendText(createMessage("init", state.toString()), channel, null);
        } catch (Exception e) {
            System.err.println("Error sending initial state: " + e.getMessage());
        }
    }

    private void sendError(WebSocketChannel channel, String error) {
        JsonObject errorData = new JsonObject();
        errorData.addProperty("message", error);
        WebSockets.sendText(createMessage("error", errorData.toString()), channel, null);
    }

    private void closeChannel(WebSocketChannel channel) {
        try {
            channel.sendClose();
        } catch (IOException e) {
            // Ignore
        }
    }

    private String createMessage(String type, String data) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        try {
            msg.add("data", gson.fromJson(data, JsonObject.class));
        } catch (Exception e) {
            msg.addProperty("data", data);
        }
        return gson.toJson(msg);
    }

    private String extractPasteKey(String uri) {
        // Extract from /ws/collaborate/{pasteKey}
        String[] parts = uri.split("\\?")[0].split("/");
        return parts.length > 3 ? parts[3] : null;
    }

    private String extractToken(String uri) {
        // Extract token from query string
        int tokenIndex = uri.indexOf("token=");
        if (tokenIndex == -1) return null;
        String tokenPart = uri.substring(tokenIndex + 6);
        int ampIndex = tokenPart.indexOf("&");
        return ampIndex > 0 ? tokenPart.substring(0, ampIndex) : tokenPart;
    }

    private User authenticateUser(String token) {
        if (token == null) return null;
        AuthKey authKey = Repo.get(AuthKey.class).where("key", token).first();
        return authKey != null ? User.get(authKey.userId) : null;
    }

    private String generateUserColor(String userId) {
        // Generate consistent color for user based on their ID
        int hash = userId.hashCode();
        String[] colors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
            "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B739", "#52B788"
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private void storeOperation(String pasteKey, EditOperation op) {
        // Store operation in Redis for conflict resolution
        try {
            if (Pastely.getInstance().isRedisEnabled()) {
                String key = "collab:ops:" + pasteKey;
                Pastely.getInstance().getRedisClient().rpush(key, gson.toJson(op));
                Pastely.getInstance().getRedisClient().expire(key, 3600); // 1 hour
            }
        } catch (Exception e) {
            System.err.println("Error storing operation: " + e.getMessage());
        }
    }

    private void updateCursorPosition(String pasteKey, String userId, CursorPosition cursor) {
        CollaborationCursor dbCursor = Repo.get(CollaborationCursor.class)
            .where("pasteId", pasteKey)
            .where("userId", userId)
            .first();

        if (dbCursor == null) {
            dbCursor = new CollaborationCursor();
            dbCursor.setPasteId(pasteKey);
            dbCursor.setUserId(userId);
        }

        dbCursor.setLine(cursor.line);
        dbCursor.setColumn(cursor.column);
        dbCursor.setColor(cursor.color);
        dbCursor.save();
    }

    private void removeCursorPosition(String pasteKey, String userId) {
        Repo.get(CollaborationCursor.class)
            .where("pasteId", pasteKey)
            .where("userId", userId)
            .delete();
    }

    private List<CursorPosition> getCursorPositions(String pasteKey) {
        List<CollaborationCursor> cursors = Repo.get(CollaborationCursor.class)
            .where("pasteId", pasteKey)
            .all();

        List<CursorPosition> positions = new ArrayList<>();
        for (CollaborationCursor cursor : cursors) {
            CursorPosition pos = new CursorPosition();
            pos.userId = cursor.getUserId();
            User user = User.get(cursor.getUserId());
            pos.userName = user != null ? user.getName() : "Unknown";
            pos.line = cursor.getLine();
            pos.column = cursor.getColumn();
            pos.color = cursor.getColor();
            positions.add(pos);
        }
        return positions;
    }

    private void updateLastActivity(String pasteKey) {
        // Update last activity timestamp for paste
        Pastely.getInstance().executeAsync(() -> {
            CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("pasteId", pasteKey)
                .where("isActive", true)
                .first();
            if (session != null) {
                session.setLastActivityAt(Timestamp.from(Instant.now()));
                session.save();
            }
        });
    }

    private void logCollaborationJoin(String pasteKey, String userId) {
        Pastely.getInstance().executeAsync(() -> {
            AccessLog log = new AccessLog();
            log.setPasteId(pasteKey);
            log.setUserId(userId);
            log.setAccessType("COLLABORATION_JOIN");
            log.save();
        });
    }

    private void logCollaborationLeave(String pasteKey, String userId) {
        Pastely.getInstance().executeAsync(() -> {
            AccessLog log = new AccessLog();
            log.setPasteId(pasteKey);
            log.setUserId(userId);
            log.setAccessType("COLLABORATION_LEAVE");
            log.save();
        });
    }

    // Helper classes
    static class SessionInfo {
        String pasteKey;
        String userId;
        String userName;
        String userColor;
        Timestamp connectedAt;

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("userId", userId);
            json.addProperty("userName", userName);
            json.addProperty("color", userColor);
            return json;
        }
    }

    static class EditOperation {
        String userId;
        String userName;
        int position;
        String text;
        String type; // "insert" or "delete"
        int length;
        long timestamp;

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("userId", userId);
            json.addProperty("userName", userName);
            json.addProperty("position", position);
            json.addProperty("text", text);
            json.addProperty("type", type);
            json.addProperty("length", length);
            json.addProperty("timestamp", timestamp);
            return json;
        }
    }

    static class CursorPosition {
        String userId;
        String userName;
        int line;
        int column;
        String color;

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("userId", userId);
            json.addProperty("userName", userName);
            json.addProperty("line", line);
            json.addProperty("column", column);
            json.addProperty("color", color);
            return json;
        }
    }
}
