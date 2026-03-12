package de.interaapps.pastely.model.elastic;

import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.json.JsonData;
import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.model.database.Paste;
import de.interaapps.pastely.model.database.User;

import java.io.IOException;

public class ElasticStars {
    public static void addStarCount(Paste paste, User user) {
        if (!Pastely.getInstance().isElasticsearchEnabled()) return;

        UpdateRequest<Object, Object> objectObjectUpdateRequest = UpdateRequest.of(q -> q
                .index("pastely_pastes")
                .id(String.valueOf(paste.getId()))
                .script(s ->
                        s.source(sr -> sr
                            .scriptString("ctx._source.starCount = params.starCount; if (ctx._source.starredBy == null) { ctx._source.starredBy = []; } ctx._source.starredBy.add(params.userId);")
                        )
                        .params("userId", JsonData.of(user.id))
                        .params("starCount", JsonData.of(paste.getStarCounts()))
                )
        );

        try {
            Pastely.getInstance().getElasticsearchClient()
                    .update(objectObjectUpdateRequest, ElasticPaste.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeStarCount(Paste paste, User user) {
        if (!Pastely.getInstance().isElasticsearchEnabled()) return;
        UpdateRequest<Object, Object> objectObjectUpdateRequest = UpdateRequest.of(q -> q
                .index("pastely_pastes")
                .id(String.valueOf(paste.getId()))
                .script(s ->
                        s.source(sr -> sr
                                .scriptString("if (ctx._source.starredBy != null) { " +
                                        "int idx = ctx._source.starredBy.indexOf(params.userId);" +
                                        "if (idx != -1) {" +
                                        "    ctx._source.starredBy.remove(idx);" +
                                        "}" +
                                        "} " +
                                        "ctx._source.starCount = params.starCount;")
                        )
                        .params("userId", JsonData.of(user.id))
                        .params("starCount", JsonData.of(paste.getStarCounts()))
                )
        );

        try {
            Pastely.getInstance().getElasticsearchClient()
                    .update(objectObjectUpdateRequest, ElasticPaste.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
