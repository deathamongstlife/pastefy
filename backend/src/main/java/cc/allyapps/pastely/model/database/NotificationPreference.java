package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * NotificationPreference - User preferences for notification delivery
 * Controls how and when users receive notifications
 */
@Dates
@Table("notification_preferences")
public class NotificationPreference extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column
    private boolean emailEnabled;

    @Column
    private boolean pushEnabled;

    @Column
    private boolean webhookEnabled;

    @Column
    private String webhookUrl;

    @Column
    private String typeFilters; // Comma-separated list of enabled notification types

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public NotificationPreference() {
        this.id = RandomUtil.string(8);
        this.emailEnabled = true;
        this.pushEnabled = true;
        this.webhookEnabled = false;
        this.typeFilters = ""; // Empty means all types enabled
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

    public boolean isTypeEnabled(String typeName) {
        if (typeFilters == null || typeFilters.isEmpty()) {
            return true; // All types enabled by default
        }
        return typeFilters.contains(typeName);
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

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getTypeFilters() {
        return typeFilters;
    }

    public void setTypeFilters(String typeFilters) {
        this.typeFilters = typeFilters;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
