package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.rule.RequiredRule;
import java.util.List;

public class BulkFolderRequest {
    @RequiredRule
    public List<String> folderKeys;

    public String action;

    public String parentFolderId;
}
