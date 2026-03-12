package de.interaapps.pastely.model.requests.admin;

import de.interaapps.pastely.model.database.User;
import org.javawebstack.validator.rule.StringRule;

public class EditUserRequest {

    @StringRule(min = 2, max = 255)
    public String name;

    @StringRule(min = 2, max = 33)
    public String uniqueName;

    public User.Type type;
}
