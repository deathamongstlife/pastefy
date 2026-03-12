package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Table("paste_views")
public class PasteView extends Model {
    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column(size = 8)
    private String userId;

    @Column(size = 45)
    private String ipAddress;

    @Column(size = 500)
    private String userAgent;

    @Column(size = 255)
    private String referer;

    @Column(size = 100)
    private String country;

    @Column(size = 100)
    private String city;

    @Column
    private Timestamp viewedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Timestamp getViewedAt() {
        return viewedAt;
    }

    public Paste getPaste() {
        return Paste.get(pasteId);
    }

    public User getUser() {
        if (userId != null) {
            return User.get(userId);
        }
        return null;
    }

    public static PasteView get(String id) {
        return Repo.get(PasteView.class).where("id", id).first();
    }

    @Override
    public void save() {
        if (id == null) {
            id = org.javawebstack.webutils.util.RandomUtil.string(8);
            viewedAt = Timestamp.from(Instant.now());
        }
        super.save();
    }
}
