package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * CollectionPaste - Many-to-many relationship between collections and pastes
 * Part of the Enhanced Organization feature
 */
@Table("collection_pastes")
public class CollectionPaste extends Model {

    @Column(size = 8)
    private String id;

    @Column(size = 8)
    private String collectionId;

    @Column(size = 8)
    private String pasteId;

    @Column
    private Integer sortOrder;

    @Column
    private Timestamp addedAt;

    public CollectionPaste() {
        this.id = RandomUtil.string(8);
        this.addedAt = Timestamp.from(Instant.now());
    }

    public String getId() {
        return id;
    }

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
}
