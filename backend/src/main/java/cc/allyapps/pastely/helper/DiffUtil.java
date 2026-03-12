package cc.allyapps.pastely.helper;

import cc.allyapps.pastely.model.database.PasteRevision;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.github.difflib.UnifiedDiffUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class DiffUtil {

    /**
     * Generate unified diff between old and new content
     */
    public static String generateDiff(String oldContent, String newContent) {
        if (oldContent == null) oldContent = "";
        if (newContent == null) newContent = "";

        List<String> oldLines = Arrays.asList(oldContent.split("\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\n", -1));

        Patch<String> patch = DiffUtils.diff(oldLines, newLines);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
            "old", "new", oldLines, patch, 3
        );

        return String.join("\n", unifiedDiff);
    }

    /**
     * Apply diff to base content to get new content
     */
    public static String applyDiff(String baseContent, String diff) throws PatchFailedException {
        if (baseContent == null) baseContent = "";
        if (diff == null || diff.isEmpty()) return baseContent;

        List<String> baseLines = Arrays.asList(baseContent.split("\n", -1));
        Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(Arrays.asList(diff.split("\n")));

        List<String> result = patch.applyTo(baseLines);
        return String.join("\n", result);
    }

    /**
     * Reconstruct content from revision history
     */
    public static String reconstructContent(PasteRevision targetRevision) {
        // Find the base revision (first in chain)
        PasteRevision current = targetRevision;
        Stack<PasteRevision> revisionStack = new Stack<>();

        while (current != null) {
            revisionStack.push(current);
            current = current.getParentRevision();
        }

        // Apply diffs from bottom to top
        String content = "";
        while (!revisionStack.isEmpty()) {
            PasteRevision rev = revisionStack.pop();
            try {
                content = applyDiff(content, rev.getDiff());
            } catch (PatchFailedException e) {
                throw new RuntimeException("Failed to apply revision: " + rev.getId(), e);
            }
        }

        return content;
    }
}
