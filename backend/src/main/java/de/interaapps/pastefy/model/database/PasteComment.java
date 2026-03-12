package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteComment - Comments on pastes
 * Part of the Social Features
 */
@Dates
@Table("paste_comments")
public class PasteComment extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column(size = 8)
    private String userId;

    @Column(size = 8)
    private String parentCommentId; // For nested comments

    @Column(size = 4096)
    private String content;

    @Column
    private boolean isEdited;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    @Column
    private boolean isDeleted;

    public PasteComment() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
        this.isEdited = false;
        this.isDeleted = false;
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

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
