package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteAccess - Manages advanced access control for pastes
 * Part of the Advanced Security feature
 */
@Dates
@Table("paste_access")
public class PasteAccess extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column
    private String passwordHash; // BCrypt hash

    @Column
    private String ipWhitelist; // Comma-separated IP addresses or CIDR blocks

    @Column
    private String ipBlacklist; // Comma-separated IP addresses or CIDR blocks

    @Column
    private Timestamp expiresAt;

    @Column
    private Integer maxViews; // For burn-after-read functionality

    @Column
    private Integer currentViews;

    @Column
    private boolean requiresAuth;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public PasteAccess() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
        this.currentViews = 0;
        this.requiresAuth = false;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getIpWhitelist() {
        return ipWhitelist;
    }

    public void setIpWhitelist(String ipWhitelist) {
        this.ipWhitelist = ipWhitelist;
    }

    public String getIpBlacklist() {
        return ipBlacklist;
    }

    public void setIpBlacklist(String ipBlacklist) {
        this.ipBlacklist = ipBlacklist;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getMaxViews() {
        return maxViews;
    }

    public void setMaxViews(Integer maxViews) {
        this.maxViews = maxViews;
    }

    public Integer getCurrentViews() {
        return currentViews;
    }

    public void setCurrentViews(Integer currentViews) {
        this.currentViews = currentViews;
    }

    public void incrementViews() {
        this.currentViews = (this.currentViews == null ? 0 : this.currentViews) + 1;
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return expiresAt.before(Timestamp.from(Instant.now()));
    }

    public boolean hasReachedMaxViews() {
        if (maxViews == null) return false;
        return currentViews != null && currentViews >= maxViews;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
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
