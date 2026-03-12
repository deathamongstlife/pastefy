package de.interaapps.pastely.controller.stats;

import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.controller.HttpController;
import de.interaapps.pastely.exceptions.PermissionsDeniedException;
import de.interaapps.pastely.model.database.AuthKey;
import de.interaapps.pastely.model.database.User;
import de.interaapps.pastely.model.responses.app.StatsResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app/stats")
public class StatsController extends HttpController {
    @Get
    public StatsResponse stats(@Attrib("authkey") AuthKey authKey, @Attrib("user") User user) {

        if (
                !Pastely.getInstance().getConfig().get("pastefy.publicstats", "false").equalsIgnoreCase("true")
                        && (authKey == null || !authKey.hasPermission("stats:read"))
                        && (user == null || user.type != User.Type.ADMIN)
        )
            throw new PermissionsDeniedException();

        return StatsResponse.create();
    }
}
