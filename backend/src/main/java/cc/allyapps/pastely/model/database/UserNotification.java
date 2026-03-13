package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * UserNotification - Enhanced notification model for Phase 4
 * Supports multiple notification types and rich data
 */
@Dates
@Table("user_notifications")
public class UserNotification extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column
    private NotificationType type;

    @Column
    private String title;

    @Column(size = 1024)
    private String message;

    @Column(size = 4096)
    private String data; // JSON data for additional context

    @Column
    private String url;

    @Column
    private boolean isRead;

    @Column
    private boolean received;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public UserNotification() {
        this.id = RandomUtil.string(8);
        this.isRead = false;
        this.received = false;
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
    }

    @Override
    public void save() {
        if (id == null || id.isEmpty()) {
            id = RandomUtil.string(8);
            createdAt = Timestamp.from(Instant.now());
        }
        updatedAt = Timestamp.from(Instant.now());
        super.save();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public enum NotificationType {
        PASTE_COMMENT,
        PASTE_SHARED,
        USER_FOLLOWED,
        PASTE_LIKED,
        PASTE_STARRED,
        PASTE_FORKED,
        COLLABORATION_INVITE,
        SYSTEM,
        SECURITY_ALERT,
        ADMIN_MESSAGE
    }
}
