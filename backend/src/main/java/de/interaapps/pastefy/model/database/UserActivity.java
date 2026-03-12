package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * UserActivity - Activity feed for user actions
 * Part of the Social Features
 */
@Table("user_activities")
public class UserActivity extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column
    private String activityType; // CREATED_PASTE, FORKED_PASTE, COMMENTED, STARRED, etc.

    @Column(size = 8)
    private String targetId; // ID of the paste, comment, etc.

    @Column
    private String metadata; // JSON string for additional data

    @Column
    private Timestamp createdAt;

    public UserActivity() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
