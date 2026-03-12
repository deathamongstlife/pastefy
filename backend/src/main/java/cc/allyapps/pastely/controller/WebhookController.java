package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.database.Webhook;
import cc.allyapps.pastely.model.database.WebhookEvent;
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
 * WebhookController - Webhook management
 * Part of the Integrations feature
 */
@PathPrefix("/api/v2/webhooks")
public class WebhookController extends HttpController {

    @Post
    @With({"auth"})
    public Webhook createWebhook(
            @Body("url") String url,
            @Body("events") String events,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = new Webhook();
        webhook.setUserId(user.getId());
        webhook.setUrl(url);
        webhook.setEvents(events);
        webhook.save();

        return webhook;
    }

    @Get
    @With({"auth"})
    public List<Webhook> getWebhooks(Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        return Repo.get(Webhook.class)
                .where("userId", user.getId())
                .orderBy("createdAt", true)
                .get();
    }

    @Get("/{webhookId}")
    @With({"auth"})
    public Webhook getWebhook(@Path("webhookId") String webhookId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = Repo.get(Webhook.class).where("id", webhookId).first();
        if (webhook == null) throw new NotFoundException();

        if (!Objects.equals(webhook.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        return webhook;
    }

    @Put("/{webhookId}")
    @With({"auth"})
    public Webhook updateWebhook(
            @Path("webhookId") String webhookId,
            @Body("url") String url,
            @Body("events") String events,
            @Body("isActive") Boolean isActive,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = Repo.get(Webhook.class).where("id", webhookId).first();
        if (webhook == null) throw new NotFoundException();

        if (!Objects.equals(webhook.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (url != null) webhook.setUrl(url);
        if (events != null) webhook.setEvents(events);
        if (isActive != null) webhook.setActive(isActive);

        webhook.setUpdatedAt(Timestamp.from(Instant.now()));
        webhook.save();

        return webhook;
    }

    @Delete("/{webhookId}")
    @With({"auth"})
    public ActionResponse deleteWebhook(@Path("webhookId") String webhookId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = Repo.get(Webhook.class).where("id", webhookId).first();
        if (webhook == null) throw new NotFoundException();

        if (!Objects.equals(webhook.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        webhook.delete();

        return ActionResponse.success();
    }

    @Get("/{webhookId}/events")
    @With({"auth"})
    public List<WebhookEvent> getWebhookEvents(
            @Path("webhookId") String webhookId,
            @Query("limit") Integer limit,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = Repo.get(Webhook.class).where("id", webhookId).first();
        if (webhook == null) throw new NotFoundException();

        if (!Objects.equals(webhook.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(WebhookEvent.class)
                .where("webhookId", webhookId)
                .orderBy("attemptedAt", true)
                .limit(limit)
                .get();
    }

    @Post("/{webhookId}/test")
    @With({"auth"})
    public ActionResponse testWebhook(@Path("webhookId") String webhookId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Webhook webhook = Repo.get(Webhook.class).where("id", webhookId).first();
        if (webhook == null) throw new NotFoundException();

        if (!Objects.equals(webhook.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        // Log test event
        WebhookEvent event = new WebhookEvent();
        event.setWebhookId(webhookId);
        event.setEventType("test");
        event.setPayload("{\"test\": true}");
        event.setSuccess(true);
        event.save();

        return ActionResponse.success();
    }
}
