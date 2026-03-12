package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

/**
 * UserProfile - Extended user profile information
 * Part of the Social Features
 */
@Dates
@Table("user_profiles")
public class UserProfile extends Model {

    @Column(size = 8)
    private String userId;

    @Column(size = 500)
    private String bio;

    @Column
    private String website;

    @Column
    private String twitter;

    @Column
    private String github;

    @Column
    private String linkedin;

    @Column
    private String location;

    @Column
    private String company;

    @Column
    private String avatarUrl;

    @Column
    private boolean emailNotifications;

    @Column
    private boolean activityFeedPublic;

    @Column
    private String theme; // dark, light, auto

    @Column
    private String editorTheme;

    @Column
    private Integer fontSize;

    public UserProfile() {
        this.emailNotifications = true;
        this.activityFeedPublic = true;
        this.theme = "auto";
        this.editorTheme = "monokai";
        this.fontSize = 14;
    }

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

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
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

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isActivityFeedPublic() {
        return activityFeedPublic;
    }

    public void setActivityFeedPublic(boolean activityFeedPublic) {
        this.activityFeedPublic = activityFeedPublic;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getEditorTheme() {
        return editorTheme;
    }

    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }
}
