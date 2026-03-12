package cc.allyapps.pastely.services;

import cc.allyapps.pastely.helper.DiffUtil;
import cc.allyapps.pastely.model.database.*;
import org.javawebstack.orm.Repo;
import java.util.List;

public class VersionControlService {

    /**
     * Create initial revision for a new paste
     */
    public static PasteRevision createInitialRevision(Paste paste, User author) {
        // Create default branch
        PasteBranch defaultBranch = new PasteBranch();
        defaultBranch.setPasteId(paste.getKey());
        defaultBranch.setName("main");
        defaultBranch.setIsDefault(true);
        defaultBranch.setCreatedBy(author.getId());
        defaultBranch.save();

        // Create initial revision (full content as "diff")
        PasteRevision revision = new PasteRevision();
        revision.setPasteId(paste.getKey());
        revision.setBranchId(defaultBranch.getId());
        revision.setParentRevisionId(null);
        revision.setRevisionNumber(1);
        revision.setDiff(paste.getContent()); // First revision stores full content
        revision.setCommitMessage("Initial commit");
        revision.setAuthorId(author.getId());
        revision.save();

        // Update branch head
        defaultBranch.setHeadRevisionId(revision.getId());
        defaultBranch.save();

        return revision;
    }

    /**
     * Create new revision when paste is updated
     */
    public static PasteRevision createRevision(
        Paste paste,
        String branchName,
        String commitMessage,
        User author
    ) {
        // Get or create branch
        PasteBranch branch = Repo.get(PasteBranch.class)
            .where("pasteId", paste.getKey())
            .where("name", branchName)
            .first();

        if (branch == null) {
            throw new RuntimeException("Branch not found: " + branchName);
        }

        // Get current head revision
        PasteRevision headRevision = branch.getHeadRevision();
        String oldContent = headRevision != null ?
            DiffUtil.reconstructContent(headRevision) : "";

        // Generate diff
        String diff = DiffUtil.generateDiff(oldContent, paste.getContent());

        // Create new revision
        PasteRevision newRevision = new PasteRevision();
        newRevision.setPasteId(paste.getKey());
        newRevision.setBranchId(branch.getId());
        newRevision.setParentRevisionId(headRevision != null ? headRevision.getId() : null);
        newRevision.setRevisionNumber(headRevision != null ? headRevision.getRevisionNumber() + 1 : 1);
        newRevision.setDiff(diff);
        newRevision.setCommitMessage(commitMessage);
        newRevision.setAuthorId(author.getId());
        newRevision.save();

        // Update branch head
        branch.setHeadRevisionId(newRevision.getId());
        branch.save();

        return newRevision;
    }

    /**
     * Create new branch from revision
     */
    public static PasteBranch createBranch(
        String pasteId,
        String branchName,
        String fromRevisionId,
        User creator
    ) {
        PasteBranch branch = new PasteBranch();
        branch.setPasteId(pasteId);
        branch.setName(branchName);
        branch.setHeadRevisionId(fromRevisionId);
        branch.setIsDefault(false);
        branch.setCreatedBy(creator.getId());
        branch.save();

        return branch;
    }

    /**
     * Get revision history for a branch
     */
    public static List<PasteRevision> getHistory(String branchId) {
        PasteBranch branch = PasteBranch.get(branchId);
        if (branch == null) return List.of();

        return Repo.get(PasteRevision.class)
            .where("branchId", branchId)
            .order("revisionNumber", true) // DESC
            .all();
    }

    /**
     * Rollback paste to specific revision
     */
    public static void rollback(Paste paste, String revisionId, User author) {
        PasteRevision targetRevision = PasteRevision.get(revisionId);
        if (targetRevision == null) {
            throw new RuntimeException("Revision not found");
        }

        // Reconstruct content at that revision
        String content = DiffUtil.reconstructContent(targetRevision);

        // Update paste
        paste.setContent(content);
        paste.save();

        // Create new revision marking the rollback
        PasteBranch branch = targetRevision.getBranch();
        createRevision(
            paste,
            branch.getName(),
            "Rollback to revision " + targetRevision.getRevisionNumber(),
            author
        );
    }
}
