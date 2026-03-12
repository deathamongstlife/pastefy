package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteBranch - Supports branching in version control
 * Part of the Version Control System feature
 */
@Dates
@Table("paste_branches")
public class PasteBranch extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column(size = 8)
    private String userId;

    @Column
    private String branchName;

    @Column(size = 8)
    private String baseRevisionId;

    @Column(size = 8)
    private String latestRevisionId;

    @Column
    private boolean isDefault;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    public PasteBranch() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
        this.isDefault = false;
    }

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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBaseRevisionId() {
        return baseRevisionId;
    }

    public void setBaseRevisionId(String baseRevisionId) {
        this.baseRevisionId = baseRevisionId;
    }

    public String getLatestRevisionId() {
        return latestRevisionId;
    }

    public void setLatestRevisionId(String latestRevisionId) {
        this.latestRevisionId = latestRevisionId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
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
