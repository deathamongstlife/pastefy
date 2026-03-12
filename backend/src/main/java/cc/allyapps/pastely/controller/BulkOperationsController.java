package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.http.router.annotation.*;
import org.javawebstack.orm.Repo;
import java.util.ArrayList;
import java.util.List;

@PathPrefix("/api/v2/bulk")
public class BulkOperationsController extends HttpController {

    @Post("/paste")
    @With({"auth"})
    public BulkOperationResponse bulkPasteOperation(@Body BulkPasteRequest request,
                                                   @Attrib("user") User user) {
        if (request.pasteKeys == null || request.pasteKeys.isEmpty()) {
            throw new HTTPException(400, "No paste keys provided");
        }

        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String pasteKey : request.pasteKeys) {
            try {
                Paste paste = Paste.get(pasteKey);
                if (paste == null) {
                    failed.add(pasteKey);
                    continue;
                }

                if (!paste.getUserId().equals(user.getId())) {
                    failed.add(pasteKey);
                    continue;
                }

                switch (request.action) {
                    case "delete":
                        paste.delete();
                        break;
                    case "move":
                        if (request.folderId != null) {
                            paste.setFolder(request.folderId);
                            paste.save();
                        }
                        break;
                    case "visibility":
                        if (request.visibility != null) {
                            try {
                                Paste.Visibility vis = Paste.Visibility.valueOf(request.visibility);
                                paste.setVisibility(vis);
                                paste.save();
                            } catch (IllegalArgumentException e) {
                                failed.add(pasteKey);
                                continue;
                            }
                        }
                        break;
                    default:
                        failed.add(pasteKey);
                        continue;
                }

                successful.add(pasteKey);
            } catch (Exception e) {
                failed.add(pasteKey);
            }
        }

        BulkOperationResponse response = new BulkOperationResponse();
        response.successful = successful;
        response.failed = failed;
        response.totalProcessed = request.pasteKeys.size();
        response.successCount = successful.size();
        response.failedCount = failed.size();

        return response;
    }

    @Post("/folder")
    @With({"auth"})
    public BulkOperationResponse bulkFolderOperation(@Body BulkFolderRequest request,
                                                    @Attrib("user") User user) {
        if (request.folderKeys == null || request.folderKeys.isEmpty()) {
            throw new HTTPException(400, "No folder keys provided");
        }

        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String folderKey : request.folderKeys) {
            try {
                Folder folder = Folder.get(folderKey);
                if (folder == null) {
                    failed.add(folderKey);
                    continue;
                }

                if (!folder.getUserId().equals(user.getId())) {
                    failed.add(folderKey);
                    continue;
                }

                switch (request.action) {
                    case "delete":
                        folder.delete();
                        break;
                    case "move":
                        if (request.parentFolderId != null) {
                            folder.setParent(request.parentFolderId);
                            folder.save();
                        }
                        break;
                    default:
                        failed.add(folderKey);
                        continue;
                }

                successful.add(folderKey);
            } catch (Exception e) {
                failed.add(folderKey);
            }
        }

        BulkOperationResponse response = new BulkOperationResponse();
        response.successful = successful;
        response.failed = failed;
        response.totalProcessed = request.folderKeys.size();
        response.successCount = successful.size();
        response.failedCount = failed.size();

        return response;
    }
}
