package cc.allyapps.pastely.model.responses.notification;

import cc.allyapps.pastely.model.database.NotificationPreference;
import com.google.gson.annotations.SerializedName;

/**
 * NotificationPreferenceResponse - Response DTO for notification preferences
 */
public class NotificationPreferenceResponse {

    public String id;

    @SerializedName("email_enabled")
    public boolean emailEnabled;

    @SerializedName("push_enabled")
    public boolean pushEnabled;

    @SerializedName("webhook_enabled")
    public boolean webhookEnabled;

    @SerializedName("webhook_url")
    public String webhookUrl;

    @SerializedName("type_filters")
    public String typeFilters;

    public static NotificationPreferenceResponse create(NotificationPreference preference) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.id = preference.getId();
        response.emailEnabled = preference.isEmailEnabled();
        response.pushEnabled = preference.isPushEnabled();
        response.webhookEnabled = preference.isWebhookEnabled();
        response.webhookUrl = preference.getWebhookUrl();
        response.typeFilters = preference.getTypeFilters();
        return response;
    }
}
