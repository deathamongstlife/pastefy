package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Table("user_activities")
public class UserActivity extends Model {
    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column(size = 50)
    private String activityType;

    @Column(size = 8)
    private String targetId;

    @Column(size = 50)
    private String targetType;

    @Column(size = 1000)
    private String metadata;

    @Column
    private Timestamp createdAt;

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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
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

    public User getUser() {
        return User.get(userId);
    }

    public static UserActivity get(String id) {
        return Repo.get(UserActivity.class).where("id", id).first();
    }

    @Override
    public void save() {
        if (id == null) {
            id = org.javawebstack.webutils.util.RandomUtil.string(8);
            createdAt = Timestamp.from(Instant.now());
        }
        super.save();
    }
}
