package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Table("user_follows")
public class UserFollow extends Model {
    @Column(size = 8)
    private String followerId;

    @Column(size = 8)
    private String followingId;

    @Column
    private Timestamp followedAt;

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public Timestamp getFollowedAt() {
        return followedAt;
    }

    public User getFollower() {
        return User.get(followerId);
    }

    public User getFollowing() {
        return User.get(followingId);
    }

    @Override
    public void save() {
        if (followedAt == null) {
            followedAt = Timestamp.from(Instant.now());
        }
        super.save();
    }
}
