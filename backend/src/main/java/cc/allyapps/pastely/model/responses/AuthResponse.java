package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.model.database.User;
import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    public String accessToken;

    public UserResponse user;

    public static AuthResponse create(String accessToken, User user) {
        AuthResponse response = new AuthResponse();
        response.accessToken = accessToken;
        response.user = UserResponse.create(user);
        return response;
    }
}
