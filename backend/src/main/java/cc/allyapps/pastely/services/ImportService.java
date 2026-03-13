package cc.allyapps.pastely.services;

import cc.allyapps.pastely.exceptions.HTTPException;
import cc.allyapps.pastely.model.database.Paste;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javawebstack.http.client.HttpClient;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ImportService - Handles importing pastes from external services
 * Supports Pastebin, Hastebin, and other paste services
 */
public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = new HttpClient();

    /**
     * Import from Pastebin URL
     * Example: https://pastebin.com/ABC123
     */
    public static ImportResult importFromPastebin(String url) {
        try {
            // Extract paste ID from URL
            String pasteId = extractPastebinId(url);
            if (pasteId == null) {
                throw new HTTPException(400, "Invalid Pastebin URL");
            }

            // Pastebin raw URL format
            String rawUrl = "https://pastebin.com/raw/" + pasteId;

            var response = httpClient.get(rawUrl).execute();

            if (response.status() != 200) {
                throw new HTTPException(400, "Failed to fetch paste from Pastebin: " + response.status());
            }

            String content = response.string();

            ImportResult result = new ImportResult();
            result.content = content;
            result.title = "Imported from Pastebin: " + pasteId;
            result.type = "PLAIN_TEXT";
            result.source = "pastebin";
            result.sourceUrl = url;

            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import from Pastebin", e);
            throw new HTTPException(500, "Failed to import from Pastebin: " + e.getMessage());
        }
    }

    /**
     * Import from Hastebin/Toptal URL
     * Example: https://hastebin.com/abc123 or https://www.toptal.com/developers/hastebin/abc123
     */
    public static ImportResult importFromHastebin(String url) {
        try {
            // Extract document ID
            String docId = extractHastebinId(url);
            if (docId == null) {
                throw new HTTPException(400, "Invalid Hastebin URL");
            }

            // Try multiple Hastebin domains
            String[] domains = {
                    "https://hastebin.com",
                    "https://www.toptal.com/developers/hastebin"
            };

            String content = null;
            String successDomain = null;

            for (String domain : domains) {
                try {
                    String rawUrl = domain + "/raw/" + docId;
                    var response = httpClient.get(rawUrl).execute();

                    if (response.status() == 200) {
                        content = response.string();
                        successDomain = domain;
                        break;
                    }
                } catch (Exception e) {
                    // Try next domain
                }
            }

            if (content == null) {
                throw new HTTPException(400, "Failed to fetch paste from Hastebin");
            }

            ImportResult result = new ImportResult();
            result.content = content;
            result.title = "Imported from Hastebin: " + docId;
            result.type = "PLAIN_TEXT";
            result.source = "hastebin";
            result.sourceUrl = url;

            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import from Hastebin", e);
            throw new HTTPException(500, "Failed to import from Hastebin: " + e.getMessage());
        }
    }

    /**
     * Import from GitHub Gist
     * Example: https://gist.github.com/username/abc123
     */
    public static ImportResult importFromGist(String url) {
        try {
            // Extract gist ID from URL
            String gistId = extractGistId(url);
            if (gistId == null) {
                throw new HTTPException(400, "Invalid GitHub Gist URL");
            }

            // GitHub Gist API
            String apiUrl = "https://api.github.com/gists/" + gistId;

            var response = httpClient.get(apiUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .execute();

            if (response.status() != 200) {
                throw new HTTPException(400, "Failed to fetch Gist: " + response.status());
            }

            JsonObject gist = gson.fromJson(response.string(), JsonObject.class);
            JsonObject files = gist.getAsJsonObject("files");

            if (files == null || files.size() == 0) {
                throw new HTTPException(400, "Gist has no files");
            }

            // Get first file
            String firstFileName = files.keySet().iterator().next();
            JsonObject firstFile = files.getAsJsonObject(firstFileName);

            String content = firstFile.get("content").getAsString();
            String language = firstFile.has("language") ? firstFile.get("language").getAsString() : "PLAIN_TEXT";

            ImportResult result = new ImportResult();
            result.content = content;
            result.title = gist.has("description") && !gist.get("description").isJsonNull()
                    ? gist.get("description").getAsString()
                    : "Imported from Gist: " + gistId;
            result.type = mapLanguageToType(language);
            result.source = "gist";
            result.sourceUrl = url;

            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import from Gist", e);
            throw new HTTPException(500, "Failed to import from Gist: " + e.getMessage());
        }
    }

    /**
     * Extract Pastebin ID from URL
     */
    private static String extractPastebinId(String url) {
        if (url.contains("pastebin.com/")) {
            String[] parts = url.split("pastebin.com/");
            if (parts.length > 1) {
                String id = parts[1].split("[?#]")[0];
                if (id.startsWith("raw/")) {
                    id = id.substring(4);
                }
                return id;
            }
        }
        return null;
    }

    /**
     * Extract Hastebin document ID from URL
     */
    private static String extractHastebinId(String url) {
        String[] patterns = {
                "hastebin.com/",
                "hastebin/",
                "toptal.com/developers/hastebin/"
        };

        for (String pattern : patterns) {
            if (url.contains(pattern)) {
                String[] parts = url.split(pattern);
                if (parts.length > 1) {
                    String id = parts[1].split("[?#/]")[0];
                    if (id.startsWith("raw/")) {
                        id = id.substring(4);
                    }
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Extract GitHub Gist ID from URL
     */
    private static String extractGistId(String url) {
        if (url.contains("gist.github.com/")) {
            String[] parts = url.split("gist.github.com/");
            if (parts.length > 1) {
                String remainder = parts[1];
                // Format: username/gistid or just gistid
                String[] idParts = remainder.split("/");
                return idParts[idParts.length - 1].split("[?#]")[0];
            }
        }
        return null;
    }

    /**
     * Map GitHub language to Pastely paste type
     */
    private static String mapLanguageToType(String language) {
        if (language == null) return "PLAIN_TEXT";

        return switch (language.toUpperCase()) {
            case "JAVASCRIPT", "JS" -> "JAVASCRIPT";
            case "TYPESCRIPT", "TS" -> "TYPESCRIPT";
            case "PYTHON", "PY" -> "PYTHON";
            case "JAVA" -> "JAVA";
            case "C", "C++" -> "C";
            case "GO" -> "GO";
            case "RUST" -> "RUST";
            case "HTML" -> "HTML";
            case "CSS" -> "CSS";
            case "JSON" -> "JSON";
            case "XML" -> "XML";
            case "YAML", "YML" -> "YAML";
            case "MARKDOWN", "MD" -> "MARKDOWN";
            case "SQL" -> "SQL";
            case "SHELL", "BASH", "SH" -> "BASH";
            default -> "PLAIN_TEXT";
        };
    }

    /**
     * Import result container
     */
    public static class ImportResult {
        public String content;
        public String title;
        public String type;
        public String source;
        public String sourceUrl;
    }
}
