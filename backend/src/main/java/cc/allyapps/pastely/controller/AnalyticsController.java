package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.*;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.params.Query;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsController - Analytics and tracking for pastes
 * Part of the Analytics & Tracking feature
 */
@PathPrefix("/api/v2/analytics")
public class AnalyticsController extends HttpController {

    @Post("/paste/{pasteKey}/view")
    public void trackView(
            @Path("pasteKey") String pasteKey,
            @Body("timeSpent") Integer timeSpent,
            Exchange exchange
    ) {
        Paste paste = Paste.get(pasteKey);
        if (paste == null) return;

        PasteView view = new PasteView();
        view.setPasteId(paste.getKey());

        User user = exchange.attrib("user");
        if (user != null) {
            view.setUserId(user.getId());
        }

        String clientIp = exchange.header("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = exchange.ip();
        }
        view.setIpAddress(clientIp);

        view.setUserAgent(exchange.header("User-Agent"));
        view.setReferer(exchange.header("Referer"));
        view.setTimeSpent(timeSpent);

        view.save();

        // Update aggregated analytics asynchronously
        Pastely.getInstance().executeAsync(() -> updateAnalytics(paste.getKey()));
    }

    @Get("/paste/{pasteKey}")
    @With({"auth"})
    public ViewAnalytics getPasteAnalytics(@Path("pasteKey") String pasteKey, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        ViewAnalytics analytics = Repo.get(ViewAnalytics.class)
                .where("pasteId", paste.getKey())
                .first();

        if (analytics == null) {
            analytics = new ViewAnalytics();
            analytics.setPasteId(paste.getKey());
        }

        return analytics;
    }

    @Get("/trending")
    public List<Map<String, Object>> getTrending(@Query("limit") Integer limit) {
        if (limit == null || limit > 50) {
            limit = 50;
        }

        List<ViewAnalytics> trending = Repo.get(ViewAnalytics.class)
                .orderBy("trendingScore", true)
                .limit(limit)
                .get();

        return trending.stream().map(analytics -> {
            Paste paste = Paste.get(analytics.getPasteId());
            if (paste == null || !paste.isPublic()) return null;

            Map<String, Object> result = new HashMap<>();
            result.put("paste", paste);
            result.put("analytics", analytics);
            return result;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Get("/paste/{pasteKey}/views/timeline")
    @With({"auth"})
    public List<Map<String, Object>> getViewsTimeline(
            @Path("pasteKey") String pasteKey,
            @Query("days") Integer days,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (days == null || days > 90) {
            days = 30;
        }

        Timestamp startDate = Timestamp.from(Instant.now().minus(days, ChronoUnit.DAYS));

        List<PasteView> views = Repo.get(PasteView.class)
                .where("pasteId", paste.getKey())
                .where("viewedAt", ">=", startDate)
                .orderBy("viewedAt", false)
                .get();

        // Group by day
        Map<String, Long> viewsByDay = views.stream()
                .collect(Collectors.groupingBy(
                        view -> view.getViewedAt().toLocalDateTime().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return viewsByDay.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("date", entry.getKey());
                    result.put("views", entry.getValue());
                    return result;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .collect(Collectors.toList());
    }

    @Get("/paste/{pasteKey}/views/geographic")
    @With({"auth"})
    public List<Map<String, Object>> getGeographicDistribution(
            @Path("pasteKey") String pasteKey,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        List<PasteView> views = Repo.get(PasteView.class)
                .where("pasteId", paste.getKey())
                .get();

        // Group by country
        Map<String, Long> viewsByCountry = views.stream()
                .filter(view -> view.getCountry() != null)
                .collect(Collectors.groupingBy(
                        PasteView::getCountry,
                        Collectors.counting()
                ));

        return viewsByCountry.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("country", entry.getKey());
                    result.put("views", entry.getValue());
                    return result;
                })
                .sorted(Comparator.comparing(m -> -(Long) m.get("views")))
                .collect(Collectors.toList());
    }

    private void updateAnalytics(String pasteKey) {
        ViewAnalytics analytics = Repo.get(ViewAnalytics.class)
                .where("pasteId", pasteKey)
                .first();

        if (analytics == null) {
            analytics = new ViewAnalytics();
            analytics.setPasteId(pasteKey);
        }

        List<PasteView> allViews = Repo.get(PasteView.class)
                .where("pasteId", pasteKey)
                .get();

        analytics.setTotalViews(allViews.size());

        // Unique views (by IP)
        Set<String> uniqueIps = allViews.stream()
                .map(PasteView::getIpAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        analytics.setUniqueViews(uniqueIps.size());

        // Time-based views
        Instant now = Instant.now();
        Timestamp today = Timestamp.from(now.truncatedTo(ChronoUnit.DAYS));
        Timestamp weekAgo = Timestamp.from(now.minus(7, ChronoUnit.DAYS));
        Timestamp monthAgo = Timestamp.from(now.minus(30, ChronoUnit.DAYS));

        analytics.setTodayViews((int) allViews.stream()
                .filter(v -> v.getViewedAt().after(today))
                .count());

        analytics.setWeekViews((int) allViews.stream()
                .filter(v -> v.getViewedAt().after(weekAgo))
                .count());

        analytics.setMonthViews((int) allViews.stream()
                .filter(v -> v.getViewedAt().after(monthAgo))
                .count());

        // Average time spent
        OptionalDouble avgTime = allViews.stream()
                .filter(v -> v.getTimeSpent() != null && v.getTimeSpent() > 0)
                .mapToInt(PasteView::getTimeSpent)
                .average();

        analytics.setAverageTimeSpent(avgTime.isPresent() ? avgTime.getAsDouble() : 0.0);

        // Calculate trending score (recent views weighted more heavily)
        double trendingScore = allViews.stream()
                .mapToDouble(view -> {
                    long hoursAgo = ChronoUnit.HOURS.between(view.getViewedAt().toInstant(), now);
                    double decay = Math.exp(-hoursAgo / 168.0); // Decay over a week
                    return decay;
                })
                .sum();

        analytics.setTrendingScore(trendingScore);

        if (!allViews.isEmpty()) {
            analytics.setLastViewedAt(allViews.get(allViews.size() - 1).getViewedAt());
        }

        analytics.setLastCalculatedAt(Timestamp.from(Instant.now()));
        analytics.save();
    }
}
