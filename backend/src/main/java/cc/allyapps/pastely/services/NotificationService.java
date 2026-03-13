package cc.allyapps.pastely.services;

import cc.allyapps.pastely.Pastely;
import cc.allyapps.pastely.model.database.NotificationPreference;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.database.UserNotification;
import com.google.gson.Gson;
import org.javawebstack.http.client.HttpClient;
import org.javawebstack.orm.Repo;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NotificationService - Centralized notification delivery
 * Handles in-app, email, and webhook notifications
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private static final Gson gson = new Gson();

    /**
     * Send a notification to a user
     * Creates in-app notification and optionally sends email/webhook based on user preferences
     */
    public static UserNotification sendNotification(String userId, UserNotification.NotificationType type,
                                                     String title, String message, Map<String, Object> data) {
        return sendNotification(userId, type, title, message, data, null);
    }

    /**
     * Send a notification to a user with optional URL
     */
    public static UserNotification sendNotification(String userId, UserNotification.NotificationType type,
                                                     String title, String message,
                                                     Map<String, Object> data, String url) {
        // Create in-app notification
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUrl(url);

        if (data != null) {
            notification.setData(gson.toJson(data));
        }

        notification.save();

        // Get user preferences
        NotificationPreference preference = Repo.get(NotificationPreference.class)
                .where("userId", userId)
                .first();

        // Create default preferences if not exists
        if (preference == null) {
            preference = new NotificationPreference();
            preference.setUserId(userId);
            preference.save();
        }

        // Check if this notification type is enabled
        if (!preference.isTypeEnabled(type.name())) {
            return notification;
        }

        // Get user for email
        User user = Repo.get(User.class).where("id", userId).first();
        if (user == null) {
            return notification;
        }

        // Send email notification asynchronously if enabled
        if (preference.isEmailEnabled()) {
            Pastely.getInstance().executeAsync(() -> sendEmailNotification(user, notification));
        }

        // Send webhook notification asynchronously if enabled
        if (preference.isWebhookEnabled() && preference.getWebhookUrl() != null) {
            Pastely.getInstance().executeAsync(() -> sendWebhookNotification(preference.getWebhookUrl(), notification));
        }

        return notification;
    }

    /**
     * Send email notification via SMTP
     */
    private static void sendEmailNotification(User user, UserNotification notification) {
        try {
            String smtpHost = Pastely.getInstance().getConfig().get("email.smtp.host", null);
            if (smtpHost == null || smtpHost.isEmpty()) {
                LOGGER.log(Level.WARNING, "SMTP not configured, skipping email notification");
                return;
            }

            String smtpPort = Pastely.getInstance().getConfig().get("email.smtp.port", "587");
            String smtpUser = Pastely.getInstance().getConfig().get("email.smtp.user", "");
            String smtpPassword = Pastely.getInstance().getConfig().get("email.smtp.password", "");
            String fromAddress = Pastely.getInstance().getConfig().get("email.from.address", "noreply@pastely.app");
            String fromName = Pastely.getInstance().getConfig().get("email.from.name", "Pastely");

            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });

            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(fromAddress, fromName));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            emailMessage.setSubject(notification.getTitle());

            // Build HTML email body
            StringBuilder htmlBody = new StringBuilder();
            htmlBody.append("<html><body>");
            htmlBody.append("<h2>").append(notification.getTitle()).append("</h2>");
            htmlBody.append("<p>").append(notification.getMessage()).append("</p>");

            if (notification.getUrl() != null && !notification.getUrl().isEmpty()) {
                String baseUrl = Pastely.getInstance().getConfig().get("server.name", "http://localhost");
                htmlBody.append("<p><a href=\"").append(baseUrl).append(notification.getUrl())
                        .append("\">View in Pastely</a></p>");
            }

            htmlBody.append("<hr>");
            htmlBody.append("<p style=\"color: #666; font-size: 12px;\">");
            htmlBody.append("You received this email because you have notifications enabled in your Pastely account.");
            htmlBody.append("</p>");
            htmlBody.append("</body></html>");

            emailMessage.setContent(htmlBody.toString(), "text/html; charset=utf-8");

            Transport.send(emailMessage);

            LOGGER.log(Level.INFO, "Email notification sent to " + user.getEmail());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email notification", e);
        }
    }

    /**
     * Send webhook notification
     */
    private static void sendWebhookNotification(String webhookUrl, UserNotification notification) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "notification.created");
            payload.put("notification_id", notification.getId());
            payload.put("type", notification.getType().name());
            payload.put("title", notification.getTitle());
            payload.put("message", notification.getMessage());
            payload.put("url", notification.getUrl());
            payload.put("data", notification.getData());
            payload.put("created_at", notification.getCreatedAt().toString());

            HttpClient client = new HttpClient();
            var response = client
                    .post(webhookUrl)
                    .header("Content-Type", "application/json")
                    .header("X-Pastely-Event", "notification.created")
                    .data(gson.toJson(payload))
                    .execute();

            if (response.status() < 200 || response.status() >= 300) {
                LOGGER.log(Level.WARNING, "Webhook notification failed with status: " + response.status());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send webhook notification", e);
        }
    }

    /**
     * Mark notification as read
     */
    public static void markAsRead(String notificationId) {
        UserNotification notification = Repo.get(UserNotification.class)
                .where("id", notificationId)
                .first();

        if (notification != null) {
            notification.setRead(true);
            notification.save();
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public static void markAllAsRead(String userId) {
        Repo.get(UserNotification.class)
                .where("userId", userId)
                .where("isRead", false)
                .stream()
                .forEach(notification -> {
                    notification.setRead(true);
                    notification.save();
                });
    }

    /**
     * Get unread count for a user
     */
    public static long getUnreadCount(String userId) {
        return Repo.get(UserNotification.class)
                .where("userId", userId)
                .where("isRead", false)
                .count();
    }
}
