package cc.allyapps.pastely.auth;

import cc.allyapps.pastely.exceptions.PermissionsDeniedException;
import cc.allyapps.pastely.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.handler.RequestHandler;

public class AdminMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {

        new AuthMiddleware().handle(exchange);

        User user = exchange.attrib("user");

        if (user.type != User.Type.ADMIN) {
            throw new PermissionsDeniedException();
        }

        return null;
    }
}
