package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.PasteComment;
import cc.allyapps.pastely.model.database.User;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class CommentResponse {
    public String id;

    @SerializedName("paste_id")
    public String pasteId;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("parent_comment_id")
    public String parentCommentId;

    public String content;

    @SerializedName("line_number")
    public Integer lineNumber;

    @SerializedName("created_at")
    public Timestamp createdAt;

    @SerializedName("updated_at")
    public Timestamp updatedAt;

    public PublicUserResponse user;

    public static CommentResponse create(PasteComment comment) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.pasteId = comment.getPasteId();
        response.userId = comment.getUserId();
        response.parentCommentId = comment.getParentCommentId();
        response.content = comment.getContent();
        response.lineNumber = comment.getLineNumber();
        response.createdAt = comment.getCreatedAt();
        response.updatedAt = comment.getUpdatedAt();

        User user = comment.getUser();
        if (user != null) {
            response.user = PublicUserResponse.create(user);
        }

        return response;
    }
}
