package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class ResetPasswordRequest {
    @Rule("required")
    public String token;

    @Rule("required|min:8")
    public String newPassword;
}
