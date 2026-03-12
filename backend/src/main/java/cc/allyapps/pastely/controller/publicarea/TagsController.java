package cc.allyapps.pastely.controller.publicarea;

import cc.allyapps.pastely.controller.HttpController;
import cc.allyapps.pastely.helper.RequestHelper;
import cc.allyapps.pastely.model.database.algorithm.TagListing;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@With("public-pastes-endpoint")
@PathPrefix("/api/v2/public/tags")
public class TagsController extends HttpController {
    @Get
    public List<TagListing> getTags(Exchange exchange) {
        Query<TagListing> query = Repo.get(TagListing.class).query();

        query.order("pasteCount", true);

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.all();
    }

    @Get("/{tag}")
    public TagListing getUser(@Path("tag") String tag) {
        return TagListing.getOrCreate(tag);
    }
}
