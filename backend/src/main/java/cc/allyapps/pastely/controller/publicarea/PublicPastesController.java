package cc.allyapps.pastely.controller.publicarea;

import cc.allyapps.pastely.controller.HttpController;
import cc.allyapps.pastely.helper.RequestHelper;
import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.database.algorithm.PublicPasteEngagement;
import cc.allyapps.pastely.model.queryparams.ListQueryParameters;
import cc.allyapps.pastely.model.queryparams.PasteQueryParameters;
import cc.allyapps.pastely.model.responses.paste.PasteResponse;
import cc.allyapps.pastely.services.PasteService;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/public-pastes")
@With("public-pastes-endpoint")
public class PublicPastesController extends HttpController {
    @Get
    public List<PasteResponse> getPublicPastes(Exchange exchange) {
        return PasteService.getAllPastes(exchange,
                PasteQueryParameters.from(exchange)
                .setFilter("visibility", Paste.Visibility.PUBLIC)
        );
    }

    @Get("/trending")
    public List<PasteResponse> getTrending(Exchange exchange) {
        ListQueryParameters listQueryParameters = PasteQueryParameters.from(exchange)
                .setFilter("visibility", Paste.Visibility.PUBLIC)
                .setFilter("encrypted", "false")
                .setSort("engagementScore");

        if (exchange.getQueryParameters().has("trending")) {
            listQueryParameters
                .setFilter("createdAt", new AbstractObject()
                        .set("$gt", ListQueryParameters.fromDate(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 4)))
                );
        }

        return PasteService.getAllPastes(exchange, listQueryParameters);
    }

    @Get("/latest")
    public List<PasteResponse> getLatest(Exchange exchange, @Attrib("user") User user) {
        return PasteService.getAllPastes(exchange,
                PasteQueryParameters.from(exchange)
                    .setFilter("visibility", Paste.Visibility.PUBLIC)
                    .setSort("createdAt"));
    }
}
