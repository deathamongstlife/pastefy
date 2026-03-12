package cc.allyapps.pastely.model.responses;

import com.google.gson.annotations.SerializedName;

/**
 * GistExportResponse - Response for GitHub Gist export
 */
public class GistExportResponse {

    @SerializedName("gist_url")
    public String gistUrl;

    public boolean success = true;

    public GistExportResponse(String gistUrl) {
        this.gistUrl = gistUrl;
    }
}
