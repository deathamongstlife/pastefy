package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import org.javawebstack.webutils.util.RandomUtil;
import java.sql.Timestamp;
import java.time.Instant;

@Dates
@Table("paste_revisions")
public class PasteRevision extends Model {
    @Column(size = 8)
    private String id;

    @Column(size = 8)
    @Filterable
    private String pasteId;

    @Column(size = 8)
    @Filterable
    private String branchId;

    @Column(size = 8)
    private String parentRevisionId;

    @Column
    private Integer revisionNumber;

    @Column(size = 16777215) // MEDIUMTEXT for diff content
    private String diff;

    @Column(size = 500)
    private String commitMessage;

    @Column(size = 8)
    private String authorId;

    @Column
    private Timestamp createdAt;

    public PasteRevision() {
        id = RandomUtil.string(8);
        createdAt = Timestamp.from(Instant.now());
    }

    public static PasteRevision get(String revisionId) {
        return Repo.get(PasteRevision.class).where("id", revisionId).first();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPasteId() { return pasteId; }
    public void setPasteId(String pasteId) { this.pasteId = pasteId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getParentRevisionId() { return parentRevisionId; }
    public void setParentRevisionId(String parentRevisionId) { this.parentRevisionId = parentRevisionId; }

    public Integer getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(Integer revisionNumber) { this.revisionNumber = revisionNumber; }

    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }

    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Timestamp getCreatedAt() { return createdAt; }

    public User getAuthor() {
        return User.get(authorId);
    }

    public Paste getPaste() {
        return Paste.get(pasteId);
    }

    public PasteBranch getBranch() {
        return PasteBranch.get(branchId);
    }

    public PasteRevision getParentRevision() {
        if (parentRevisionId == null) return null;
        return PasteRevision.get(parentRevisionId);
    }
}
