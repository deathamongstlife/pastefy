package de.interaapps.pastely.controller;

import de.interaapps.pastely.Pastely;
import de.interaapps.pastely.model.responses.app.AppInfoResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app")
public class AppController extends HttpController {
    @Get("/info")
    public AppInfoResponse appInfo() {
        return new AppInfoResponse(Pastely.getInstance());
    }
}
