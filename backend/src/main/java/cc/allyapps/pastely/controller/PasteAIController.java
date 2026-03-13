package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.ai.PasteAI;
import cc.allyapps.pastely.exceptions.FeatureDisabledException;
import cc.allyapps.pastely.model.requests.ai.*;
import cc.allyapps.pastely.model.responses.ai.AIStatusResponse;
import cc.allyapps.pastely.model.responses.ai.AITextResponse;
import com.google.gson.JsonObject;
import org.javawebstack.http.router.annotation.PathPrefix;
import org.javawebstack.http.router.annotation.With;
import org.javawebstack.http.router.annotation.params.Body;
import org.javawebstack.http.router.annotation.verbs.Get;
import org.javawebstack.http.router.annotation.verbs.Post;

import java.io.IOException;
import java.util.List;

@PathPrefix("/api/v2/ai")
public class PasteAIController extends HttpController {

    private PasteAI getPasteAI() {
        if (!Pastely.getInstance().aiEnabled()) {
            throw new FeatureDisabledException("AI features are not configured. Please set JARVIS_GATEWAY_TOKEN in your environment.");
        }
        return Pastely.getInstance().getPasteAI();
    }

    @Get("/status")
    public AIStatusResponse status() {
        if (!Pastely.getInstance().aiEnabled()) {
            return new AIStatusResponse(
                false,
                false,
                null,
                "AI features not configured. Set JARVIS_GATEWAY_TOKEN to enable."
            );
        }

        PasteAI pasteAI = Pastely.getInstance().getPasteAI();
        boolean connected = pasteAI.testConnection();

        return new AIStatusResponse(
            true,
            connected,
            Pastely.getInstance().getConfig().get("jarvis.agent.id", "main"),
            connected ? "AI gateway connected" : "AI gateway not reachable"
        );
    }

    @Post("/explain")
    @With({"auth"})
    public AITextResponse explainCode(@Body AIRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        String explanation = pasteAI.explainCode(request.code, request.language);
        return new AITextResponse(explanation);
    }

    @Post("/detect-bugs")
    @With({"auth"})
    public JsonObject detectBugs(@Body AIRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        return pasteAI.detectBugs(request.code, request.language);
    }

    @Post("/generate-tags")
    @With({"auth"})
    public JsonObject generateTags(@Body GenerateTagsRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.content == null || request.content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }

        List<String> tags = pasteAI.generateTags(
            request.title != null ? request.title : "",
            request.content,
            request.language
        );

        JsonObject response = new JsonObject();
        response.add("tags", com.google.gson.JsonParser.parseString(new com.google.gson.Gson().toJson(tags)));
        return response;
    }

    @Post("/translate")
    @With({"auth"})
    public AITextResponse translateCode(@Body TranslateCodeRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        if (request.fromLanguage == null || request.fromLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("fromLanguage is required");
        }

        if (request.toLanguage == null || request.toLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("toLanguage is required");
        }

        String translatedCode = pasteAI.translateCode(
            request.code,
            request.fromLanguage,
            request.toLanguage
        );

        return new AITextResponse(translatedCode);
    }

    @Post("/quality")
    @With({"auth"})
    public JsonObject analyzeQuality(@Body AIRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        return pasteAI.analyzeQuality(request.code, request.language);
    }

    @Post("/docs")
    @With({"auth"})
    public AITextResponse generateDocumentation(@Body GenerateDocsRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        String documentation = pasteAI.generateDocumentation(
            request.code,
            request.language,
            request.format != null ? request.format : "markdown"
        );

        return new AITextResponse(documentation);
    }

    @Post("/improve")
    @With({"auth"})
    public JsonObject suggestImprovements(@Body AIRequest request) throws IOException {
        PasteAI pasteAI = getPasteAI();

        if (request.code == null || request.code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        return pasteAI.suggestImprovements(request.code, request.language);
    }
}
