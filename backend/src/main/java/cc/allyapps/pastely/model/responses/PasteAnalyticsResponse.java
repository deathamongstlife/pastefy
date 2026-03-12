package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.PasteView;
import cc.allyapps.pastely.model.database.ViewAnalytics;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PasteAnalyticsResponse {
    @SerializedName("paste_id")
    public String pasteId;

    @SerializedName("total_views")
    public Integer totalViews;

    @SerializedName("unique_views")
    public Integer uniqueViews;

    @SerializedName("recent_views")
    public List<ViewData> recentViews;

    @SerializedName("views_by_date")
    public List<DateViewData> viewsByDate;

    @SerializedName("top_countries")
    public Map<String, Integer> topCountries;

    @SerializedName("top_referers")
    public Map<String, Integer> topReferers;

    public static PasteAnalyticsResponse create(Paste paste, int totalViews, 
                                               List<PasteView> recentViews,
                                               List<ViewAnalytics> analytics) {
        PasteAnalyticsResponse response = new PasteAnalyticsResponse();
        response.pasteId = paste.getKey();
        response.totalViews = totalViews;

        response.recentViews = recentViews.stream()
            .map(ViewData::create)
            .collect(Collectors.toList());

        response.viewsByDate = analytics.stream()
            .map(DateViewData::create)
            .collect(Collectors.toList());

        response.topCountries = recentViews.stream()
            .filter(v -> v.getCountry() != null && !v.getCountry().isEmpty())
            .collect(Collectors.groupingBy(
                PasteView::getCountry,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        response.topReferers = recentViews.stream()
            .filter(v -> v.getReferer() != null && !v.getReferer().isEmpty())
            .collect(Collectors.groupingBy(
                PasteView::getReferer,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        return response;
    }

    public static class ViewData {
        @SerializedName("ip_address")
        public String ipAddress;

        @SerializedName("user_agent")
        public String userAgent;

        public String referer;
        public String country;
        public String city;

        @SerializedName("viewed_at")
        public java.sql.Timestamp viewedAt;

        public static ViewData create(PasteView view) {
            ViewData data = new ViewData();
            data.ipAddress = view.getIpAddress();
            data.userAgent = view.getUserAgent();
            data.referer = view.getReferer();
            data.country = view.getCountry();
            data.city = view.getCity();
            data.viewedAt = view.getViewedAt();
            return data;
        }
    }

    public static class DateViewData {
        public String date;

        @SerializedName("view_count")
        public Integer viewCount;

        @SerializedName("unique_view_count")
        public Integer uniqueViewCount;

        public static DateViewData create(ViewAnalytics analytics) {
            DateViewData data = new DateViewData();
            data.date = analytics.getDate();
            data.viewCount = analytics.getViewCount();
            data.uniqueViewCount = analytics.getUniqueViewCount();
            return data;
        }
    }
}
