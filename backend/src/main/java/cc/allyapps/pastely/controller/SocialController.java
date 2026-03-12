package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.orm.Repo;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/social")
public class SocialController extends HttpController {

    @Post("/follow/{userId}")
    @With({"auth", "rate-limiter"})
    public ActionResponse follow(@Path("userId") String userId,
                                @Attrib("user") User user) {
        if (user.getId().equals(userId)) {
            throw new HTTPException(400, "Cannot follow yourself");
        }

        User targetUser = User.get(userId);
        if (targetUser == null) {
            throw new NotFoundException("User not found");
        }

        UserFollow existing = Repo.get(UserFollow.class)
            .where("followerId", user.getId())
            .where("followingId", userId)
            .first();

        if (existing != null) {
            throw new HTTPException(400, "Already following");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(user.getId());
        follow.setFollowingId(userId);
        follow.save();

        logActivity(user.getId(), "follow", userId, "user");

        return new ActionResponse(true);
    }

    @Delete("/follow/{userId}")
    @With({"auth"})
    public ActionResponse unfollow(@Path("userId") String userId,
                                  @Attrib("user") User user) {
        Repo.get(UserFollow.class)
            .where("followerId", user.getId())
            .where("followingId", userId)
            .delete();

        return new ActionResponse(true);
    }

    @Get("/followers")
    @With({"auth"})
    public List<PublicUserResponse> getFollowers(@Attrib("user") User user) {
        List<UserFollow> follows = Repo.get(UserFollow.class)
            .where("followingId", user.getId())
            .all();

        return follows.stream()
            .map(f -> User.get(f.getFollowerId()))
            .filter(u -> u != null)
            .map(PublicUserResponse::create)
            .collect(Collectors.toList());
    }

    @Get("/followers/{userId}")
    public List<PublicUserResponse> getFollowersByUserId(@Path("userId") String userId) {
        List<UserFollow> follows = Repo.get(UserFollow.class)
            .where("followingId", userId)
            .all();

        return follows.stream()
            .map(f -> User.get(f.getFollowerId()))
            .filter(u -> u != null)
            .map(PublicUserResponse::create)
            .collect(Collectors.toList());
    }

    @Get("/following")
    @With({"auth"})
    public List<PublicUserResponse> getFollowing(@Attrib("user") User user) {
        List<UserFollow> follows = Repo.get(UserFollow.class)
            .where("followerId", user.getId())
            .all();

        return follows.stream()
            .map(f -> User.get(f.getFollowingId()))
            .filter(u -> u != null)
            .map(PublicUserResponse::create)
            .collect(Collectors.toList());
    }

    @Get("/following/{userId}")
    public List<PublicUserResponse> getFollowingByUserId(@Path("userId") String userId) {
        List<UserFollow> follows = Repo.get(UserFollow.class)
            .where("followerId", userId)
            .all();

        return follows.stream()
            .map(f -> User.get(f.getFollowingId()))
            .filter(u -> u != null)
            .map(PublicUserResponse::create)
            .collect(Collectors.toList());
    }

    @Get("/feed")
    @With({"auth"})
    public List<ActivityResponse> getFeed(@Attrib("user") User user,
                                         @Query("limit") Integer limit) {
        if (limit == null || limit > 100) limit = 50;

        List<UserFollow> follows = Repo.get(UserFollow.class)
            .where("followerId", user.getId())
            .all();

        List<String> followingIds = follows.stream()
            .map(UserFollow::getFollowingId)
            .collect(Collectors.toList());

        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<UserActivity> activities = Repo.get(UserActivity.class)
            .whereIn("userId", followingIds.toArray())
            .order("createdAt", true)
            .limit(limit)
            .all();

        return activities.stream()
            .map(ActivityResponse::create)
            .collect(Collectors.toList());
    }

    @Get("/profile/{userId}")
    public UserProfileResponse getProfile(@Path("userId") String userId) {
        User user = User.get(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        UserProfile profile = UserProfile.getByUserId(userId);

        return UserProfileResponse.create(user, profile);
    }

    @Put("/profile")
    @With({"auth"})
    public UserProfileResponse updateProfile(@Body UpdateProfileRequest request,
                                            @Attrib("user") User user) {
        UserProfile profile = UserProfile.getByUserId(user.getId());

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
        }

        if (request.bio != null) profile.setBio(request.bio);
        if (request.website != null) profile.setWebsite(request.website);
        if (request.location != null) profile.setLocation(request.location);
        if (request.company != null) profile.setCompany(request.company);
        if (request.githubUsername != null) profile.setGithubUsername(request.githubUsername);
        if (request.twitterUsername != null) profile.setTwitterUsername(request.twitterUsername);
        profile.save();

        return UserProfileResponse.create(user, profile);
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
