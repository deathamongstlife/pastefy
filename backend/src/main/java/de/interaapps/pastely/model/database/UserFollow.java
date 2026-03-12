package de.interaapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * UserFollow - Manages user following relationships
 * Part of the Social Features
 */
@Dates
@Table("user_follows")
public class UserFollow extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String followerId; // User who is following

    @Column(size = 8)
    private String followingId; // User being followed

    @Column
    private Timestamp createdAt;

    public UserFollow() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
