package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.CodeTemplate;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.params.Query;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.http.router.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * TemplateController - Code template management
 * Part of the Enhanced Editor feature
 */
@PathPrefix("/api/v2/templates")
public class TemplateController extends HttpController {

    @Post
    @With({"auth"})
    public CodeTemplate createTemplate(
            @Body("name") String name,
            @Body("description") String description,
            @Body("language") String language,
            @Body("content") String content,
            @Body("category") String category,
            @Body("isPublic") Boolean isPublic,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        CodeTemplate template = new CodeTemplate();
        template.setUserId(user.getId());
        template.setName(name);
        template.setDescription(description);
        template.setLanguage(language);
        template.setContent(content);
        template.setCategory(category);
        template.setPublic(isPublic != null && isPublic);
        template.save();

        return template;
    }

    @Get
    public List<CodeTemplate> getTemplates(
            @Query("language") String language,
            @Query("category") String category,
            @Query("public") Boolean publicOnly,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");

        var query = Repo.get(CodeTemplate.class);

        if (publicOnly == null || publicOnly) {
            query = query.where("isPublic", true);
        } else if (user != null) {
            // Show public templates and user's own templates
            query = query.where(q -> q
                    .where("isPublic", true)
                    .orWhere("userId", user.getId())
            );
        }

        if (language != null) {
            query = query.where("language", language);
        }

        if (category != null) {
            query = query.where("category", category);
        }

        return query.orderBy("useCount", true).get();
    }

    @Get("/{templateId}")
    public CodeTemplate getTemplate(@Path("templateId") String templateId, Exchange exchange) {
        CodeTemplate template = Repo.get(CodeTemplate.class).where("id", templateId).first();
        if (template == null) throw new NotFoundException();

        User user = exchange.attrib("user");
        if (!template.isPublic() && (user == null || !Objects.equals(template.getUserId(), user.getId()))) {
            throw new AuthenticationException();
        }

        return template;
    }

    @Put("/{templateId}")
    @With({"auth"})
    public CodeTemplate updateTemplate(
            @Path("templateId") String templateId,
            @Body("name") String name,
            @Body("description") String description,
            @Body("content") String content,
            @Body("category") String category,
            @Body("isPublic") Boolean isPublic,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        CodeTemplate template = Repo.get(CodeTemplate.class).where("id", templateId).first();
        if (template == null) throw new NotFoundException();

        if (!Objects.equals(template.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (name != null) template.setName(name);
        if (description != null) template.setDescription(description);
        if (content != null) template.setContent(content);
        if (category != null) template.setCategory(category);
        if (isPublic != null) template.setPublic(isPublic);

        template.setUpdatedAt(Timestamp.from(Instant.now()));
        template.save();

        return template;
    }

    @Delete("/{templateId}")
    @With({"auth"})
    public ActionResponse deleteTemplate(@Path("templateId") String templateId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        CodeTemplate template = Repo.get(CodeTemplate.class).where("id", templateId).first();
        if (template == null) throw new NotFoundException();

        if (!Objects.equals(template.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        template.delete();

        return ActionResponse.success();
    }

    @Post("/{templateId}/use")
    public ActionResponse incrementUseCount(@Path("templateId") String templateId) {
        CodeTemplate template = Repo.get(CodeTemplate.class).where("id", templateId).first();
        if (template == null) throw new NotFoundException();

        template.incrementUseCount();
        template.save();

        return ActionResponse.success();
    }

    @Get("/user/{userId}")
    public List<CodeTemplate> getUserTemplates(@Path("userId") String userId, Exchange exchange) {
        User currentUser = exchange.attrib("user");
        boolean isOwner = currentUser != null && Objects.equals(currentUser.getId(), userId);

        if (isOwner) {
            return Repo.get(CodeTemplate.class)
                    .where("userId", userId)
                    .orderBy("createdAt", true)
                    .get();
        } else {
            return Repo.get(CodeTemplate.class)
                    .where("userId", userId)
                    .where("isPublic", true)
                    .orderBy("createdAt", true)
                    .get();
        }
    }
}
