package cc.allyapps.pastely.websocket.model;

import cc.allyapps.pastely.model.database.User;
import io.undertow.websockets.core.WebSocketChannel;

import java.time.Instant;

public class UserConnection {
    private String userId;
    private User user;
    private WebSocketChannel channel;
    private CursorPosition lastCursorPosition;
    private Instant lastActivityAt;
    private String connectionId;

    public UserConnection(String userId, User user, WebSocketChannel channel, String connectionId) {
        this.userId = userId;
        this.user = user;
        this.channel = channel;
        this.connectionId = connectionId;
        this.lastActivityAt = Instant.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WebSocketChannel getChannel() {
        return channel;
    }

    public void setChannel(WebSocketChannel channel) {
        this.channel = channel;
    }

    public CursorPosition getLastCursorPosition() {
        return lastCursorPosition;
    }

    public void setLastCursorPosition(CursorPosition lastCursorPosition) {
        this.lastCursorPosition = lastCursorPosition;
        this.lastActivityAt = Instant.now();
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void updateActivity() {
        this.lastActivityAt = Instant.now();
    }
}
