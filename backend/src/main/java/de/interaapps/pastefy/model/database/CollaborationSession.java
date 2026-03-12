package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * CollaborationSession - Manages real-time collaboration sessions
 * Part of the Real-time Collaboration feature
 */
@Dates
@Table("collaboration_sessions")
public class CollaborationSession extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column(size = 8)
    private String ownerId;

    @Column
    private String sessionToken;

    @Column
    private boolean isActive;

    @Column
    private Integer maxParticipants;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp expiresAt;

    @Column
    private Timestamp lastActivityAt;

    public CollaborationSession() {
        this.id = RandomUtil.string(8);
        this.sessionToken = RandomUtil.string(32);
        this.isActive = true;
        this.createdAt = Timestamp.from(Instant.now());
        this.lastActivityAt = Timestamp.from(Instant.now());
        this.maxParticipants = 10;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Timestamp lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}
