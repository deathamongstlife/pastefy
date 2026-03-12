package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class LoginRequest {
    @Rule("required|email")
    public String email;

    @Rule("required")
    public String password;
}
