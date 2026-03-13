package cc.allyapps.pastely.model.responses.notification;

import com.google.gson.annotations.SerializedName;

/**
 * UnreadCountResponse - Response with count of unread notifications
 */
public class UnreadCountResponse {

    @SerializedName("unread_count")
    public long unreadCount;

    public UnreadCountResponse(long count) {
        this.unreadCount = count;
    }
}
