package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.PasteBranch;
import cc.allyapps.pastely.model.responses.user.PublicUserResponse;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class PasteBranchResponse {
    public String id;

    @SerializedName("paste_id")
    public String pasteId;

    public String name;

    @SerializedName("head_revision_id")
    public String headRevisionId;

    @SerializedName("is_default")
    public Boolean isDefault;

    @SerializedName("created_by")
    public PublicUserResponse creator;

    @SerializedName("created_at")
    public Timestamp createdAt;

    @SerializedName("updated_at")
    public Timestamp updatedAt;

    public static PasteBranchResponse create(PasteBranch branch) {
        PasteBranchResponse response = new PasteBranchResponse();
        response.id = branch.getId();
        response.pasteId = branch.getPasteId();
        response.name = branch.getName();
        response.headRevisionId = branch.getHeadRevisionId();
        response.isDefault = branch.getIsDefault();
        response.creator = PublicUserResponse.create(branch.getCreator());
        response.createdAt = branch.getCreatedAt();
        response.updatedAt = branch.getUpdatedAt();
        return response;
    }
}
