package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.database.UserProfile;
import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    public String id;
    public String name;
    public String username;

    public String bio;
    public String website;
    public String location;
    public String company;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("github_username")
    public String githubUsername;

    @SerializedName("twitter_username")
    public String twitterUsername;

    @SerializedName("paste_count")
    public Integer pasteCount;

    @SerializedName("follower_count")
    public Integer followerCount;

    @SerializedName("following_count")
    public Integer followingCount;

    public static UserProfileResponse create(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.username = user.getUsername();

        if (profile != null) {
            response.bio = profile.getBio();
            response.website = profile.getWebsite();
            response.location = profile.getLocation();
            response.company = profile.getCompany();
            response.avatarUrl = profile.getAvatarUrl();
            response.githubUsername = profile.getGithubUsername();
            response.twitterUsername = profile.getTwitterUsername();
        }

        return response;
    }
}
