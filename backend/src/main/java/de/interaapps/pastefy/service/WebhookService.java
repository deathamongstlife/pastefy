package de.interaapps.pastefy.service;

import com.google.gson.Gson;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Webhook;
import de.interaapps.pastefy.model.database.WebhookEvent;
import org.javawebstack.http.client.HttpClient;
import org.javawebstack.orm.Repo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * WebhookService - Handles webhook dispatching
 * Part of the Integrations feature
 */
public class WebhookService {

    private static final Gson gson = new Gson();

    public static void triggerWebhooks(String userId, String eventType, Map<String, Object> payload) {
        List<Webhook> webhooks = Repo.get(Webhook.class)
                .where("userId", userId)
                .where("isActive", true)
                .get();

        for (Webhook webhook : webhooks) {
            // Check if this webhook subscribes to this event type
            if (webhook.getEvents() != null && !webhook.getEvents().isEmpty()) {
                List<String> subscribedEvents = Arrays.asList(webhook.getEvents().split(","));
                if (!subscribedEvents.contains(eventType)) {
                    continue;
                }
            }

            // Dispatch asynchronously
            Pastefy.getInstance().executeAsync(() -> dispatchWebhook(webhook, eventType, payload));
        }
    }

    private static void dispatchWebhook(Webhook webhook, String eventType, Map<String, Object> payload) {
        WebhookEvent event = new WebhookEvent();
        event.setWebhookId(webhook.getId());
        event.setEventType(eventType);

        try {
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("event", eventType);
            webhookPayload.put("timestamp", Instant.now().toString());
            webhookPayload.put("data", payload);

            String payloadJson = gson.toJson(webhookPayload);
            event.setPayload(payloadJson);

            // Generate signature
            String signature = generateSignature(payloadJson, webhook.getSecret());

            // Send HTTP POST request
            HttpClient client = new HttpClient();
            var response = client
                    .post(webhook.getUrl())
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Event", eventType)
                    .header("Content-Type", "application/json")
                    .data(payloadJson)
                    .execute();

            event.setResponseCode(response.status());
            event.setResponseBody(response.string().substring(0, Math.min(1024, response.string().length())));
            event.setSuccess(response.status() >= 200 && response.status() < 300);

            if (!event.isSuccess()) {
                webhook.incrementFailureCount();

                // Disable webhook after 10 consecutive failures
                if (webhook.getFailureCount() >= 10) {
                    webhook.setActive(false);
                }
            } else {
                webhook.setFailureCount(0);
            }

            webhook.setLastTriggeredAt(Timestamp.from(Instant.now()));
            webhook.save();

        } catch (Exception e) {
            event.setSuccess(false);
            event.setResponseBody("Error: " + e.getMessage());

            webhook.incrementFailureCount();
            if (webhook.getFailureCount() >= 10) {
                webhook.setActive(false);
            }
            webhook.save();
        }

        event.save();
    }

    private static String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }
}
