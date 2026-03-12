package de.interaapps.pastefy.controller;

import com.google.gson.Gson;
import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.PasteBranch;
import de.interaapps.pastefy.model.database.PasteRevision;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * RevisionController - Version control for pastes
 * Part of the Version Control System feature
 */
@PathPrefix("/api/v2/paste/{pasteKey}/revisions")
public class RevisionController extends HttpController {

    @Get
    @With({"auth-login-required-read"})
    public List<PasteRevision> getRevisions(@Path("pasteKey") String pasteKey, Exchange exchange) {
        User user = exchange.attrib("user");
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        return Repo.get(PasteRevision.class)
                .where("pasteId", paste.getKey())
                .orderBy("revisionNumber", true)
                .get();
    }

    @Post
    @With({"auth", "auth-login-required-create"})
    public ActionResponse createRevision(
            @Path("pasteKey") String pasteKey,
            @Body("commitMessage") String commitMessage,
            @Body("diffContent") String diffContent,
            @Body("branchId") String branchId,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        PasteRevision revision = new PasteRevision();
        revision.setPasteId(paste.getKey());
        revision.setUserId(user.getId());
        revision.setCommitMessage(commitMessage);
        revision.setDiffContent(diffContent);
        revision.setParentTitle(paste.getTitle());

        if (branchId != null) {
            revision.setBranchId(branchId);
        }

        // Get the latest revision number
        PasteRevision latestRevision = Repo.get(PasteRevision.class)
                .where("pasteId", paste.getKey())
                .orderBy("revisionNumber", true)
                .first();

        revision.setRevisionNumber(latestRevision != null ? latestRevision.getRevisionNumber() + 1 : 1);

        if (latestRevision != null) {
            revision.setPreviousRevisionId(latestRevision.getId());
        }

        revision.save();

        return ActionResponse.success();
    }

    @Get("/{revisionId}")
    @With({"auth-login-required-read"})
    public PasteRevision getRevision(
            @Path("pasteKey") String pasteKey,
            @Path("revisionId") String revisionId,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteRevision revision = Repo.get(PasteRevision.class)
                .where("id", revisionId)
                .where("pasteId", paste.getKey())
                .first();

        if (revision == null) throw new NotFoundException();

        return revision;
    }

    @Post("/{revisionId}/rollback")
    @With({"auth", "auth-login-required-create"})
    public ActionResponse rollback(
            @Path("pasteKey") String pasteKey,
            @Path("revisionId") String revisionId,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        PasteRevision revision = Repo.get(PasteRevision.class)
                .where("id", revisionId)
                .where("pasteId", paste.getKey())
                .first();

        if (revision == null) throw new NotFoundException();

        // Create a new revision representing the rollback
        PasteRevision rollbackRevision = new PasteRevision();
        rollbackRevision.setPasteId(paste.getKey());
        rollbackRevision.setUserId(user.getId());
        rollbackRevision.setCommitMessage("Rollback to revision " + revision.getRevisionNumber());
        rollbackRevision.setBranchId(revision.getBranchId());

        PasteRevision latestRevision = Repo.get(PasteRevision.class)
                .where("pasteId", paste.getKey())
                .orderBy("revisionNumber", true)
                .first();

        rollbackRevision.setRevisionNumber(latestRevision != null ? latestRevision.getRevisionNumber() + 1 : 1);
        rollbackRevision.setPreviousRevisionId(latestRevision != null ? latestRevision.getId() : null);

        rollbackRevision.save();

        return ActionResponse.success();
    }

    @Get("/branches")
    @With({"auth-login-required-read"})
    public List<PasteBranch> getBranches(@Path("pasteKey") String pasteKey, Exchange exchange) {
        User user = exchange.attrib("user");
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        return Repo.get(PasteBranch.class)
                .where("pasteId", paste.getKey())
                .get();
    }

    @Post("/branches")
    @With({"auth", "auth-login-required-create"})
    public PasteBranch createBranch(
            @Path("pasteKey") String pasteKey,
            @Body("branchName") String branchName,
            @Body("baseRevisionId") String baseRevisionId,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        PasteBranch branch = new PasteBranch();
        branch.setPasteId(paste.getKey());
        branch.setUserId(user.getId());
        branch.setBranchName(branchName);
        branch.setBaseRevisionId(baseRevisionId);
        branch.setDefault(false);
        branch.save();

        return branch;
    }
}
