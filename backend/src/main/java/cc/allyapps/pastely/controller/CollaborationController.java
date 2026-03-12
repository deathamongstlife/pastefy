package cc.allyapps.pastely.controller;

import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;
import cc.allyapps.pastely.model.database.CollaborationSession;
import cc.allyapps.pastely.model.database.CollaborationCursor;
import cc.allyapps.pastely.model.database.Paste;
import cc.allyapps.pastely.model.database.User;
import cc.allyapps.pastely.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * CollaborationController - Real-time collaboration sessions
 * Part of the Real-time Collaboration feature
 */
@PathPrefix("/api/v2/collaboration")
public class CollaborationController extends HttpController {

    @Post("/sessions")
    @With({"auth", "auth-login-required-create"})
    public CollaborationSession createSession(
            @Body("pasteKey") String pasteKey,
            @Body("maxParticipants") Integer maxParticipants,
            @Body("expiresInHours") Integer expiresInHours,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        CollaborationSession session = new CollaborationSession();
        session.setPasteId(paste.getKey());
        session.setOwnerId(user.getId());

        if (maxParticipants != null) {
            session.setMaxParticipants(maxParticipants);
        }

        if (expiresInHours != null) {
            session.setExpiresAt(Timestamp.from(Instant.now().plus(expiresInHours, ChronoUnit.HOURS)));
        }

        session.save();

        return session;
    }

    @Get("/sessions/{sessionId}")
    public CollaborationSession getSession(@Path("sessionId") String sessionId) {
        CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("id", sessionId)
                .first();

        if (session == null) throw new NotFoundException();

        return session;
    }

    @Get("/sessions/token/{sessionToken}")
    public CollaborationSession getSessionByToken(@Path("sessionToken") String sessionToken) {
        CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("sessionToken", sessionToken)
                .where("isActive", true)
                .first();

        if (session == null) throw new NotFoundException();

        // Check if expired
        if (session.getExpiresAt() != null && session.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            session.setActive(false);
            session.save();
            throw new NotFoundException();
        }

        return session;
    }

    @Delete("/sessions/{sessionId}")
    @With({"auth"})
    public ActionResponse closeSession(@Path("sessionId") String sessionId, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("id", sessionId)
                .first();

        if (session == null) throw new NotFoundException();

        if (!Objects.equals(session.getOwnerId(), user.getId())) {
            throw new AuthenticationException();
        }

        session.setActive(false);
        session.save();

        // Clean up cursors
        Repo.get(CollaborationCursor.class)
                .where("sessionId", sessionId)
                .delete();

        return ActionResponse.success();
    }

    @Get("/sessions/{sessionId}/cursors")
    public List<CollaborationCursor> getCursors(@Path("sessionId") String sessionId) {
        CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("id", sessionId)
                .first();

        if (session == null) throw new NotFoundException();

        return Repo.get(CollaborationCursor.class)
                .where("sessionId", sessionId)
                .get();
    }

    @Post("/sessions/{sessionId}/cursors")
    public CollaborationCursor updateCursor(
            @Path("sessionId") String sessionId,
            @Body("userId") String userId,
            @Body("userDisplayName") String userDisplayName,
            @Body("userColor") String userColor,
            @Body("cursorLine") Integer cursorLine,
            @Body("cursorColumn") Integer cursorColumn,
            @Body("selectionStartLine") Integer selectionStartLine,
            @Body("selectionStartColumn") Integer selectionStartColumn,
            @Body("selectionEndLine") Integer selectionEndLine,
            @Body("selectionEndColumn") Integer selectionEndColumn
    ) {
        CollaborationSession session = Repo.get(CollaborationSession.class)
                .where("id", sessionId)
                .first();

        if (session == null) throw new NotFoundException();

        // Update session activity
        session.setLastActivityAt(Timestamp.from(Instant.now()));
        session.save();

        // Find or create cursor for this user
        CollaborationCursor cursor = Repo.get(CollaborationCursor.class)
                .where("sessionId", sessionId)
                .where("userId", userId)
                .first();

        if (cursor == null) {
            cursor = new CollaborationCursor();
            cursor.setSessionId(sessionId);
            cursor.setUserId(userId);
        }

        cursor.setUserDisplayName(userDisplayName);
        cursor.setUserColor(userColor);
        cursor.setCursorLine(cursorLine);
        cursor.setCursorColumn(cursorColumn);
        cursor.setSelectionStartLine(selectionStartLine);
        cursor.setSelectionStartColumn(selectionStartColumn);
        cursor.setSelectionEndLine(selectionEndLine);
        cursor.setSelectionEndColumn(selectionEndColumn);
        cursor.setLastSeenAt(Timestamp.from(Instant.now()));

        cursor.save();

        return cursor;
    }
}
