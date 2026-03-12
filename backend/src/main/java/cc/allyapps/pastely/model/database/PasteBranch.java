package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import org.javawebstack.webutils.util.RandomUtil;
import java.sql.Timestamp;
import java.time.Instant;

@Dates
@Table("paste_branches")
public class PasteBranch extends Model {
    @Column(size = 8)
    private String id;

    @Column(size = 8)
    @Filterable
    private String pasteId;

    @Column(size = 100)
    @Filterable
    private String name;

    @Column(size = 8)
    private String headRevisionId;

    @Column
    private Boolean isDefault = false;

    @Column(size = 8)
    private String createdBy;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public PasteBranch() {
        id = RandomUtil.string(8);
        createdAt = Timestamp.from(Instant.now());
        updatedAt = Timestamp.from(Instant.now());
    }

    public static PasteBranch get(String branchId) {
        return Repo.get(PasteBranch.class).where("id", branchId).first();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPasteId() { return pasteId; }
    public void setPasteId(String pasteId) { this.pasteId = pasteId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHeadRevisionId() { return headRevisionId; }
    public void setHeadRevisionId(String headRevisionId) {
        this.headRevisionId = headRevisionId;
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public Boolean getIsDefault() { return isDefault != null && isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    public Paste getPaste() {
        return Paste.get(pasteId);
    }

    public PasteRevision getHeadRevision() {
        if (headRevisionId == null) return null;
        return PasteRevision.get(headRevisionId);
    }

    public User getCreator() {
        return User.get(createdBy);
    }
}
