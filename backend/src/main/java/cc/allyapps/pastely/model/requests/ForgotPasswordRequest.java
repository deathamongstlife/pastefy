package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class ForgotPasswordRequest {
    @Rule("required|email")
    public String email;
}
