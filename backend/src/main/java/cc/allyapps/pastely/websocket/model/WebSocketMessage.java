package cc.allyapps.pastely.websocket.model;

import java.util.List;

public class WebSocketMessage {
    private String type;
    private String sessionId;
    private String userId;
    private String pasteId;
    private CursorPosition position;
    private TextOperation operation;
    private UserInfo user;
    private List<UserInfo> users;
    private String error;
    private String message;

    public WebSocketMessage() {}

    public WebSocketMessage(String type) {
        this.type = type;
    }

    public static WebSocketMessage pong() {
        return new WebSocketMessage("pong");
    }

    public static WebSocketMessage userJoined(UserInfo user, List<UserInfo> allUsers) {
        WebSocketMessage message = new WebSocketMessage("user_joined");
        message.user = user;
        message.users = allUsers;
        return message;
    }

    public static WebSocketMessage userLeft(String userId) {
        WebSocketMessage message = new WebSocketMessage("user_left");
        message.userId = userId;
        return message;
    }

    public static WebSocketMessage cursorUpdate(String userId, CursorPosition position) {
        WebSocketMessage message = new WebSocketMessage("cursor_update");
        message.userId = userId;
        message.position = position;
        return message;
    }

    public static WebSocketMessage editBroadcast(String userId, TextOperation operation) {
        WebSocketMessage message = new WebSocketMessage("edit_broadcast");
        message.userId = userId;
        message.operation = operation;
        return message;
    }

    public static WebSocketMessage error(String errorMessage) {
        WebSocketMessage message = new WebSocketMessage("error");
        message.error = errorMessage;
        return message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public CursorPosition getPosition() {
        return position;
    }

    public void setPosition(CursorPosition position) {
        this.position = position;
    }

    public TextOperation getOperation() {
        return operation;
    }

    public void setOperation(TextOperation operation) {
        this.operation = operation;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
