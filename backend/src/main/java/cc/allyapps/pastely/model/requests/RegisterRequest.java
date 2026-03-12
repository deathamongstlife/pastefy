package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class RegisterRequest {
    @Rule("required|email")
    public String email;

    @Rule("required|min:8")
    public String password;

    @Rule("required")
    public String username;
}
