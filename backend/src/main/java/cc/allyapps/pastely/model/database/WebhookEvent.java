package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * WebhookEvent - Log of webhook delivery attempts
 * Part of the Integrations feature
 */
@Table("webhook_events")
public class WebhookEvent extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String webhookId;

    @Column
    private String eventType;

    @Column(size = 4096)
    private String payload;

    @Column
    private Integer responseCode;

    @Column(size = 1024)
    private String responseBody;

    @Column
    private boolean success;

    @Column
    private Timestamp attemptedAt;

    public WebhookEvent() {
        this.id = RandomUtil.string(8);
        this.attemptedAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Timestamp getAttemptedAt() {
        return attemptedAt;
    }
}
