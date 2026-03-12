package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Table("view_analytics")
public class ViewAnalytics extends Model {
    @Column(size = 8)
    private String pasteId;

    @Column(size = 10)
    private String date;

    @Column
    private Integer viewCount = 0;

    @Column
    private Integer uniqueViewCount = 0;

    @Column
    private Timestamp updatedAt;

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getUniqueViewCount() {
        return uniqueViewCount;
    }

    public void setUniqueViewCount(Integer uniqueViewCount) {
        this.uniqueViewCount = uniqueViewCount;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public Paste getPaste() {
        return Paste.get(pasteId);
    }

    public static ViewAnalytics getByPasteAndDate(String pasteId, String date) {
        return Repo.get(ViewAnalytics.class)
            .where("pasteId", pasteId)
            .where("date", date)
            .first();
    }

    @Override
    public void save() {
        updatedAt = Timestamp.from(Instant.now());
        super.save();
    }
}
