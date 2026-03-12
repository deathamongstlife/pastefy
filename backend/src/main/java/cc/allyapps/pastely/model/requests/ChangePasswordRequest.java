package cc.allyapps.pastely.model.requests;

import org.javawebstack.validator.Rule;

public class ChangePasswordRequest {
    @Rule("required")
    public String currentPassword;

    @Rule("required|min:8")
    public String newPassword;
}
