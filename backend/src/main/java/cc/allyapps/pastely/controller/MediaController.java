package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.AttachToPasteRequest;
import cc.allyapps.pastely.model.responses.ActionResponse;
import cc.allyapps.pastely.model.responses.AttachmentResponse;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.multipart.MultipartFile;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.webutils.util.RandomUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MediaController - File upload and attachment management
 * Part of Phase 3: Media Support
 */
@PathPrefix("/api/v2/media")
public class MediaController extends HttpController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Post("/upload")
    @With({"auth", "rate-limiter"})
    public AttachmentResponse uploadFile(@Attrib("user") User user, Exchange exchange) {
        if (!exchange.hasMultipart()) {
            throw new HTTPException(400, "No file uploaded");
        }

        List<MultipartFile> files = exchange.multipart("file");
        if (files.isEmpty()) {
            throw new HTTPException(400, "No file uploaded");
        }

        MultipartFile file = files.get(0);
        String originalFilename = file.filename();
        String mimeType = file.contentType();
        byte[] fileData = file.data();
        long fileSize = fileData.length;

        // Validate file size
        if (fileSize > MAX_FILE_SIZE) {
            throw new HTTPException(400, "File too large (max 10MB)");
        }

        // Generate storage key
        String storageKey = RandomUtil.string(16) + "_" + sanitizeFilename(originalFilename);

        // Upload to MinIO/S3 if available
        String storageType = "NONE";
        String storagePath = null;

        if (Pastely.getInstance().isMinioEnabled()) {
            try {
                uploadToMinio(storageKey, fileData, mimeType);
                storageType = "MINIO";
                storagePath = "attachments/" + storageKey;
            } catch (Exception e) {
                throw new HTTPException(500, "Upload failed: " + e.getMessage());
            }
        } else {
            throw new HTTPException(503, "File storage not configured");
        }

        // Create attachment record
        PasteAttachment attachment = new PasteAttachment();
        attachment.setFilename(storageKey);
        attachment.setOriginalFilename(originalFilename);
        attachment.setMimeType(mimeType);
        attachment.setFileSize(fileSize);
        attachment.setStorageType(storageType);
        attachment.setStoragePath(storagePath);
        attachment.save();

        return AttachmentResponse.create(attachment);
    }

    @Post("/paste/{pasteKey}/attach")
    @With({"auth"})
    public ActionResponse attachToPaste(
            @Path("pasteKey") String pasteKey,
            @Body AttachToPasteRequest request,
            @Attrib("user") User user
    ) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new PermissionsDeniedException();
        }

        PasteAttachment attachment = Repo.get(PasteAttachment.class)
                .where("id", request.attachmentId)
                .first();

        if (attachment == null) {
            throw new NotFoundException("Attachment not found");
        }

        attachment.setPasteId(pasteKey);
        attachment.save();

        return ActionResponse.success();
    }

    @Get("/paste/{pasteKey}/attachments")
    @With({"auth-login-required-read"})
    public List<AttachmentResponse> getAttachments(
            @Path("pasteKey") String pasteKey,
            @Attrib("user") User user
    ) {
        Paste.getAccessiblePasteOrFail(pasteKey, user);

        List<PasteAttachment> attachments = Repo.get(PasteAttachment.class)
                .where("pasteId", pasteKey)
                .get();

        return attachments.stream()
                .map(AttachmentResponse::create)
                .collect(Collectors.toList());
    }

    @Get("/download/{attachmentId}")
    @With({"auth-login-required-read"})
    public void download(
            @Path("attachmentId") String attachmentId,
            Exchange exchange,
            @Attrib("user") User user
    ) throws Exception {
        PasteAttachment attachment = Repo.get(PasteAttachment.class)
                .where("id", attachmentId)
                .first();

        if (attachment == null) {
            throw new NotFoundException("Attachment not found");
        }

        // Check paste access if attached to a paste
        if (attachment.getPasteId() != null) {
            Paste.getAccessiblePasteOrFail(attachment.getPasteId(), user);
        }

        // Download from MinIO
        if (Pastely.getInstance().isMinioEnabled() && "MINIO".equals(attachment.getStorageType())) {
            try {
                InputStream stream = downloadFromMinio(attachment.getFilename());
                exchange.header("Content-Type", attachment.getMimeType());
                exchange.header("Content-Disposition",
                    "attachment; filename=\"" + attachment.getOriginalFilename() + "\"");
                stream.transferTo(exchange.rawOutputStream());
                stream.close();
            } catch (Exception e) {
                throw new HTTPException(500, "Download failed: " + e.getMessage());
            }
        } else {
            throw new HTTPException(404, "File not found");
        }
    }

    @Delete("/{attachmentId}")
    @With({"auth"})
    public ActionResponse delete(
            @Path("attachmentId") String attachmentId,
            @Attrib("user") User user
    ) {
        PasteAttachment attachment = Repo.get(PasteAttachment.class)
                .where("id", attachmentId)
                .first();

        if (attachment == null) {
            throw new NotFoundException("Attachment not found");
        }

        // Check if user owns the paste this attachment is attached to
        if (attachment.getPasteId() != null) {
            Paste paste = Paste.get(attachment.getPasteId());
            if (paste != null && !Objects.equals(paste.getUserId(), user.getId())) {
                throw new PermissionsDeniedException();
            }
        }

        // Delete from MinIO
        if (Pastely.getInstance().isMinioEnabled() && "MINIO".equals(attachment.getStorageType())) {
            deleteFromMinio(attachment.getFilename());
        }

        attachment.delete();
        return ActionResponse.success();
    }

    // Helper methods

    private void uploadToMinio(String key, byte[] data, String contentType) throws Exception {
        Pastely.getInstance().getMinioClient().putObject(
                PutObjectArgs.builder()
                        .bucket(Pastely.getInstance().getMinioBucket())
                        .object("attachments/" + key)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    private InputStream downloadFromMinio(String key) throws Exception {
        return Pastely.getInstance().getMinioClient().getObject(
                GetObjectArgs.builder()
                        .bucket(Pastely.getInstance().getMinioBucket())
                        .object("attachments/" + key)
                        .build()
        );
    }

    private void deleteFromMinio(String key) {
        try {
            Pastely.getInstance().getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(Pastely.getInstance().getMinioBucket())
                            .object("attachments/" + key)
                            .build()
            );
        } catch (Exception e) {
            // Log but don't fail - file might already be deleted
            System.err.println("Failed to delete attachment from MinIO: " + e.getMessage());
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
