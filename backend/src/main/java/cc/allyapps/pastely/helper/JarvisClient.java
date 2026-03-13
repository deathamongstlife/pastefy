package cc.allyapps.pastely.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpclient.HTTPRequest;
import org.javawebstack.httpclient.HTTPResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JarvisClient {

    private final String gatewayUrl;
    private final String apiToken;
    private final String agentId;
    private final int timeoutMs;
    private final Gson gson;

    public JarvisClient(String gatewayUrl, String apiToken, String agentId, int timeoutMs) {
        this.gatewayUrl = gatewayUrl;
        this.apiToken = apiToken;
        this.agentId = agentId;
        this.timeoutMs = timeoutMs;
        this.gson = new Gson();
    }

    public boolean testConnection() {
        try {
            HTTPRequest request = HTTPClient.builder()
                .url(gatewayUrl + "/health")
                .timeout(5000)
                .build();

            HTTPResponse response = request.get();
            return response.status() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String chat(String systemPrompt, String userMessage) throws IOException {
        return chat(systemPrompt, userMessage, 1024);
    }

    public String chat(String systemPrompt, String userMessage, int maxTokens) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("agent_id", agentId);
        requestBody.addProperty("model", "gpt-4o-mini");
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", systemPrompt);
            messages.add(systemMsg);
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        requestBody.add("messages", messages);

        HTTPRequest request = HTTPClient.builder()
            .url(gatewayUrl + "/v1/chat/completions")
            .timeout(timeoutMs)
            .header("Authorization", "Bearer " + apiToken)
            .header("Content-Type", "application/json")
            .build();

        HTTPResponse response = request.post(gson.toJson(requestBody));

        if (response.status() != 200) {
            throw new IOException("Jarvis Gateway returned status " + response.status() + ": " + response.content());
        }

        JsonObject responseJson = gson.fromJson(response.content(), JsonObject.class);

        if (responseJson.has("choices") && responseJson.getAsJsonArray("choices").size() > 0) {
            JsonObject choice = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                return choice.getAsJsonObject("message").get("content").getAsString();
            }
        }

        throw new IOException("Invalid response format from Jarvis Gateway");
    }

    public JsonObject chatJson(String systemPrompt, String userMessage) throws IOException {
        String response = chat(systemPrompt, userMessage, 2048);

        try {
            String jsonContent = extractJson(response);
            return gson.fromJson(jsonContent, JsonObject.class);
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON response: " + e.getMessage());
        }
    }

    private String extractJson(String text) {
        text = text.trim();

        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }

        text = text.trim();

        int jsonStart = text.indexOf('{');
        int jsonEnd = text.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }

        return text;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getAgentId() {
        return agentId;
    }
}
