package cc.allyapps.pastely.services;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.model.database.Webhook;
import cc.allyapps.pastely.model.database.WebhookEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.orm.Repo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebhookService - Service for triggering webhooks
 * Part of Phase 3: Integrations
 */
public class WebhookService {

    private static final Gson gson = new Gson();
    private static final int MAX_FAILURES = 5;

    /**
     * Trigger webhooks for a specific event
     *
     * @param userId    The user ID who owns the resource
     * @param eventType The event type (e.g., "paste.created", "paste.updated", "paste.deleted")
     * @param payload   The event payload
     */
    public static void trigger(String userId, String eventType, Object payload) {
        // Run asynchronously
        Pastely.getInstance().executeAsync(() -> {
            try {
                List<Webhook> webhooks = Repo.get(Webhook.class)
                        .where("userId", userId)
                        .where("isActive", true)
                        .get();

                for (Webhook webhook : webhooks) {
                    // Check if this webhook is subscribed to this event type
                    if (isSubscribedToEvent(webhook, eventType)) {
                        sendWebhook(webhook, eventType, payload);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error triggering webhooks: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Send a webhook request
     */
    private static void sendWebhook(Webhook webhook, String eventType, Object payload) {
        WebhookEvent event = new WebhookEvent();
        event.setWebhookId(webhook.getId());
        event.setEventType(eventType);

        try {
            // Create payload
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("event", eventType);
            webhookPayload.put("timestamp", Timestamp.from(Instant.now()).getTime());
            webhookPayload.put("data", payload);

            String payloadJson = gson.toJson(webhookPayload);
            event.setPayload(payloadJson);

            // Generate signature
            String signature = generateSignature(payloadJson, webhook.getSecret());

            // Send HTTP request
            HTTPClient client = new HTTPClient();
            String response = client.post(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Pastely-Signature", signature)
                    .header("X-Pastely-Event", eventType)
                    .header("User-Agent", "Pastely-Webhook/1.0")
                    .body(payloadJson)
                    .timeout(10000) // 10 second timeout
                    .string();

            // Mark as successful
            event.setSuccess(true);
            event.setResponseCode(200);
            event.setResponseBody(response != null && response.length() > 1000
                    ? response.substring(0, 1000) + "..."
                    : response);

            // Reset failure count on success
            webhook.setFailureCount(0);
            webhook.setLastTriggeredAt(Timestamp.from(Instant.now()));
            webhook.save();

        } catch (Exception e) {
            // Mark as failed
            event.setSuccess(false);
            event.setErrorMessage(e.getMessage());

            // Increment failure count
            webhook.incrementFailureCount();
            webhook.setLastTriggeredAt(Timestamp.from(Instant.now()));

            // Disable webhook after too many failures
            if (webhook.getFailureCount() >= MAX_FAILURES) {
                webhook.setActive(false);
                System.err.println("Webhook " + webhook.getId() + " disabled after " +
                        MAX_FAILURES + " consecutive failures");
            }

            webhook.save();

            System.err.println("Failed to send webhook to " + webhook.getUrl() + ": " + e.getMessage());
        }

        // Save event log
        event.save();
    }

    /**
     * Check if webhook is subscribed to an event type
     */
    private static boolean isSubscribedToEvent(Webhook webhook, String eventType) {
        String events = webhook.getEvents();
        if (events == null || events.isEmpty()) {
            return false;
        }

        // Support wildcard subscriptions
        if (events.contains("*")) {
            return true;
        }

        // Check if event type is in the comma-separated list
        List<String> eventList = Arrays.asList(events.split(","));
        for (String subscribedEvent : eventList) {
            subscribedEvent = subscribedEvent.trim();

            // Exact match
            if (subscribedEvent.equals(eventType)) {
                return true;
            }

            // Wildcard match (e.g., "paste.*" matches "paste.created")
            if (subscribedEvent.endsWith(".*")) {
                String prefix = subscribedEvent.substring(0, subscribedEvent.length() - 2);
                if (eventType.startsWith(prefix + ".")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Generate HMAC-SHA256 signature for webhook payload
     */
    private static String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            System.err.println("Failed to generate webhook signature: " + e.getMessage());
            return "";
        }
    }

    /**
     * Convert bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Helper methods for common webhook events
     */

    public static void pasteCreated(String userId, String pasteKey, Object pasteData) {
        trigger(userId, "paste.created", pasteData);
    }

    public static void pasteUpdated(String userId, String pasteKey, Object pasteData) {
        trigger(userId, "paste.updated", pasteData);
    }

    public static void pasteDeleted(String userId, String pasteKey) {
        Map<String, String> data = new HashMap<>();
        data.put("pasteKey", pasteKey);
        trigger(userId, "paste.deleted", data);
    }

    public static void pasteShared(String userId, String pasteKey, String sharedWithUserId) {
        Map<String, String> data = new HashMap<>();
        data.put("pasteKey", pasteKey);
        data.put("sharedWithUserId", sharedWithUserId);
        trigger(userId, "paste.shared", data);
    }
}
