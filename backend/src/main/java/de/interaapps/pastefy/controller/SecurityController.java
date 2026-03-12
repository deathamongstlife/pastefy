package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.model.database.*;
import de.interaapps.pastefy.model.responses.ActionResponse;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.util.encoders.Hex;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.params.Query;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * SecurityController - Advanced security features for pastes
 * Part of the Advanced Security feature
 */
@PathPrefix("/api/v2/paste/{pasteKey}/security")
public class SecurityController extends HttpController {

    @Post("/access")
    @With({"auth", "auth-login-required-create"})
    public PasteAccess setAccess(
            @Path("pasteKey") String pasteKey,
            @Body("password") String password,
            @Body("ipWhitelist") String ipWhitelist,
            @Body("ipBlacklist") String ipBlacklist,
            @Body("maxViews") Integer maxViews,
            @Body("expiresAt") String expiresAt,
            @Body("requiresAuth") Boolean requiresAuth,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        PasteAccess access = Repo.get(PasteAccess.class)
                .where("pasteId", paste.getKey())
                .first();

        if (access == null) {
            access = new PasteAccess();
            access.setPasteId(paste.getKey());
        }

        // Hash password if provided
        if (password != null && !password.isEmpty()) {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Generate BCrypt hash
            byte[] hash = BCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 12);

            // Store salt + hash together (hex encoded)
            String saltHex = Hex.toHexString(salt);
            String hashHex = Hex.toHexString(hash);
            access.setPasswordHash(saltHex + ":" + hashHex);
        }

        if (ipWhitelist != null) {
            access.setIpWhitelist(ipWhitelist);
        }

        if (ipBlacklist != null) {
            access.setIpBlacklist(ipBlacklist);
        }

        if (maxViews != null) {
            access.setMaxViews(maxViews);
        }

        if (expiresAt != null) {
            access.setExpiresAt(Timestamp.valueOf(expiresAt));
        }

        if (requiresAuth != null) {
            access.setRequiresAuth(requiresAuth);
        }

        access.save();

        return access;
    }

    @Get("/access")
    @With({"auth"})
    public PasteAccess getAccess(@Path("pasteKey") String pasteKey, Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        PasteAccess access = Repo.get(PasteAccess.class)
                .where("pasteId", paste.getKey())
                .first();

        if (access == null) throw new NotFoundException();

        return access;
    }

    @Post("/verify")
    public ActionResponse verifyAccess(
            @Path("pasteKey") String pasteKey,
            @Body("password") String password,
            Exchange exchange
    ) {
        Paste paste = Paste.get(pasteKey);
        if (paste == null) throw new NotFoundException();

        PasteAccess access = Repo.get(PasteAccess.class)
                .where("pasteId", paste.getKey())
                .first();

        if (access == null) {
            return ActionResponse.success();
        }

        String clientIp = exchange.header("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = exchange.ip();
        }

        // Check IP blacklist
        if (access.getIpBlacklist() != null && !access.getIpBlacklist().isEmpty()) {
            List<String> blacklist = Arrays.asList(access.getIpBlacklist().split(","));
            if (blacklist.stream().anyMatch(ip -> clientIp.startsWith(ip.trim()))) {
                logAccess(paste, exchange, "IP_BLACKLISTED", false);
                return ActionResponse.error("Access denied: IP blacklisted");
            }
        }

        // Check IP whitelist
        if (access.getIpWhitelist() != null && !access.getIpWhitelist().isEmpty()) {
            List<String> whitelist = Arrays.asList(access.getIpWhitelist().split(","));
            if (whitelist.stream().noneMatch(ip -> clientIp.startsWith(ip.trim()))) {
                logAccess(paste, exchange, "IP_NOT_WHITELISTED", false);
                return ActionResponse.error("Access denied: IP not whitelisted");
            }
        }

        // Check if expired
        if (access.isExpired()) {
            logAccess(paste, exchange, "EXPIRED", false);
            return ActionResponse.error("Access denied: Paste expired");
        }

        // Check if max views reached
        if (access.hasReachedMaxViews()) {
            logAccess(paste, exchange, "MAX_VIEWS_REACHED", false);
            return ActionResponse.error("Access denied: Maximum views reached");
        }

        // Verify password
        if (access.getPasswordHash() != null && !access.getPasswordHash().isEmpty()) {
            if (password == null || password.isEmpty()) {
                return ActionResponse.error("Password required");
            }

            // Extract salt and hash from stored value
            String[] parts = access.getPasswordHash().split(":");
            if (parts.length != 2) {
                logAccess(paste, exchange, "INVALID_HASH_FORMAT", false);
                return ActionResponse.error("Invalid password configuration");
            }

            byte[] salt = Hex.decode(parts[0]);
            byte[] storedHash = Hex.decode(parts[1]);

            // Generate hash with same salt
            byte[] providedHash = BCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 12);

            // Compare hashes using constant-time comparison
            if (!constantTimeEquals(storedHash, providedHash)) {
                logAccess(paste, exchange, "WRONG_PASSWORD", false);
                return ActionResponse.error("Invalid password");
            }
        }

        // Increment view count
        access.incrementViews();
        access.save();

        logAccess(paste, exchange, "VIEW", true);

        return ActionResponse.success();
    }

    @Get("/logs")
    @With({"auth"})
    public List<AccessLog> getAccessLogs(
            @Path("pasteKey") String pasteKey,
            @Query("limit") Integer limit,
            Exchange exchange
    ) {
        User user = exchange.attrib("user");
        if (user == null) throw new AuthenticationException();

        Paste paste = Paste.getAccessiblePasteOrFail(pasteKey, user);

        if (!Objects.equals(paste.getUserId(), user.getId())) {
            throw new AuthenticationException();
        }

        if (limit == null || limit > 100) {
            limit = 100;
        }

        return Repo.get(AccessLog.class)
                .where("pasteId", paste.getKey())
                .orderBy("accessedAt", true)
                .limit(limit)
                .get();
    }

    private void logAccess(Paste paste, Exchange exchange, String denyReason, boolean granted) {
        AccessLog log = new AccessLog();
        log.setPasteId(paste.getKey());

        User user = exchange.attrib("user");
        if (user != null) {
            log.setUserId(user.getId());
        }

        String clientIp = exchange.header("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = exchange.ip();
        }
        log.setIpAddress(clientIp);

        log.setUserAgent(exchange.header("User-Agent"));
        log.setReferer(exchange.header("Referer"));
        log.setAccessType("VIEW");
        log.setAccessGranted(granted);
        log.setDenyReason(denyReason);

        log.save();
    }

    /**
     * Constant-time comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
