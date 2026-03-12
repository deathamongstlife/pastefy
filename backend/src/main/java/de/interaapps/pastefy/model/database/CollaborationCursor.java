package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * CollaborationCursor - Tracks cursor positions for real-time collaboration
 * Part of the Real-time Collaboration feature
 */
@Table("collaboration_cursors")
public class CollaborationCursor extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String sessionId;

    @Column(size = 8)
    private String userId;

    @Column
    private String userDisplayName;

    @Column
    private String userColor;

    @Column
    private Integer cursorLine;

    @Column
    private Integer cursorColumn;

    @Column
    private Integer selectionStartLine;

    @Column
    private Integer selectionStartColumn;

    @Column
    private Integer selectionEndLine;

    @Column
    private Integer selectionEndColumn;

    @Column
    private Timestamp lastSeenAt;

    public CollaborationCursor() {
        this.id = RandomUtil.string(8);
        this.lastSeenAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(String userColor) {
        this.userColor = userColor;
    }

    public Integer getCursorLine() {
        return cursorLine;
    }

    public void setCursorLine(Integer cursorLine) {
        this.cursorLine = cursorLine;
    }

    public Integer getCursorColumn() {
        return cursorColumn;
    }

    public void setCursorColumn(Integer cursorColumn) {
        this.cursorColumn = cursorColumn;
    }

    public Integer getSelectionStartLine() {
        return selectionStartLine;
    }

    public void setSelectionStartLine(Integer selectionStartLine) {
        this.selectionStartLine = selectionStartLine;
    }

    public Integer getSelectionStartColumn() {
        return selectionStartColumn;
    }

    public void setSelectionStartColumn(Integer selectionStartColumn) {
        this.selectionStartColumn = selectionStartColumn;
    }

    public Integer getSelectionEndLine() {
        return selectionEndLine;
    }

    public void setSelectionEndLine(Integer selectionEndLine) {
        this.selectionEndLine = selectionEndLine;
    }

    public Integer getSelectionEndColumn() {
        return selectionEndColumn;
    }

    public void setSelectionEndColumn(Integer selectionEndColumn) {
        this.selectionEndColumn = selectionEndColumn;
    }

    public Timestamp getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Timestamp lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
