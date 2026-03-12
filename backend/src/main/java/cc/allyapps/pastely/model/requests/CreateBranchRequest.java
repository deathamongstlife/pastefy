package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class CreateBranchRequest {
    @Rule("required")
    public String name;

    public String fromRevisionId;
}
