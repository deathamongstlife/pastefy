package de.interaapps.pastely.controller;

import de.interaapps.pastely.exceptions.AuthenticationException;
import de.interaapps.pastely.exceptions.NotFoundException;
import de.interaapps.pastely.model.database.*;
import de.interaapps.pastely.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.http.router.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CollectionController - Curated paste collections
 * Part of the Enhanced Organization feature
 */
@PathPrefix("/api/v2/collections")
public class CollectionController extends HttpController {

    @Post
    @With({"auth"})
    public PasteCollection createCollection(
            @Body("name") String name,
            @Body("description") String description,
            @Body("isPublic") Boolean isPublic,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteCollection collection = new PasteCollection();
        collection.setUserId(user.getId());
        collection.setName(name);
        collection.setDescription(description);
        collection.setPublic(isPublic != null && isPublic);
        collection.save();

        return collection;
    }

    @Get("/{collectionId}")
    public Map<String, Object> getCollection(@Path("collectionId") String collectionId, Exchange exchange) {
        PasteCollection collection = Repo.get(PasteCollection.class)
                .where("id", collectionId)
                .first();

        if (collection == null) throw new NotFoundException();

        User user = exchange.attrib("user");
        if (!collection.isPublic() && (user == null || !Objects.equals(collection.getUserId(), user.getId()))) {
            throw new AuthenticationException();
        }

        List<CollectionPaste> collectionPastes = Repo.get(CollectionPaste.class)
                .where("collectionId", collectionId)
                .orderBy("sortOrder", false)
                .get();

        List<Paste> pastes = collectionPastes.stream()
                .map(cp -> Paste.get(cp.getPasteId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("collection", collection);
        result.put("pastes", pastes);
        return result;
    }

    @Put("/{collectionId}")
    @With({"auth"})
    public PasteCollection updateCollection(
            @Path("collectionId") String collectionId,
            @Body("name") String name,
            @Body("description") String description,
            @Body("isPublic") Boolean isPublic,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteCollection collection = Repo.get(PasteCollection.class)
                .where("id", collectionId)
                .first();

        if (collection == null) throw new NotFoundException();

        if (!Objects.equals(collection.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (name != null) collection.setName(name);
        if (description != null) collection.setDescription(description);
        if (isPublic != null) collection.setPublic(isPublic);

        collection.setUpdatedAt(Timestamp.from(Instant.now()));
        collection.save();

        return collection;
    }

    @Delete("/{collectionId}")
    @With({"auth"})
    public ActionResponse deleteCollection(@Path("collectionId") String collectionId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteCollection collection = Repo.get(PasteCollection.class)
                .where("id", collectionId)
                .first();

        if (collection == null) throw new NotFoundException();

        if (!Objects.equals(collection.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        // Delete all collection pastes
        Repo.get(CollectionPaste.class)
                .where("collectionId", collectionId)
                .delete();

        collection.delete();

        return ActionResponse.success();
    }

    @Post("/{collectionId}/pastes")
    @With({"auth"})
    public ActionResponse addPasteToCollection(
            @Path("collectionId") String collectionId,
            @Body("pasteKey") String pasteKey,
            @Body("sortOrder") Integer sortOrder,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteCollection collection = Repo.get(PasteCollection.class)
                .where("id", collectionId)
                .first();

        if (collection == null) throw new NotFoundException();

        if (!Objects.equals(collection.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        Paste paste = Paste.get(pasteKey);
        if (paste == null) throw new NotFoundException();

        // Check if already in collection
        CollectionPaste existing = Repo.get(CollectionPaste.class)
                .where("collectionId", collectionId)
                .where("pasteId", pasteKey)
                .first();

        if (existing != null) {
            return ActionResponse.error("Paste already in collection");
        }

        CollectionPaste collectionPaste = new CollectionPaste();
        collectionPaste.setCollectionId(collectionId);
        collectionPaste.setPasteId(pasteKey);
        collectionPaste.setSortOrder(sortOrder != null ? sortOrder : 0);
        collectionPaste.save();

        collection.setUpdatedAt(Timestamp.from(Instant.now()));
        collection.save();

        return ActionResponse.success();
    }

    @Delete("/{collectionId}/pastes/{pasteKey}")
    @With({"auth"})
    public ActionResponse removePasteFromCollection(
            @Path("collectionId") String collectionId,
            @Path("pasteKey") String pasteKey,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        PasteCollection collection = Repo.get(PasteCollection.class)
                .where("id", collectionId)
                .first();

        if (collection == null) throw new NotFoundException();

        if (!Objects.equals(collection.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        Repo.get(CollectionPaste.class)
                .where("collectionId", collectionId)
                .where("pasteId", pasteKey)
                .delete();

        collection.setUpdatedAt(Timestamp.from(Instant.now()));
        collection.save();

        return ActionResponse.success();
    }

    @Get("/user/{userId}")
    public List<PasteCollection> getUserCollections(@Path("userId") String userId, Exchange exchange) {
        User currentUser = exchange.attrib("user");
        boolean isOwner = currentUser != null && Objects.equals(currentUser.getId(), userId);

        if (isOwner) {
            // Owner can see all their collections
            return Repo.get(PasteCollection.class)
                    .where("userId", userId)
                    .orderBy("updatedAt", true)
                    .get();
        } else {
            // Others can only see public collections
            return Repo.get(PasteCollection.class)
                    .where("userId", userId)
                    .where("isPublic", true)
                    .orderBy("updatedAt", true)
                    .get();
        }
    }
}
