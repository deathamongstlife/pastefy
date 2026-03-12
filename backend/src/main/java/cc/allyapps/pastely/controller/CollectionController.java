package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.orm.Repo;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/collection")
public class CollectionController extends HttpController {

    @Get
    @With({"auth"})
    public List<CollectionResponse> list(@Attrib("user") User user) {
        List<Collection> collections = Repo.get(Collection.class)
            .where("userId", user.getId())
            .order("createdAt", true)
            .all();

        return collections.stream()
            .map(CollectionResponse::create)
            .collect(Collectors.toList());
    }

    @Post
    @With({"auth", "rate-limiter"})
    public CollectionResponse create(@Body CreateCollectionRequest request,
                                    @Attrib("user") User user) {
        Collection collection = new Collection();
        collection.setName(request.name);
        collection.setDescription(request.description);
        collection.setUserId(user.getId());
        collection.setPublic(request.isPublic);
        collection.setIcon(request.icon);
        collection.setColor(request.color);
        collection.save();

        return CollectionResponse.create(collection);
    }

    @Get("/{id}")
    @With({"auth-login-required-read"})
    public CollectionResponse get(@Path("id") String id,
                                 @Attrib("user") User user) {
        Collection collection = Collection.get(id);

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        if (!collection.isPublic() && (user == null || !collection.getUserId().equals(user.getId()))) {
            throw new PermissionsDeniedException();
        }

        return CollectionResponse.create(collection);
    }

    @Put("/{id}")
    @With({"auth"})
    public CollectionResponse update(@Path("id") String id,
                                    @Body UpdateCollectionRequest request,
                                    @Attrib("user") User user) {
        Collection collection = Repo.get(Collection.class)
            .where("id", id)
            .where("userId", user.getId())
            .first();

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        if (request.name != null) collection.setName(request.name);
        if (request.description != null) collection.setDescription(request.description);
        if (request.isPublic != null) collection.setPublic(request.isPublic);
        if (request.icon != null) collection.setIcon(request.icon);
        if (request.color != null) collection.setColor(request.color);
        collection.save();

        return CollectionResponse.create(collection);
    }

    @Delete("/{id}")
    @With({"auth"})
    public ActionResponse delete(@Path("id") String id,
                                @Attrib("user") User user) {
        Collection collection = Repo.get(Collection.class)
            .where("id", id)
            .where("userId", user.getId())
            .first();

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        collection.delete();
        return new ActionResponse(true);
    }

    @Post("/{id}/paste/{pasteKey}")
    @With({"auth"})
    public ActionResponse addPaste(@Path("id") String id,
                                  @Path("pasteKey") String pasteKey,
                                  @Attrib("user") User user) {
        Collection collection = Repo.get(Collection.class)
            .where("id", id)
            .where("userId", user.getId())
            .first();

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        Paste paste = Paste.get(pasteKey);
        if (paste == null) {
            throw new NotFoundException("Paste not found");
        }

        CollectionPaste existing = Repo.get(CollectionPaste.class)
            .where("collectionId", id)
            .where("pasteId", pasteKey)
            .first();

        if (existing != null) {
            throw new HTTPException(400, "Paste already in collection");
        }

        CollectionPaste cp = new CollectionPaste();
        cp.setCollectionId(id);
        cp.setPasteId(pasteKey);
        cp.save();

        return new ActionResponse(true);
    }

    @Delete("/{id}/paste/{pasteKey}")
    @With({"auth"})
    public ActionResponse removePaste(@Path("id") String id,
                                     @Path("pasteKey") String pasteKey,
                                     @Attrib("user") User user) {
        Collection collection = Repo.get(Collection.class)
            .where("id", id)
            .where("userId", user.getId())
            .first();

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        Repo.get(CollectionPaste.class)
            .where("collectionId", id)
            .where("pasteId", pasteKey)
            .delete();

        return new ActionResponse(true);
    }

    @Get("/{id}/pastes")
    @With({"auth-login-required-read"})
    public List<PasteResponse> getPastes(@Path("id") String id,
                                        @Attrib("user") User user) {
        Collection collection = Collection.get(id);

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        if (!collection.isPublic() && (user == null || !collection.getUserId().equals(user.getId()))) {
            throw new PermissionsDeniedException();
        }

        List<CollectionPaste> cps = Repo.get(CollectionPaste.class)
            .where("collectionId", id)
            .order("sortOrder", false)
            .all();

        return cps.stream()
            .map(cp -> Paste.get(cp.getPasteId()))
            .filter(p -> p != null)
            .map(p -> PasteResponse.create(p, user))
            .collect(Collectors.toList());
    }

    @Put("/{id}/reorder")
    @With({"auth"})
    public ActionResponse reorder(@Path("id") String id,
                                 @Body List<String> pasteIds,
                                 @Attrib("user") User user) {
        Collection collection = Repo.get(Collection.class)
            .where("id", id)
            .where("userId", user.getId())
            .first();

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        for (int i = 0; i < pasteIds.size(); i++) {
            CollectionPaste cp = Repo.get(CollectionPaste.class)
                .where("collectionId", id)
                .where("pasteId", pasteIds.get(i))
                .first();

            if (cp != null) {
                cp.setSortOrder(i);
                cp.save();
            }
        }

        return new ActionResponse(true);
    }
}
