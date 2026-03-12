package de.interaapps.pastely.controller.auth;

import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.auth.strategies.oauth2.OAuth2Provider;
import de.interaapps.pastely.controller.HttpController;
import de.interaapps.pastely.exceptions.AuthenticationException;
import de.interaapps.pastely.exceptions.NotFoundException;
import de.interaapps.pastely.model.database.AuthKey;
import de.interaapps.pastely.model.database.User;
import de.interaapps.pastely.model.requests.auth.InteraAppsExternalAccessRequest;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.webutils.config.Config;

import java.util.Map;

@PathPrefix("/api/v2/auth")
public class InteraAppsExternalAccessController extends HttpController {
    @Post("/iaea")
    public String iaea(@Body InteraAppsExternalAccessRequest request, Exchange exchange) {
        Map<String, OAuth2Provider> providers = Pastely.getInstance().getOAuth2Strategy().getProviders();

        if (providers.containsKey("interaapps")) {

            Config config = Pastely.getInstance().getConfig();

            if (config.get("oauth2.interaapps.id").equals(request.appId) && config.get("oauth2.interaapps.secret").equals(request.appSecret)) {
                User user = Repo.get(User.class).where("authId", request.userId).where("authProvider", User.AuthenticationProvider.INTERAAPPS).first();

                if (user != null) {
                    AuthKey authKey = new AuthKey();
                    authKey.userId = user.id;
                    authKey.type = AuthKey.Type.ACCESS_TOKEN;
                    request.appScopeList.forEach(authKey::addScope);

                    authKey.save();
                    return authKey.getKey();
                }
            } else {
                throw new AuthenticationException();
            }
        }

        throw new NotFoundException();
    }
}
