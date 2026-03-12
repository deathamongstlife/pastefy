package de.interaapps.pastely.auth;

import de.interaapps.pastely.exceptions.AwaitingAccessException;
import de.interaapps.pastely.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.handler.Middleware;

public class AwaitingAccessMiddleware implements Middleware {
    @Override
    public Object handle(Exchange exchange) {
        User user = exchange.attrib("user");

        if (user != null && user.type == User.Type.AWAITING_ACCESS) {
            throw new AwaitingAccessException();
        }

        return null;
    }
}
