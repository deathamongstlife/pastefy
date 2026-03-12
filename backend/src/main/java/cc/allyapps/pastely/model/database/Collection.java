package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Dates
@Table("collections")
public class Collection extends Model {
    @Column(size = 8)
    private String id;

    @Column(size = 255)
    private String name;

    @Column(size = 1000)
    private String description;

    @Column(size = 8)
    private String userId;

    @Column
    private boolean isPublic = false;

    @Column(size = 50)
    private String icon;

    @Column(size = 50)
    private String color;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return User.get(userId);
    }

    public static Collection get(String id) {
        return Repo.get(Collection.class).where("id", id).first();
    }

    @Override
    public void save() {
        if (id == null) {
            id = org.javawebstack.webutils.util.RandomUtil.string(8);
            createdAt = Timestamp.from(Instant.now());
        }
        updatedAt = Timestamp.from(Instant.now());
        super.save();
    }

    @Override
    public void delete() {
        Repo.get(CollectionPaste.class).where("collectionId", id).delete();
        super.delete();
    }
}
