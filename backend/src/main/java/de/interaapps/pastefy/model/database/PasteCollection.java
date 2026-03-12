package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteCollection - Curated collections of pastes
 * Part of the Enhanced Organization feature
 */
@Dates
@Table("paste_collections")
public class PasteCollection extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String userId;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private boolean isPublic;

    @Column
    private String coverImage; // URL or path to cover image

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public PasteCollection() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
        this.isPublic = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
