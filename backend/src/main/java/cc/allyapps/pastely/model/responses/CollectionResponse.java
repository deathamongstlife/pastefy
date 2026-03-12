package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.Collection;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class CollectionResponse {
    public String id;
    public String name;
    public String description;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("is_public")
    public boolean isPublic;

    public String icon;
    public String color;

    @SerializedName("created_at")
    public Timestamp createdAt;

    @SerializedName("updated_at")
    public Timestamp updatedAt;

    @SerializedName("paste_count")
    public Integer pasteCount;

    public static CollectionResponse create(Collection collection) {
        CollectionResponse response = new CollectionResponse();
        response.id = collection.getId();
        response.name = collection.getName();
        response.description = collection.getDescription();
        response.userId = collection.getUserId();
        response.isPublic = collection.isPublic();
        response.icon = collection.getIcon();
        response.color = collection.getColor();
        response.createdAt = collection.getCreatedAt();
        response.updatedAt = collection.getUpdatedAt();
        return response;
    }
}
