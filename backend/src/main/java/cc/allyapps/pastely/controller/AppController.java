package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.model.responses.app.AppInfoResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app")
public class AppController extends HttpController {
    @Get("/info")
    public AppInfoResponse appInfo() {
        return new AppInfoResponse(Pastely.getInstance());
    }
}
