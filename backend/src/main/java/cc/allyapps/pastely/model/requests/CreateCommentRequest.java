package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.rule.RequiredRule;

public class CreateCommentRequest {
    @RequiredRule
    public String content;

    public String parentCommentId;

    public Integer lineNumber;
}
