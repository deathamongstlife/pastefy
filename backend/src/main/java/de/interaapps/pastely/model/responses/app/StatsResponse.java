package de.interaapps.pastely.model.responses.app;

import com.google.gson.annotations.SerializedName;
import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.model.database.Folder;
import de.interaapps.pastely.model.database.Paste;
import de.interaapps.pastely.model.database.User;
import de.interaapps.pastely.model.database.algorithm.TagListing;
import org.javawebstack.orm.Repo;

import java.io.IOException;

public class StatsResponse {

    private int createdPastes;
    private int loggedInPastes;
    private int userCount;
    private int tagCount;
    private int folderCount;

    private Long indexedPastes;

    @SerializedName("s3_pastes")
    private Integer s3Pastes;
    private Integer databasePastes;

    public static StatsResponse create() {
        Pastefy instance = Pastely.getInstance();
        StatsResponse response = new StatsResponse();

        response.createdPastes = Repo.get(Paste.class).count();
        response.loggedInPastes = Repo.get(Paste.class).query().whereNotNull("userId").count();
        response.userCount = Repo.get(User.class).query().count();
        response.tagCount = Repo.get(TagListing.class).query().count();
        response.folderCount = Repo.get(Folder.class).query().count();

        response.s3Pastes = Repo.get(Paste.class).query().where("storageType", Paste.StorageType.S3).count();

        if (instance.isElasticsearchEnabled()) {
            try {
                response.indexedPastes = instance.getElasticsearchClient()
                        .count(r -> r.index("pastely_pastes"))
                        .count();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }
}
