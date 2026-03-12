package de.interaapps.pastely.controller.publicarea;

import de.interaapps.pastely.controller.HttpController;
import de.interaapps.pastely.exceptions.NotFoundException;
import de.interaapps.pastely.model.database.User;
import de.interaapps.pastely.model.responses.user.PublicUserResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@With("public-pastes-endpoint")
@PathPrefix("/api/v2/public/user")
public class PublicUserController extends HttpController {
    @Get("/{name}")
    public PublicUserResponse getUser(@Path("name") String name) {
        User user = User.getByName(name);
        if (user == null) throw new NotFoundException();
        return new PublicUserResponse(user);
    }
}
