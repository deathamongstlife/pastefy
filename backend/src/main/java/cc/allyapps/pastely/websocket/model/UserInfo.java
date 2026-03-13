package cc.allyapps.pastely.websocket.model;

import cc.allyapps.pastely.model.database.User;

public class UserInfo {
    private String id;
    private String username;
    private String name;
    private String connectionId;
    private CursorPosition cursor;

    public UserInfo() {}

    public UserInfo(User user, String connectionId) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.connectionId = connectionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public CursorPosition getCursor() {
        return cursor;
    }

    public void setCursor(CursorPosition cursor) {
        this.cursor = cursor;
    }
}
