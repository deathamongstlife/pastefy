package de.interaapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * PasteAttachment - File attachments for pastes
 * Part of the Media Support feature
 */
@Dates
@Table("paste_attachments")
public class PasteAttachment extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column
    private String filename;

    @Column
    private String originalFilename;

    @Column
    private String mimeType;

    @Column
    private Long fileSize; // In bytes

    @Column
    private String storageType; // S3, MINIO, LOCAL

    @Column
    private String storagePath;

    @Column
    private String thumbnailPath; // For images

    @Column
    private Timestamp createdAt;

    public PasteAttachment() {
        this.id = RandomUtil.string(8);
        this.createdAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
