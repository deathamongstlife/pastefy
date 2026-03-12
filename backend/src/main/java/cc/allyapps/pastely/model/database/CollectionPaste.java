package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

@Table("collection_pastes")
public class CollectionPaste extends Model {
    @Column(size = 8)
    private String collectionId;

    @Column(size = 8)
    private String pasteId;

    @Column
    private Integer sortOrder = 0;

    @Column
    private Timestamp addedAt;

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getPasteId() {
        return pasteId;
    }

    public void setPasteId(String pasteId) {
        this.pasteId = pasteId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    @Override
    public void save() {
        if (addedAt == null) {
            addedAt = Timestamp.from(Instant.now());
        }
        super.save();
    }
}
