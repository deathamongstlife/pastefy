package de.interaapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteRevision - Stores version history for pastes using diffs
 * Part of the Version Control System feature
 */
@Dates
@Table("paste_revisions")
public class PasteRevision extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column(size = 8)
    private String userId;

    @Column(size = 8)
    private String branchId;

    @Column
    private String commitMessage;

    @Column(size = 16777215)
    private String diffContent; // Stores diff instead of full content

    @Column
    private String previousRevisionId;

    @Column
    private Integer revisionNumber;

    @Column
    private Timestamp createdAt;

    @Column
    private String parentTitle; // Title at this revision

    public PasteRevision() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
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

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getDiffContent() {
        return diffContent;
    }

    public void setDiffContent(String diffContent) {
        this.diffContent = diffContent;
    }

    public String getPreviousRevisionId() {
        return previousRevisionId;
    }

    public void setPreviousRevisionId(String previousRevisionId) {
        this.previousRevisionId = previousRevisionId;
    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }
}
