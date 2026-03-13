package cc.allyapps.pastely.model.responses.notification;

import cc.allyapps.pastely.model.database.UserNotification;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

/**
 * NotificationResponse - Response DTO for user notifications
 */
public class NotificationResponse {

    public String id;

    public String type;

    public String title;

    public String message;

    public String data;

    public String url;

    @SerializedName("is_read")
    public boolean isRead;

    @SerializedName("created_at")
    public Timestamp createdAt;

    public static NotificationResponse create(UserNotification notification) {
        NotificationResponse response = new NotificationResponse();
        response.id = notification.getId();
        response.type = notification.getType() != null ? notification.getType().name() : null;
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.data = notification.getData();
        response.url = notification.getUrl();
        response.isRead = notification.isRead();
        response.createdAt = notification.getCreatedAt();
        return response;
    }
}
