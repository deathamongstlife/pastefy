package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.params.Query;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.http.router.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * SocialController - Social features for users
 * Part of the Social Features
 */
@PathPrefix("/api/v2/social")
public class SocialController extends HttpController {

    @Post("/follow/{userId}")
    @With({"auth"})
    public ActionResponse followUser(@Path("userId") String userId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        if (Objects.equals(user.getId(), userId)) {
            return ActionResponse.error("Cannot follow yourself");
        }

        User targetUser = Repo.get(User.class).where("id", userId).first();
        if (targetUser == null) throw new NotFoundException();

        UserFollow existing = Repo.get(UserFollow.class)
                .where("followerId", user.getId())
                .where("followingId", userId)
                .first();

        if (existing != null) {
            return ActionResponse.error("Already following");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(user.getId());
        follow.setFollowingId(userId);
        follow.save();

        // Log activity
        logActivity(user.getId(), "FOLLOWED_USER", userId, null);

        return ActionResponse.success();
    }

    @Delete("/follow/{userId}")
    @With({"auth"})
    public ActionResponse unfollowUser(@Path("userId") String userId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Repo.get(UserFollow.class)
                .where("followerId", user.getId())
                .where("followingId", userId)
                .delete();

        return ActionResponse.success();
    }

    @Get("/followers/{userId}")
    public List<UserFollow> getFollowers(
            @Path("userId") String userId,
            @Query("limit") Integer limit
    ) {
        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(UserFollow.class)
                .where("followingId", userId)
                .limit(limit)
                .get();
    }

    @Get("/following/{userId}")
    public List<UserFollow> getFollowing(
            @Path("userId") String userId,
            @Query("limit") Integer limit
    ) {
        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(UserFollow.class)
                .where("followerId", userId)
                .limit(limit)
                .get();
    }

    @Post("/paste/{pasteKey}/comments")
    @With({"auth"})
    public PasteComment addComment(
            @Path("pasteKey") String pasteKey,
            @Body("content") String content,
            @Body("parentCommentId") String parentCommentId,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.get(pasteKey);
        if (paste == null) throw new NotFoundException();

        PasteComment comment = new PasteComment();
        comment.setPasteId(paste.getKey());
        comment.setUserId(user.getId());
        comment.setContent(content);
        comment.setParentCommentId(parentCommentId);
        comment.save();

        // Log activity
        logActivity(user.getId(), "COMMENTED", paste.getKey(), null);

        return comment;
    }

    @Get("/paste/{pasteKey}/comments")
    public List<PasteComment> getComments(
            @Path("pasteKey") String pasteKey,
            @Query("limit") Integer limit
    ) {
        Paste paste = Paste.get(pasteKey);
        if (paste == null) throw new NotFoundException();

        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(PasteComment.class)
                .where("pasteId", paste.getKey())
                .where("isDeleted", false)
                .orderBy("createdAt", false)
                .limit(limit)
                .get();
    }

    @Put("/comments/{commentId}")
    @With({"auth"})
    public PasteComment updateComment(
            @Path("commentId") String commentId,
            @Body("content") String content,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteComment comment = Repo.get(PasteComment.class).where("id", commentId).first();
        if (comment == null) throw new NotFoundException();

        if (!Objects.equals(comment.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        comment.setContent(content);
        comment.setEdited(true);
        comment.setUpdatedAt(Timestamp.from(Instant.now()));
        comment.save();

        return comment;
    }

    @Delete("/comments/{commentId}")
    @With({"auth"})
    public ActionResponse deleteComment(@Path("commentId") String commentId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteComment comment = Repo.get(PasteComment.class).where("id", commentId).first();
        if (comment == null) throw new NotFoundException();

        if (!Objects.equals(comment.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        comment.setDeleted(true);
        comment.save();

        return ActionResponse.success();
    }

    @Get("/activity/{userId}")
    public List<UserActivity> getUserActivity(
            @Path("userId") String userId,
            @Query("limit") Integer limit
    ) {
        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(UserActivity.class)
                .where("userId", userId)
                .orderBy("createdAt", true)
                .limit(limit)
                .get();
    }

    @Get("/feed")
    @With({"auth"})
    public List<UserActivity> getActivityFeed(
            @Query("limit") Integer limit,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        if (limit == null || limit > 100) {
            limit = 100;
        }

        // Get IDs of users being followed
        List<UserFollow> following = Repo.get(UserFollow.class)
                .where("followerId", user.getId())
                .get();

        if (following.isEmpty()) {
            return List.of();
        }

        List<String> followingIds = following.stream()
                .map(UserFollow::getFollowingId)
                .toList();

        return Repo.get(UserActivity.class)
                .where("userId", "IN", followingIds)
                .orderBy("createdAt", true)
                .limit(limit)
                .get();
    }

    private void logActivity(String userId, String activityType, String targetId, String metadata) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setActivityType(activityType);
        activity.setTargetId(targetId);
        activity.setMetadata(metadata);
        activity.save();
    }
}
