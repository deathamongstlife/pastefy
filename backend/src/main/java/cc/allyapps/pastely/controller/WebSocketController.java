package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.responses.ActionResponse;
import cc.allyapps.pastely.websocket.CollaborationWebSocketHandler;
import org.javawebstack.http.router.annotation.With;
import org.javawebstack.http.router.annotation.params.Attrib;
import org.javawebstack.http.router.annotation.verbs.Get;
import org.javawebstack.http.router.annotation.PathPrefix;

import java.util.HashMap;
import java.util.Map;

@PathPrefix("/api/v2/websocket")
public class WebSocketController extends HttpController {

    @Get("/stats")
    @With({"auth"})
    public Map<String, Object> getStats(@Attrib("user") User user) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_rooms", CollaborationWebSocketHandler.getActiveRoomCount());
        stats.put("total_users", CollaborationWebSocketHandler.getTotalUserCount());
        stats.put("rooms", CollaborationWebSocketHandler.getRooms().entrySet().stream()
            .map(entry -> {
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("session_id", entry.getKey());
                roomInfo.put("paste_id", entry.getValue().getPasteId());
                roomInfo.put("user_count", entry.getValue().getUserCount());
                roomInfo.put("version", entry.getValue().getCurrentVersion());
                roomInfo.put("created_at", entry.getValue().getCreatedAt());
                roomInfo.put("last_activity", entry.getValue().getLastActivityAt());
                return roomInfo;
            })
            .toList()
        );
        return stats;
    }

    @Get("/health")
    public ActionResponse health() {
        return new ActionResponse(true);
    }
}
