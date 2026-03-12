package cc.allyapps.pastely.model.responses;

import com.google.gson.annotations.SerializedName;

public class UserStatsResponse {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("paste_count")
    public Integer pasteCount;

    @SerializedName("follower_count")
    public Integer followerCount;

    @SerializedName("following_count")
    public Integer followingCount;
}
