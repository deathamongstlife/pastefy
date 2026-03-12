package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.PasteRevision;
import cc.allyapps.pastely.model.responses.user.PublicUserResponse;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class PasteRevisionResponse {
    public String id;

    @SerializedName("paste_id")
    public String pasteId;

    @SerializedName("branch_id")
    public String branchId;

    @SerializedName("parent_revision_id")
    public String parentRevisionId;

    @SerializedName("revision_number")
    public Integer revisionNumber;

    @SerializedName("commit_message")
    public String commitMessage;

    public PublicUserResponse author;

    @SerializedName("created_at")
    public Timestamp createdAt;

    // Include diff only if requested
    public String diff;

    public static PasteRevisionResponse create(PasteRevision revision, boolean includeDiff) {
        PasteRevisionResponse response = new PasteRevisionResponse();
        response.id = revision.getId();
        response.pasteId = revision.getPasteId();
        response.branchId = revision.getBranchId();
        response.parentRevisionId = revision.getParentRevisionId();
        response.revisionNumber = revision.getRevisionNumber();
        response.commitMessage = revision.getCommitMessage();
        response.author = PublicUserResponse.create(revision.getAuthor());
        response.createdAt = revision.getCreatedAt();

        if (includeDiff) {
            response.diff = revision.getDiff();
        }

        return response;
    }
}
