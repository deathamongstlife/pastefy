package de.interaapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * ViewAnalytics - Aggregated analytics for paste views
 * Part of the Analytics & Tracking feature
 */
@Table("view_analytics")
public class ViewAnalytics extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String pasteId;

    @Column
    private Integer totalViews;

    @Column
    private Integer uniqueViews;

    @Column
    private Integer todayViews;

    @Column
    private Integer weekViews;

    @Column
    private Integer monthViews;

    @Column
    private Double averageTimeSpent;

    @Column
    private Double trendingScore; // Calculated score for trending

    @Column
    private Timestamp lastViewedAt;

    @Column
    private Timestamp lastCalculatedAt;

    public ViewAnalytics() {
        this.id = RandomUtil.string(8);
        this.totalViews = 0;
        this.uniqueViews = 0;
        this.todayViews = 0;
        this.weekViews = 0;
        this.monthViews = 0;
        this.averageTimeSpent = 0.0;
        this.trendingScore = 0.0;
        this.lastCalculatedAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public Integer getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Integer totalViews) {
        this.totalViews = totalViews;
    }

    public Integer getUniqueViews() {
        return uniqueViews;
    }

    public void setUniqueViews(Integer uniqueViews) {
        this.uniqueViews = uniqueViews;
    }

    public Integer getTodayViews() {
        return todayViews;
    }

    public void setTodayViews(Integer todayViews) {
        this.todayViews = todayViews;
    }

    public Integer getWeekViews() {
        return weekViews;
    }

    public void setWeekViews(Integer weekViews) {
        this.weekViews = weekViews;
    }

    public Integer getMonthViews() {
        return monthViews;
    }

    public void setMonthViews(Integer monthViews) {
        this.monthViews = monthViews;
    }

    public Double getAverageTimeSpent() {
        return averageTimeSpent;
    }

    public void setAverageTimeSpent(Double averageTimeSpent) {
        this.averageTimeSpent = averageTimeSpent;
    }

    public Double getTrendingScore() {
        return trendingScore;
    }

    public void setTrendingScore(Double trendingScore) {
        this.trendingScore = trendingScore;
    }

    public Timestamp getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(Timestamp lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }

    public Timestamp getLastCalculatedAt() {
        return lastCalculatedAt;
    }

    public void setLastCalculatedAt(Timestamp lastCalculatedAt) {
        this.lastCalculatedAt = lastCalculatedAt;
    }
}
