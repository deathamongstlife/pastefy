package de.interaapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Webhook - Webhook subscriptions for events
 * Part of the Integrations feature
 */
@Dates
@Table("webhooks")
public class Webhook extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column
    private String url;

    @Column
    private String secret; // For signing payloads

    @Column
    private String events; // Comma-separated: paste.created,paste.updated,paste.deleted

    @Column
    private boolean isActive;

    @Column
    private Integer failureCount;

    @Column
    private Timestamp lastTriggeredAt;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public Webhook() {
        this.id = RandomUtil.string(8);
        this.secret = RandomUtil.string(32);
        this.isActive = true;
        this.failureCount = 0;
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public void incrementFailureCount() {
        this.failureCount = (this.failureCount == null ? 0 : this.failureCount) + 1;
    }

    public Timestamp getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(Timestamp lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
