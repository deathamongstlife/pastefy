package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.*;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.ExportGistRequest;
import cc.allyapps.pastely.model.requests.ImportGistRequest;
import cc.allyapps.pastely.model.responses.GistExportResponse;
import cc.allyapps.pastely.model.responses.paste.PasteResponse;
import com.google.gson.*;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.httpclient.HTTPClient;

import java.util.Map;

/**
 * IntegrationController - External integrations (GitHub Gist, etc.)
 * Part of Phase 3: Integrations
 */
@PathPrefix("/api/v2/integration")
public class IntegrationController extends HttpController {

    @Post("/gist/import")
    @With({"auth", "rate-limiter"})
    public PasteResponse importFromGist(
            @Body ImportGistRequest request,
            @Attrib("user") User user,
            Exchange exchange
    ) {
        try {
            // Extract gist ID from URL
            String gistId = extractGistId(request.url);

            // Fetch gist from GitHub API
            HTTPClient client = new HTTPClient();
            String response = client.get("https://api.github.com/gists/" + gistId)
                    .header("Accept", "application/vnd.github+json")
                    .string();

            JsonObject gist = JsonParser.parseString(response).getAsJsonObject();
            JsonObject files = gist.getAsJsonObject("files");

            if (files == null || files.size() == 0) {
                throw new HTTPException(400, "Gist has no files");
            }

            // Create paste from first file
            Map.Entry<String, JsonElement> firstFile = files.entrySet().iterator().next();
            JsonObject fileObj = firstFile.getValue().getAsJsonObject();

            String fileName = firstFile.getKey();
            String content = fileObj.get("content").getAsString();
            String language = fileObj.has("language") && !fileObj.get("language").isJsonNull()
                    ? fileObj.get("language").getAsString()
                    : "text";

            // Get gist description
            String description = gist.has("description") && !gist.get("description").isJsonNull()
                    ? gist.get("description").getAsString()
                    : fileName;

            // Create paste
            Paste paste = new Paste();
            paste.setTitle(description);
            paste.setContent(content);
            paste.setUserId(user.getId());
            paste.setVisibility(Paste.Visibility.PRIVATE);

            // Try to set language/type based on file extension
            if (language != null && !language.equalsIgnoreCase("text")) {
                paste.setType(language.toLowerCase());
            } else {
                // Try to detect from filename
                String detectedType = detectTypeFromFilename(fileName);
                if (detectedType != null) {
                    paste.setType(detectedType);
                }
            }

            paste.save();

            return PasteResponse.create(paste, exchange, user);

        } catch (HTTPException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPException(400, "Failed to import gist: " + e.getMessage());
        }
    }

    @Post("/gist/export/{pasteKey}")
    @With({"auth", "rate-limiter"})
    public GistExportResponse exportToGist(
            @Path("pasteKey") String pasteKey,
            @Body ExportGistRequest request,
            @Attrib("user") User user
    ) {
        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!paste.getUserId().equals(user.getId())) {
            throw new PermissionsDeniedException();
        }

        if (request.githubToken == null || request.githubToken.isEmpty()) {
            throw new HTTPException(400, "GitHub token required");
        }

        try {
            // Create gist on GitHub
            HTTPClient client = new HTTPClient();
            client.header("Authorization", "Bearer " + request.githubToken);
            client.header("Accept", "application/vnd.github+json");

            JsonObject payload = new JsonObject();
            payload.addProperty("description", paste.getTitle());
            payload.addProperty("public", request.isPublic);

            // Determine filename
            String filename = paste.getTitle();
            if (!filename.contains(".")) {
                // Add extension based on paste type
                String extension = getExtensionForType(paste.getType());
                filename = filename + "." + extension;
            }

            JsonObject files = new JsonObject();
            JsonObject file = new JsonObject();
            file.addProperty("content", paste.getContent());
            files.add(filename, file);
            payload.add("files", files);

            String response = client.post("https://api.github.com/gists")
                    .body(payload.toString())
                    .string();

            JsonObject gist = JsonParser.parseString(response).getAsJsonObject();

            if (!gist.has("html_url")) {
                throw new HTTPException(500, "Invalid response from GitHub");
            }

            String gistUrl = gist.get("html_url").getAsString();

            return new GistExportResponse(gistUrl);

        } catch (HTTPException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPException(500, "Failed to export to gist: " + e.getMessage());
        }
    }

    // Helper methods

    private String extractGistId(String url) {
        if (url == null || url.isEmpty()) {
            throw new HTTPException(400, "Invalid gist URL");
        }

        // Handle various formats:
        // https://gist.github.com/username/gistid
        // https://gist.github.com/gistid
        // gistid

        url = url.trim();

        // If it's just an ID
        if (!url.contains("/") && !url.contains(".")) {
            return url;
        }

        // Extract from URL
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // Remove any query parameters or fragments
            lastPart = lastPart.split("\\?")[0].split("#")[0];
            if (!lastPart.isEmpty()) {
                return lastPart;
            }
        }

        throw new HTTPException(400, "Could not extract gist ID from URL");
    }

    private String detectTypeFromFilename(String filename) {
        if (filename == null) return null;

        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return null;
        }

        String extension = filename.substring(lastDot + 1).toLowerCase();

        // Map common extensions to types
        switch (extension) {
            case "js":
                return "javascript";
            case "ts":
                return "typescript";
            case "py":
                return "python";
            case "java":
                return "java";
            case "cpp":
            case "cc":
            case "cxx":
                return "cpp";
            case "c":
                return "c";
            case "cs":
                return "csharp";
            case "go":
                return "go";
            case "rs":
                return "rust";
            case "rb":
                return "ruby";
            case "php":
                return "php";
            case "html":
                return "html";
            case "css":
                return "css";
            case "json":
                return "json";
            case "xml":
                return "xml";
            case "yaml":
            case "yml":
                return "yaml";
            case "md":
                return "markdown";
            case "sh":
            case "bash":
                return "bash";
            case "sql":
                return "sql";
            default:
                return null;
        }
    }

    private String getExtensionForType(String type) {
        if (type == null) return "txt";

        type = type.toLowerCase();

        // Map types to extensions
        switch (type) {
            case "javascript":
                return "js";
            case "typescript":
                return "ts";
            case "python":
                return "py";
            case "java":
                return "java";
            case "cpp":
                return "cpp";
            case "c":
                return "c";
            case "csharp":
                return "cs";
            case "go":
                return "go";
            case "rust":
                return "rs";
            case "ruby":
                return "rb";
            case "php":
                return "php";
            case "html":
                return "html";
            case "css":
                return "css";
            case "json":
                return "json";
            case "xml":
                return "xml";
            case "yaml":
                return "yaml";
            case "markdown":
                return "md";
            case "bash":
            case "shell":
                return "sh";
            case "sql":
                return "sql";
            default:
                return "txt";
        }
    }
}
