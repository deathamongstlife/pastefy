package cc.allyapps.pastely.model.requests.notification;

/**
 * UpdateNotificationPreferenceRequest - Request to update notification preferences
 */
public class UpdateNotificationPreferenceRequest {

    public boolean emailEnabled = true;

    public boolean pushEnabled = true;

    public boolean webhookEnabled = false;

    public String webhookUrl;

    public String typeFilters; // Comma-separated list of enabled types
}
