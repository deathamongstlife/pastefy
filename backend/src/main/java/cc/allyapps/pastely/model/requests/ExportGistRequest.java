package cc.allyapps.pastely.model.requests;

/**
 * ExportGistRequest - Request to export a paste to GitHub Gist
 */
public class ExportGistRequest {
    public String githubToken;
    public boolean isPublic = false;
}
