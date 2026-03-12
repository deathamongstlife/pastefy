package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;

@Table("user_profiles")
public class UserProfile extends Model {
    @Column(size = 8)
    private String userId;

    @Column(size = 1000)
    private String bio;

    @Column(size = 255)
    private String website;

    @Column(size = 255)
    private String location;

    @Column(size = 255)
    private String company;

    @Column(size = 255)
    private String avatarUrl;

    @Column(size = 255)
    private String githubUsername;

    @Column(size = 255)
    private String twitterUsername;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getTwitterUsername() {
        return twitterUsername;
    }

    public void setTwitterUsername(String twitterUsername) {
        this.twitterUsername = twitterUsername;
    }

    public User getUser() {
        return User.get(userId);
    }

    public static UserProfile getByUserId(String userId) {
        return Repo.get(UserProfile.class).where("userId", userId).first();
    }
}
