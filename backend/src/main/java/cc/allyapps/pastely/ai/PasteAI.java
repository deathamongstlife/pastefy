package cc.allyapps.pastely.ai;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.helper.JarvisClient;
import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.algorithm.TagListing;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PasteAI {

    private final JarvisClient jarvisClient;

    public PasteAI(Pastely pastely) {
        String gatewayUrl = pastely.getConfig().get("jarvis.gateway.url", "http://127.0.0.1:18789");
        String apiToken = pastely.getConfig().get("jarvis.gateway.token");
        String agentId = pastely.getConfig().get("jarvis.agent.id", "main");
        int timeoutMs = pastely.getConfig().getInt("jarvis.timeout.ms", 30000);

        this.jarvisClient = new JarvisClient(gatewayUrl, apiToken, agentId, timeoutMs);
    }

    public boolean testConnection() {
        return jarvisClient.testConnection();
    }

    public String explainCode(String code, String language) throws IOException {
        String systemPrompt = "You are a helpful programming assistant. Explain the provided code in a clear, " +
                "concise manner. Focus on what the code does, its purpose, and any notable patterns or techniques used.";

        String userMessage = "Explain this " + (language != null ? language : "") + " code:\n\n" + code;

        return jarvisClient.chat(systemPrompt, userMessage, 1024);
    }

    public JsonObject detectBugs(String code, String language) throws IOException {
        String systemPrompt = "You are an expert code reviewer. Analyze the provided code for potential bugs, " +
                "security vulnerabilities, and code quality issues. " +
                "Respond ONLY with a JSON object in this format:\n" +
                "{\n" +
                "  \"bugs\": [{\"line\": number, \"severity\": \"low|medium|high|critical\", \"description\": string, \"suggestion\": string}],\n" +
                "  \"has_critical_issues\": boolean,\n" +
                "  \"summary\": string\n" +
                "}";

        String userMessage = "Analyze this " + (language != null ? language : "") + " code for bugs:\n\n" + code;

        return jarvisClient.chatJson(systemPrompt, userMessage);
    }

    public List<String> generateTags(String title, String content, String language) throws IOException {
        String systemPrompt = "Generate relevant tags for code snippets. Tags should be lowercase, use hyphens for spaces, " +
                "and include the programming language (format: lang-{language}). Maximum 8 tags, each max 30 characters. " +
                "Respond ONLY with a JSON object: {\"tags\": [string]}";

        String contentPreview = content.length() > 1000 ? content.substring(0, 1000) : content;
        String userMessage = "Title: " + title + "\nLanguage: " + (language != null ? language : "unknown") +
                "\nCode:\n" + contentPreview;

        JsonObject response = jarvisClient.chatJson(systemPrompt, userMessage);

        List<String> tags = new ArrayList<>();
        if (response.has("tags") && response.get("tags").isJsonArray()) {
            JsonArray tagsArray = response.getAsJsonArray("tags");
            for (int i = 0; i < tagsArray.size(); i++) {
                tags.add(tagsArray.get(i).getAsString());
            }
        }

        return tags;
    }

    public String translateCode(String code, String fromLanguage, String toLanguage) throws IOException {
        String systemPrompt = "You are an expert programmer proficient in multiple programming languages. " +
                "Translate the provided code from " + fromLanguage + " to " + toLanguage + ". " +
                "Maintain the same logic and functionality. Provide only the translated code without explanations.";

        String userMessage = "Translate this " + fromLanguage + " code to " + toLanguage + ":\n\n" + code;

        return jarvisClient.chat(systemPrompt, userMessage, 2048);
    }

    public JsonObject analyzeQuality(String code, String language) throws IOException {
        String systemPrompt = "You are a code quality expert. Analyze the provided code for quality metrics. " +
                "Respond ONLY with a JSON object in this format:\n" +
                "{\n" +
                "  \"overall_score\": number (0-100),\n" +
                "  \"readability\": number (0-100),\n" +
                "  \"maintainability\": number (0-100),\n" +
                "  \"complexity\": \"low|medium|high\",\n" +
                "  \"best_practices\": number (0-100),\n" +
                "  \"issues\": [string],\n" +
                "  \"strengths\": [string],\n" +
                "  \"summary\": string\n" +
                "}";

        String userMessage = "Analyze the quality of this " + (language != null ? language : "") + " code:\n\n" + code;

        return jarvisClient.chatJson(systemPrompt, userMessage);
    }

    public String generateDocumentation(String code, String language, String format) throws IOException {
        String systemPrompt = "You are a technical documentation expert. Generate comprehensive documentation " +
                "for the provided code in " + format + " format. Include function descriptions, parameters, " +
                "return values, and usage examples where applicable.";

        String userMessage = "Generate " + format + " documentation for this " +
                (language != null ? language : "") + " code:\n\n" + code;

        return jarvisClient.chat(systemPrompt, userMessage, 2048);
    }

    public JsonObject suggestImprovements(String code, String language) throws IOException {
        String systemPrompt = "You are a senior software engineer. Analyze the provided code and suggest improvements. " +
                "Respond ONLY with a JSON object in this format:\n" +
                "{\n" +
                "  \"improvements\": [{\"category\": string, \"description\": string, \"priority\": \"low|medium|high\", \"code_snippet\": string}],\n" +
                "  \"summary\": string\n" +
                "}";

        String userMessage = "Suggest improvements for this " + (language != null ? language : "") + " code:\n\n" + code;

        return jarvisClient.chatJson(systemPrompt, userMessage);
    }

    public String generateInfo(Paste paste) throws IOException {
        String systemPrompt = "Extract tags and a description from the following code snippet. " +
                "There are some default tags like lang-{programming_language}. To describe the language use always the lang-{programming_language} tag!\n" +
                "In system_warnings you can add security information for running this code or potential security flaws of the code. Max description 100. Severity: 1-10 (10 most dangerous, 1 harmless). If there is nothing above 5, leave it undefined.\n" +
                "(optional) suggested_filename: You can suggest another title if the current might be not that good\n" +
                "max characters for description: 200\n" +
                "dangerous: mark as dangerous if it's obviously dangerous and would harm users. Ignore examples to show the user something\n" +
                "Respond ONLY with a JSON object in this format:\n" +
                "{\n" +
                "    \"tags\": [string],\n" +
                "    \"description\": string\n" +
                "    \"system_warnings\": ?[{\"description\": string, severity: number}]\n" +
                "    \"suggested_filename\": ?string\n" +
                "    \"dangerous\": boolean\n" +
                "}";

        String contentPreview = paste.getContent().length() > 1000 ? paste.getContent().substring(0, 1000) : paste.getContent();
        String userMessage = "Title: " + paste.getTitle() + ". Contents: " + contentPreview;

        return jarvisClient.chat(systemPrompt, userMessage, 512);
    }

    public AbstractObject generateTags(Paste paste) throws IOException {
        String contents = paste.getContent();
        if (contents.length() > 500) {
            contents = contents.substring(0, 500);
        }

        String systemPrompt = "Generate tags (max. 6 tags and max length 30 chars), a file_name (with extension) " +
                "and file_extension (without dot) for this code. You can ignore file_name and file_extension if you can't " +
                "find anything obvious you can leave it empty. " +
                "There are some default tags like lang-{programming_language}. Tags only contain a-z 1-9 and -.\n" +
                "Respond ONLY with a JSON object in this format. Never generate anything else than JSON!:\n" +
                "{\n" +
                "    \"tags\": [string],\n" +
                "    \"file_name\": string,\n" +
                "    \"file_extension\": string\n" +
                "}";

        String userMessage = "Title: " + paste.getTitle() + ". Contents: " + contents;

        String response = jarvisClient.chat(systemPrompt, userMessage, 512);
        return AbstractElement.fromJson(response).object();
    }

    public String generateTagDescription(TagListing tagListing) throws IOException {
        String systemPrompt = "Generate a description for the tag given by the user. Max 150 chars. " +
                "Respond ONLY with the description. Never generate anything else than the description!";

        String userMessage = tagListing.tag;

        return jarvisClient.chat(systemPrompt, userMessage, 256);
    }
}
