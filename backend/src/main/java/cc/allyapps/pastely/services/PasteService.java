package cc.allyapps.pastely.services;

import cc.allyapps.pastely.model.queryparams.ListQueryParameters;
import cc.allyapps.pastely.model.queryparams.PasteQueryParameters;
import cc.allyapps.pastely.model.queryparams.transformer.PasteQueryBuilder;
import cc.allyapps.pastely.model.responses.paste.PasteResponse;
import org.javawebstack.http.router.Exchange;

import java.util.List;

public class PasteService {
    public static List<PasteResponse> getAllPastes(Exchange exchange, ListQueryParameters params, boolean guarded) {
        if (guarded) params.guarded(exchange);
        return new PasteQueryBuilder().get(exchange, params);
    }
    public static List<PasteResponse> getAllPastes(Exchange exchange, ListQueryParameters params) {
        return getAllPastes(exchange, params, true);
    }
    public static List<PasteResponse> getAllPastes(Exchange exchange) {
        return getAllPastes(exchange, PasteQueryParameters.from(exchange));
    }
}
