package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.helper.DiffUtil;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import cc.allyapps.pastely.services.VersionControlService;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.orm.Repo;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/paste")
public class VersionControlController extends HttpController {

    @Get("/{pasteKey}/branches")
    public List<PasteBranchResponse> getBranches(@Path("pasteKey") String pasteKey) {
        Paste paste = Paste.get(pasteKey);
        if (paste == null) throw new NotFoundException();

        return Repo.get(PasteBranch.class)
            .where("pasteId", pasteKey)
            .all()
            .stream()
            .map(PasteBranchResponse::create)
            .collect(Collectors.toList());
    }

    @Post("/{pasteKey}/branches")
    @With({"auth"})
    public PasteBranchResponse createBranch(
        @Path("pasteKey") String pasteKey,
        @Body CreateBranchRequest request,
        @Attrib("user") User user
    ) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        PasteBranch branch = VersionControlService.createBranch(
            pasteKey,
            request.name,
            request.fromRevisionId,
            user
        );

        return PasteBranchResponse.create(branch);
    }

    @Get("/{pasteKey}/branches/{branchName}/history")
    public List<PasteRevisionResponse> getHistory(
        @Path("pasteKey") String pasteKey,
        @Path("branchName") String branchName
    ) {
        PasteBranch branch = Repo.get(PasteBranch.class)
            .where("pasteId", pasteKey)
            .where("name", branchName)
            .first();

        if (branch == null) throw new NotFoundException();

        return VersionControlService.getHistory(branch.getId())
            .stream()
            .map(rev -> PasteRevisionResponse.create(rev, false))
            .collect(Collectors.toList());
    }

    @Get("/{pasteKey}/revisions/{revisionId}")
    public PasteRevisionResponse getRevision(
        @Path("pasteKey") String pasteKey,
        @Path("revisionId") String revisionId,
        @Query("includeDiff") String includeDiff
    ) {
        PasteRevision revision = PasteRevision.get(revisionId);
        if (revision == null) throw new NotFoundException();

        boolean showDiff = "true".equals(includeDiff);
        return PasteRevisionResponse.create(revision, showDiff);
    }

    @Get("/{pasteKey}/revisions/{revisionId}/content")
    public ContentResponse getRevisionContent(
        @Path("pasteKey") String pasteKey,
        @Path("revisionId") String revisionId
    ) {
        PasteRevision revision = PasteRevision.get(revisionId);
        if (revision == null) throw new NotFoundException();

        String content = DiffUtil.reconstructContent(revision);

        ContentResponse response = new ContentResponse();
        response.content = content;
        return response;
    }

    @Post("/{pasteKey}/rollback/{revisionId}")
    @With({"auth"})
    public ActionResponse rollback(
        @Path("pasteKey") String pasteKey,
        @Path("revisionId") String revisionId,
        @Attrib("user") User user
    ) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        VersionControlService.rollback(paste, revisionId, user);

        return new ActionResponse(true);
    }

    @Get("/{pasteKey}/compare/{fromRevisionId}/{toRevisionId}")
    public CompareResponse compare(
        @Path("pasteKey") String pasteKey,
        @Path("fromRevisionId") String fromRevisionId,
        @Path("toRevisionId") String toRevisionId
    ) {
        PasteRevision fromRev = PasteRevision.get(fromRevisionId);
        PasteRevision toRev = PasteRevision.get(toRevisionId);

        if (fromRev == null || toRev == null) throw new NotFoundException();

        String fromContent = DiffUtil.reconstructContent(fromRev);
        String toContent = DiffUtil.reconstructContent(toRev);
        String diff = DiffUtil.generateDiff(fromContent, toContent);

        CompareResponse response = new CompareResponse();
        response.fromRevision = PasteRevisionResponse.create(fromRev, false);
        response.toRevision = PasteRevisionResponse.create(toRev, false);
        response.diff = diff;

        return response;
    }

    public static class ContentResponse {
        public String content;
    }

    public static class CompareResponse {
        public PasteRevisionResponse fromRevision;
        public PasteRevisionResponse toRevision;
        public String diff;
    }
}
