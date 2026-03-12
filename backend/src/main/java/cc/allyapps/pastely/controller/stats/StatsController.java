package cc.allyapps.pastely.controller.stats;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.controller.HttpController;
import cc.allyapps.pastely.exceptions.PermissionsDeniedException;
import cc.allyapps.pastely.model.database.AuthKey;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.responses.app.StatsResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app/stats")
public class StatsController extends HttpController {
    @Get
    public StatsResponse stats(@Attrib("authkey") AuthKey authKey, @Attrib("user") User user) {

        if (
                !Pastely.getInstance().getConfig().get("pastely.publicstats", "false").equalsIgnoreCase("true")
                        && (authKey == null || !authKey.hasPermission("stats:read"))
                        && (user == null || user.type != User.Type.ADMIN)
        )
            throw new PermissionsDeniedException();

        return StatsResponse.create();
    }
}
