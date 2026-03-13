package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.AuthKey;
import cc.allyapps.pastely.model.database.NotificationPreference;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.database.UserNotification;
import cc.allyapps.pastely.model.requests.notification.UpdateNotificationPreferenceRequest;
import cc.allyapps.pastely.model.responses.ActionResponse;
import cc.allyapps.pastely.model.responses.notification.NotificationPreferenceResponse;
import cc.allyapps.pastely.model.responses.notification.NotificationResponse;
import cc.allyapps.pastely.model.responses.notification.UnreadCountResponse;
import cc.allyapps.pastely.services.NotificationService;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationController - Enhanced notification management for Phase 4
 * Handles user notifications and preferences
 */
@PathPrefix("/api/v2/notifications")
public class NotificationController extends HttpController {

    /**
     * Get paginated list of notifications for authenticated user
     */
    @Get
    @With({"auth"})
    public List<NotificationResponse> getNotifications(Exchange exchange,
                                                        @Attrib("user") User user,
                                                        @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:read");
        }

        int page = exchange.getQueryParameters().getInt("page", 0);
        int limit = exchange.getQueryParameters().getInt("limit", 20);

        var query = Repo.get(UserNotification.class)
                .where("userId", user.getId())
                .order("createdAt", true);

        // Filter by read status
        if (exchange.getQueryParameters().has("unread")) {
            query.where("isRead", false);
        }

        // Mark as received
        List<UserNotification> notifications = query
                .limit(limit)
                .offset(page * limit)
                .get();

        notifications.forEach(n -> {
            if (!n.isReceived()) {
                n.setReceived(true);
                n.save();
            }
        });

        return notifications.stream()
                .map(NotificationResponse::create)
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications
     */
    @Get("/unread/count")
    @With({"auth"})
    public UnreadCountResponse getUnreadCount(@Attrib("user") User user,
                                                @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:read");
        }

        long count = NotificationService.getUnreadCount(user.getId());
        return new UnreadCountResponse(count);
    }

    /**
     * Mark a specific notification as read
     */
    @Put("/{id}/read")
    @With({"auth"})
    public ActionResponse markAsRead(@Path("id") String id,
                                      @Attrib("user") User user,
                                      @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:edit");
        }

        UserNotification notification = Repo.get(UserNotification.class)
                .where("id", id)
                .where("userId", user.getId())
                .first();

        if (notification == null) {
            throw new NotFoundException("Notification not found");
        }

        notification.setRead(true);
        notification.save();

        return new ActionResponse(true);
    }

    /**
     * Mark all notifications as read
     */
    @Put("/read-all")
    @With({"auth"})
    public ActionResponse markAllAsRead(@Attrib("user") User user,
                                         @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:edit");
        }

        NotificationService.markAllAsRead(user.getId());

        return new ActionResponse(true);
    }

    /**
     * Delete a specific notification
     */
    @Delete("/{id}")
    @With({"auth"})
    public ActionResponse deleteNotification(@Path("id") String id,
                                              @Attrib("user") User user,
                                              @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:delete");
        }

        UserNotification notification = Repo.get(UserNotification.class)
                .where("id", id)
                .where("userId", user.getId())
                .first();

        if (notification == null) {
            throw new NotFoundException("Notification not found");
        }

        notification.delete();

        return new ActionResponse(true);
    }

    /**
     * Get notification preferences for authenticated user
     */
    @Get("/preferences")
    @With({"auth"})
    public NotificationPreferenceResponse getPreferences(@Attrib("user") User user,
                                                          @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:read");
        }

        NotificationPreference preference = Repo.get(NotificationPreference.class)
                .where("userId", user.getId())
                .first();

        if (preference == null) {
            // Create default preferences
            preference = new NotificationPreference();
            preference.setUserId(user.getId());
            preference.save();
        }

        return NotificationPreferenceResponse.create(preference);
    }

    /**
     * Update notification preferences
     */
    @Put("/preferences")
    @With({"auth"})
    public NotificationPreferenceResponse updatePreferences(@Body UpdateNotificationPreferenceRequest request,
                                                             @Attrib("user") User user,
                                                             @Attrib("authkey") AuthKey authKey) {
        if (authKey != null) {
            authKey.checkPermission("notifications:edit");
        }

        NotificationPreference preference = Repo.get(NotificationPreference.class)
                .where("userId", user.getId())
                .first();

        if (preference == null) {
            preference = new NotificationPreference();
            preference.setUserId(user.getId());
        }

        preference.setEmailEnabled(request.emailEnabled);
        preference.setPushEnabled(request.pushEnabled);
        preference.setWebhookEnabled(request.webhookEnabled);
        preference.setWebhookUrl(request.webhookUrl);
        preference.setTypeFilters(request.typeFilters);

        preference.save();

        return NotificationPreferenceResponse.create(preference);
    }
}
