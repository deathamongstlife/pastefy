package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.PasteAttachment;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

/**
 * AttachmentResponse - Response for file attachments
 */
public class AttachmentResponse {

    public String id;

    @SerializedName("paste_id")
    public String pasteId;

    public String filename;

    @SerializedName("original_filename")
    public String originalFilename;

    @SerializedName("mime_type")
    public String mimeType;

    @SerializedName("file_size")
    public Long fileSize;

    @SerializedName("storage_type")
    public String storageType;

    @SerializedName("download_url")
    public String downloadUrl;

    @SerializedName("created_at")
    public Timestamp createdAt;

    public static AttachmentResponse create(PasteAttachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.id = attachment.getId();
        response.pasteId = attachment.getPasteId();
        response.filename = attachment.getFilename();
        response.originalFilename = attachment.getOriginalFilename();
        response.mimeType = attachment.getMimeType();
        response.fileSize = attachment.getFileSize();
        response.storageType = attachment.getStorageType();
        response.downloadUrl = "/api/v2/media/download/" + attachment.getId();
        response.createdAt = attachment.getCreatedAt();
        return response;
    }
}
