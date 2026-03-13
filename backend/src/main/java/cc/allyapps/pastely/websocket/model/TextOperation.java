package cc.allyapps.pastely.websocket.model;

import java.sql.Timestamp;

public class TextOperation {
    private String type;
    private int position;
    private String text;
    private int version;
    private String userId;
    private Timestamp timestamp;

    public TextOperation() {}

    public TextOperation(String type, int position, String text, int version, String userId) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.version = version;
        this.userId = userId;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
