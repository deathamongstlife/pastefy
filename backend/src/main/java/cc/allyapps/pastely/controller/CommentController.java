package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.orm.Repo;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/paste/{pasteKey}/comment")
public class CommentController extends HttpController {

    @Get
    public List<CommentResponse> list(@Path("pasteKey") String pasteKey,
                                     @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        List<PasteComment> comments = Repo.get(PasteComment.class)
            .where("pasteId", pasteKey)
            .whereNull("parentCommentId")
            .order("createdAt", false)
            .all();

        return comments.stream()
            .map(CommentResponse::create)
            .collect(Collectors.toList());
    }

    @Post
    @With({"auth", "rate-limiter"})
    public CommentResponse create(@Path("pasteKey") String pasteKey,
                                 @Body CreateCommentRequest request,
                                 @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (request.parentCommentId != null) {
            PasteComment parent = PasteComment.get(request.parentCommentId);
            if (parent == null || !parent.getPasteId().equals(pasteKey)) {
                throw new NotFoundException("Parent comment not found");
            }
        }

        PasteComment comment = new PasteComment();
        comment.setPasteId(pasteKey);
        comment.setUserId(user.getId());
        comment.setContent(request.content);
        comment.setParentCommentId(request.parentCommentId);
        comment.setLineNumber(request.lineNumber);
        comment.save();

        Pastely.getInstance().executeAsync(() -> {
            logActivity(user.getId(), "comment", pasteKey, "paste");
        });

        return CommentResponse.create(comment);
    }

    @Get("/{commentId}")
    public CommentResponse get(@Path("pasteKey") String pasteKey,
                              @Path("commentId") String commentId,
                              @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteComment comment = PasteComment.get(commentId);
        if (comment == null || !comment.getPasteId().equals(pasteKey)) {
            throw new NotFoundException("Comment not found");
        }

        return CommentResponse.create(comment);
    }

    @Put("/{commentId}")
    @With({"auth"})
    public CommentResponse update(@Path("pasteKey") String pasteKey,
                                 @Path("commentId") String commentId,
                                 @Body CreateCommentRequest request,
                                 @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteComment comment = PasteComment.get(commentId);
        if (comment == null || !comment.getPasteId().equals(pasteKey)) {
            throw new NotFoundException("Comment not found");
        }

        if (!comment.getUserId().equals(user.getId())) {
            throw new PermissionsDeniedException();
        }

        comment.setContent(request.content);
        comment.save();

        return CommentResponse.create(comment);
    }

    @Delete("/{commentId}")
    @With({"auth"})
    public ActionResponse delete(@Path("pasteKey") String pasteKey,
                                @Path("commentId") String commentId,
                                @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteComment comment = PasteComment.get(commentId);
        if (comment == null || !comment.getPasteId().equals(pasteKey)) {
            throw new NotFoundException("Comment not found");
        }

        if (!comment.getUserId().equals(user.getId())) {
            throw new PermissionsDeniedException();
        }

        comment.delete();
        return new ActionResponse(true);
    }

    @Get("/{commentId}/replies")
    public List<CommentResponse> getReplies(@Path("pasteKey") String pasteKey,
                                           @Path("commentId") String commentId,
                                           @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteComment comment = PasteComment.get(commentId);
        if (comment == null || !comment.getPasteId().equals(pasteKey)) {
            throw new NotFoundException("Comment not found");
        }

        List<PasteComment> replies = Repo.get(PasteComment.class)
            .where("parentCommentId", commentId)
            .order("createdAt", false)
            .all();

        return replies.stream()
            .map(CommentResponse::create)
            .collect(Collectors.toList());
    }

    private void logActivity(String userId, String type, String targetId, String targetType) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setActivityType(type);
        activity.setTargetId(targetId);
        activity.setTargetType(targetType);
        activity.save();
    }
}
