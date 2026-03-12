package cc.allyapps.pastely.controller.publicarea;

import cc.allyapps.pastely.controller.HttpController;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.responses.user.PublicUserResponse;
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
