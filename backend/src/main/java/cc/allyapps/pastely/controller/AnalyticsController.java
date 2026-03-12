package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.Repo;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/analytics")
public class AnalyticsController extends HttpController {

    @Post("/track/{pasteKey}")
    public ActionResponse trackView(@Path("pasteKey") String pasteKey,
                                   Exchange exchange) {
        Paste paste = Paste.get(pasteKey);
        if (paste == null) {
            throw new NotFoundException("Paste not found");
        }

        PasteView view = new PasteView();
        view.setPasteId(pasteKey);
        view.setIpAddress(exchange.getClientIP());
        view.setUserAgent(exchange.header("User-Agent"));
        view.setReferer(exchange.header("Referer"));

        User user = exchange.attrib("user");
        if (user != null) {
            view.setUserId(user.getId());
        }

        view.save();

        Pastely.getInstance().executeAsync(() -> updateAnalytics(pasteKey));

        return new ActionResponse(true);
    }

    @Get("/paste/{pasteKey}")
    @With({"auth"})
    public PasteAnalyticsResponse getAnalytics(@Path("pasteKey") String pasteKey,
                                              @Attrib("user") User user) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!paste.getUserId().equals(user.getId())) {
            throw new PermissionsDeniedException();
        }

        int totalViews = (int) Repo.get(PasteView.class)
            .where("pasteId", pasteKey)
            .count();

        List<PasteView> recentViews = Repo.get(PasteView.class)
            .where("pasteId", pasteKey)
            .order("viewedAt", true)
            .limit(100)
            .all();

        List<ViewAnalytics> analytics = Repo.get(ViewAnalytics.class)
            .where("pasteId", pasteKey)
            .order("date", true)
            .limit(30)
            .all();

        return PasteAnalyticsResponse.create(paste, totalViews, recentViews, analytics);
    }

    @Get("/trending")
    public List<PasteResponse> getTrending(@Query("limit") Integer limit) {
        if (limit == null || limit > 50) limit = 20;

        List<PublicPasteEngagement> trending = Repo.get(PublicPasteEngagement.class)
            .order("score", true)
            .limit(limit)
            .all();

        return trending.stream()
            .map(e -> Paste.get(e.getPasteId()))
            .filter(p -> p != null && p.isPublic())
            .map(p -> PasteResponse.create(p, null))
            .collect(Collectors.toList());
    }

    @Get("/user/{userId}/stats")
    public UserStatsResponse getUserStats(@Path("userId") String userId) {
        User user = User.get(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        int pasteCount = (int) Repo.get(Paste.class)
            .where("userId", userId)
            .count();

        int followerCount = (int) Repo.get(UserFollow.class)
            .where("followingId", userId)
            .count();

        int followingCount = (int) Repo.get(UserFollow.class)
            .where("followerId", userId)
            .count();

        UserStatsResponse stats = new UserStatsResponse();
        stats.userId = userId;
        stats.pasteCount = pasteCount;
        stats.followerCount = followerCount;
        stats.followingCount = followingCount;

        return stats;
    }

    private void updateAnalytics(String pasteKey) {
        String today = LocalDate.now().toString();

        ViewAnalytics analytics = ViewAnalytics.getByPasteAndDate(pasteKey, today);

        if (analytics == null) {
            analytics = new ViewAnalytics();
            analytics.setPasteId(pasteKey);
            analytics.setDate(today);
        }

        analytics.setViewCount(analytics.getViewCount() + 1);

        List<PasteView> todayViews = Repo.get(PasteView.class)
            .where("pasteId", pasteKey)
            .raw("DATE(viewedAt) = ?", today)
            .all();

        long uniqueCount = todayViews.stream()
            .map(PasteView::getIpAddress)
            .distinct()
            .count();

        analytics.setUniqueViewCount((int) uniqueCount);
        analytics.save();
    }
}
