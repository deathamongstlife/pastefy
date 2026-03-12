package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.UserActivity;
import cc.allyapps.pastely.model.database.User;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class ActivityResponse {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("activity_type")
    public String activityType;

    @SerializedName("target_id")
    public String targetId;

    @SerializedName("target_type")
    public String targetType;

    public String metadata;

    @SerializedName("created_at")
    public Timestamp createdAt;

    public PublicUserResponse user;

    public static ActivityResponse create(UserActivity activity) {
        ActivityResponse response = new ActivityResponse();
        response.id = activity.getId();
        response.userId = activity.getUserId();
        response.activityType = activity.getActivityType();
        response.targetId = activity.getTargetId();
        response.targetType = activity.getTargetType();
        response.metadata = activity.getMetadata();
        response.createdAt = activity.getCreatedAt();

        User user = activity.getUser();
        if (user != null) {
            response.user = PublicUserResponse.create(user);
        }

        return response;
    }
}
