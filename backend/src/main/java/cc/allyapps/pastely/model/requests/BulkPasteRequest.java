package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.rule.RequiredRule;
import java.util.List;

public class BulkPasteRequest {
    @RequiredRule
    public List<String> pasteKeys;

    public String action;

    public String folderId;

    public String visibility;
}
