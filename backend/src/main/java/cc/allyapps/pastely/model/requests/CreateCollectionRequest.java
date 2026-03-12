package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.rule.RequiredRule;

public class CreateCollectionRequest {
    @RequiredRule
    public String name;

    public String description = "";

    public boolean isPublic = false;

    public String icon;

    public String color;
}
