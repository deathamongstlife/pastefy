package cc.allyapps.pastely.websocket;

import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.websocket.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.javawebstack.orm.Repo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocket handler for real-time collaborative editing.
 * Manages connections, rooms, and message routing.
 */
public class CollaborationWebSocketHandler implements WebSocketConnectionCallback {

    private static final Map<String, CollaborationRoom> rooms = new ConcurrentHashMap<>();
    private static final Map<String, String> channelToRoom = new ConcurrentHashMap<>();
    private static final Map<String, String> channelToConnectionId = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    private static final long HEARTBEAT_INTERVAL_MS = 30000; // 30 seconds
    private static final long CLEANUP_INTERVAL_MS = 60000; // 1 minute

    private final Gson gson;

    static {
        // Schedule periodic cleanup of idle rooms
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupIdleRooms();
            } catch (Exception e) {
                System.err.println("Error during room cleanup: " + e.getMessage());
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);

        System.out.println("CollaborationWebSocketHandler initialized with periodic cleanup");
    }

    public CollaborationWebSocketHandler() {
        this.gson = new Gson();
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        String channelId = getChannelId(channel);
        System.out.println("WebSocket connection established: " + channelId);

        // Set up message receiver
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                handleMessage(channel, message.getData());
            }

            @Override
            protected void onClose(WebSocketChannel channel, StreamSourceFrameChannel streamSourceChannel) throws IOException {
                handleClose(channel);
            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                handleError(channel, error);
            }

            @Override
            protected void onPing(WebSocketChannel channel, BufferedBinaryMessage message) {
                try {
                    WebSockets.sendPong(message.getData().getResource(), channel, null);
                } catch (Exception e) {
                    System.err.println("Error sending pong: " + e.getMessage());
                }
            }
        });

        channel.resumeReceives();
    }

    private void handleMessage(WebSocketChannel channel, String messageText) {
        String channelId = getChannelId(channel);

        try {
            WebSocketMessage message = gson.fromJson(messageText, WebSocketMessage.class);
            if (message == null || message.getType() == null) {
                sendError(channel, "Invalid message format");
                return;
            }

            switch (message.getType()) {
                case "join":
                    handleJoin(channel, message);
                    break;
                case "cursor":
                    handleCursor(channel, message);
                    break;
                case "edit":
                    handleEdit(channel, message);
                    break;
                case "ping":
                    handlePing(channel);
                    break;
                default:
                    sendError(channel, "Unknown message type: " + message.getType());
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON from " + channelId + ": " + e.getMessage());
            sendError(channel, "Invalid JSON");
        } catch (Exception e) {
            System.err.println("Error handling message from " + channelId + ": " + e.getMessage());
            e.printStackTrace();
            sendError(channel, "Internal server error");
        }
    }

    private void handleJoin(WebSocketChannel channel, WebSocketMessage message) {
        String sessionId = message.getSessionId();
        String userId = message.getUserId();
        String pasteId = message.getPasteId();

        if (sessionId == null || userId == null || pasteId == null) {
            sendError(channel, "Missing required fields: sessionId, userId, pasteId");
            return;
        }

        // Verify user exists
        User user = Repo.get(User.class).get(userId);
        if (user == null) {
            sendError(channel, "User not found");
            return;
        }

        // Verify paste exists and user has access
        Paste paste = Repo.get(Paste.class).where("key", pasteId).first();
        if (paste == null) {
            sendError(channel, "Paste not found");
            return;
        }

        // Get or create room
        CollaborationRoom room = rooms.computeIfAbsent(sessionId, key -> {
            String content = paste.getContent();
            CollaborationRoom newRoom = new CollaborationRoom(sessionId, pasteId, content);
            System.out.println("Created new collaboration room: " + sessionId);
            return newRoom;
        });

        // Create connection ID
        String connectionId = UUID.randomUUID().toString();
        UserConnection userConnection = new UserConnection(userId, user, channel, connectionId);

        // Add user to room
        if (!room.addUser(userConnection)) {
            sendError(channel, "Room is full");
            return;
        }

        // Track channel mappings
        String channelId = getChannelId(channel);
        channelToRoom.put(channelId, sessionId);
        channelToConnectionId.put(channelId, connectionId);

        // Send user joined message to all users
        UserInfo userInfo = new UserInfo(user, connectionId);
        List<UserInfo> allUsers = room.getActiveUsers();
        WebSocketMessage joinedMessage = WebSocketMessage.userJoined(userInfo, allUsers);
        room.broadcastToAll(joinedMessage);

        System.out.println("User " + user.getUsername() + " joined session " + sessionId);
    }

    private void handleCursor(WebSocketChannel channel, WebSocketMessage message) {
        String sessionId = getSessionId(channel);
        String connectionId = getConnectionId(channel);

        if (sessionId == null || connectionId == null) {
            sendError(channel, "Not joined to a session");
            return;
        }

        CollaborationRoom room = rooms.get(sessionId);
        if (room == null) {
            sendError(channel, "Session not found");
            return;
        }

        CursorPosition position = message.getPosition();
        if (position != null) {
            room.broadcastCursor(connectionId, position);
        }
    }

    private void handleEdit(WebSocketChannel channel, WebSocketMessage message) {
        String sessionId = getSessionId(channel);
        String connectionId = getConnectionId(channel);

        if (sessionId == null || connectionId == null) {
            sendError(channel, "Not joined to a session");
            return;
        }

        CollaborationRoom room = rooms.get(sessionId);
        if (room == null) {
            sendError(channel, "Session not found");
            return;
        }

        TextOperation operation = message.getOperation();
        if (operation == null) {
            sendError(channel, "Missing operation");
            return;
        }

        boolean success = room.applyOperation(connectionId, operation);
        if (!success) {
            sendError(channel, "Failed to apply operation");
        }
    }

    private void handlePing(WebSocketChannel channel) {
        try {
            WebSocketMessage pong = WebSocketMessage.pong();
            String json = gson.toJson(pong);
            WebSockets.sendText(json, channel, null);
        } catch (Exception e) {
            System.err.println("Error sending pong: " + e.getMessage());
        }
    }

    private void handleClose(WebSocketChannel channel) {
        String channelId = getChannelId(channel);
        String sessionId = channelToRoom.remove(channelId);
        String connectionId = channelToConnectionId.remove(channelId);

        if (sessionId != null && connectionId != null) {
            CollaborationRoom room = rooms.get(sessionId);
            if (room != null) {
                UserConnection user = room.getUser(connectionId);
                room.removeUser(connectionId);

                // Broadcast user left
                if (user != null) {
                    WebSocketMessage leftMessage = WebSocketMessage.userLeft(user.getUserId());
                    room.broadcast(leftMessage, null);
                    System.out.println("User " + user.getUser().getUsername() + " disconnected from " + sessionId);
                }

                // Clean up empty room
                if (room.isEmpty()) {
                    rooms.remove(sessionId);
                    System.out.println("Removed empty room: " + sessionId);
                }
            }
        }

        System.out.println("WebSocket connection closed: " + channelId);
    }

    private void handleError(WebSocketChannel channel, Throwable error) {
        String channelId = getChannelId(channel);
        System.err.println("WebSocket error on " + channelId + ": " + error.getMessage());
        error.printStackTrace();

        try {
            handleClose(channel);
        } catch (Exception e) {
            System.err.println("Error during error cleanup: " + e.getMessage());
        }
    }

    private void sendError(WebSocketChannel channel, String error) {
        try {
            WebSocketMessage errorMessage = WebSocketMessage.error(error);
            String json = gson.toJson(errorMessage);
            WebSockets.sendText(json, channel, null);
        } catch (Exception e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    private String getChannelId(WebSocketChannel channel) {
        return channel.toString();
    }

    private String getSessionId(WebSocketChannel channel) {
        return channelToRoom.get(getChannelId(channel));
    }

    private String getConnectionId(WebSocketChannel channel) {
        return channelToConnectionId.get(getChannelId(channel));
    }

    private static void cleanupIdleRooms() {
        List<String> toRemove = new ArrayList<>();

        rooms.forEach((sessionId, room) -> {
            // Cleanup disconnected users
            room.cleanup();

            // Remove idle empty rooms
            if (room.isEmpty() && room.isIdle()) {
                toRemove.add(sessionId);
            }
        });

        toRemove.forEach(sessionId -> {
            rooms.remove(sessionId);
            System.out.println("Cleaned up idle room: " + sessionId);
        });

        if (!toRemove.isEmpty()) {
            System.out.println("Cleaned up " + toRemove.size() + " idle rooms. Active rooms: " + rooms.size());
        }
    }

    public static Map<String, CollaborationRoom> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    public static int getActiveRoomCount() {
        return rooms.size();
    }

    public static int getTotalUserCount() {
        return rooms.values().stream()
            .mapToInt(CollaborationRoom::getUserCount)
            .sum();
    }
}
