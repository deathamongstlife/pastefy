package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.exceptions.PermissionsDeniedException;
import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.services.ExportService;
import cc.allyapps.pastely.services.PasteService;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;

import java.io.IOException;

/**
 * ExportController - Handles exporting pastes to various formats
 * Supports PDF, HTML, and image exports
 */
@PathPrefix("/api/v2/export")
public class ExportController extends HttpController {

    /**
     * Export paste as PDF
     */
    @Get("/paste/{key}/pdf")
    public byte[] exportAsPdf(@Path("key") String key,
                               Exchange exchange,
                               @Attrib("user") User user) throws IOException {
        Paste paste = Repo.get(Paste.class).where("key", key).first();

        if (paste == null) {
            throw new NotFoundException("Paste not found");
        }

        // Check access permissions
        if (!PasteService.canAccess(paste, user)) {
            throw new PermissionsDeniedException();
        }

        byte[] pdfBytes = ExportService.exportAsPdf(paste);

        exchange.header("Content-Type", "application/pdf");
        exchange.header("Content-Disposition", "attachment; filename=\"paste-" + key + ".pdf\"");

        return pdfBytes;
    }

    /**
     * Export paste as standalone HTML
     */
    @Get("/paste/{key}/html")
    public String exportAsHtml(@Path("key") String key,
                                Exchange exchange,
                                @Attrib("user") User user) {
        Paste paste = Repo.get(Paste.class).where("key", key).first();

        if (paste == null) {
            throw new NotFoundException("Paste not found");
        }

        // Check access permissions
        if (!PasteService.canAccess(paste, user)) {
            throw new PermissionsDeniedException();
        }

        String theme = exchange.getQueryParameters().getString("theme", "light");
        String html = ExportService.exportAsHtml(paste, theme);

        exchange.header("Content-Type", "text/html; charset=utf-8");
        exchange.header("Content-Disposition", "attachment; filename=\"paste-" + key + ".html\"");

        return html;
    }

    /**
     * Export paste as PNG image
     */
    @Get("/paste/{key}/image")
    public byte[] exportAsImage(@Path("key") String key,
                                 Exchange exchange,
                                 @Attrib("user") User user) throws IOException {
        Paste paste = Repo.get(Paste.class).where("key", key).first();

        if (paste == null) {
            throw new NotFoundException("Paste not found");
        }

        // Check access permissions
        if (!PasteService.canAccess(paste, user)) {
            throw new PermissionsDeniedException();
        }

        int width = exchange.getQueryParameters().getInt("width", 1200);
        int height = exchange.getQueryParameters().getInt("height", 800);

        byte[] imageBytes = ExportService.exportAsImage(paste, width, height);

        exchange.header("Content-Type", "image/svg+xml");
        exchange.header("Content-Disposition", "attachment; filename=\"paste-" + key + ".svg\"");

        return imageBytes;
    }
}
