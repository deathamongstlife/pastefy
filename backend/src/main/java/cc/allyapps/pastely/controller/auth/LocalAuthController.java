package cc.allyapps.pastely.controller.auth;

import cc.allyapps.pastely.controller.HttpController;
import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.HTTPException;
import cc.allyapps.pastely.helper.PasswordHasher;
import cc.allyapps.pastely.model.database.*;
import cc.allyapps.pastely.model.requests.*;
import cc.allyapps.pastely.model.responses.*;
import org.javawebstack.httprouter.handler.RequestHandler;
import org.javawebstack.httprouter.router.annotation.*;
import org.javawebstack.orm.Repo;
import org.javawebstack.webutils.util.RandomUtil;
import java.sql.Timestamp;
import java.time.Instant;

@PathPrefix("/api/v2/auth/local")
public class LocalAuthController extends HttpController {

    @Post("/register")
    @With({"rate-limiter"})
    public AuthResponse register(@Body RegisterRequest request) {
        // Check if email already exists
        LocalAuth existingAuth = Repo.get(LocalAuth.class)
            .where("email", request.email.toLowerCase())
            .first();

        if (existingAuth != null) {
            throw new HTTPException(400, "Email already registered");
        }

        // Check if username already exists
        User existingUser = Repo.get(User.class)
            .where("name", request.username)
            .first();

        if (existingUser != null) {
            throw new HTTPException(400, "Username already taken");
        }

        // Create user
        User user = new User();
        user.setName(request.username);
        user.setType(User.Type.USER);
        user.save();

        // Create local auth
        LocalAuth localAuth = new LocalAuth();
        localAuth.setUserId(user.getId());
        localAuth.setEmail(request.email);
        localAuth.setPasswordHash(PasswordHasher.hash(request.password));
        localAuth.setEmailVerified(false);

        // Generate verification token
        String verificationToken = RandomUtil.string(64);
        localAuth.setVerificationToken(verificationToken);
        localAuth.setVerificationTokenExpiry(
            Timestamp.from(Instant.now().plusSeconds(86400)) // 24 hours
        );

        localAuth.save();

        // Create auth key
        AuthKey authKey = new AuthKey();
        authKey.setUserId(user.getId());
        authKey.setName("Registration");
        authKey.save();

        // TODO: Send verification email

        return AuthResponse.create(authKey.getKey(), user);
    }

    @Post("/login")
    @With({"rate-limiter"})
    public AuthResponse login(@Body LoginRequest request) {
        // Find user by email
        LocalAuth localAuth = Repo.get(LocalAuth.class)
            .where("email", request.email.toLowerCase())
            .first();

        if (localAuth == null) {
            throw new AuthenticationException();
        }

        // Verify password
        if (!PasswordHasher.verify(request.password, localAuth.getPasswordHash())) {
            throw new AuthenticationException();
        }

        User user = localAuth.getUser();
        if (user == null) {
            throw new AuthenticationException();
        }

        // Create new auth key
        AuthKey authKey = new AuthKey();
        authKey.setUserId(user.getId());
        authKey.setName("Login");
        authKey.save();

        return AuthResponse.create(authKey.getKey(), user);
    }

    @Post("/verify-email")
    public ActionResponse verifyEmail(@Query("token") String token) {
        LocalAuth localAuth = Repo.get(LocalAuth.class)
            .where("verificationToken", token)
            .first();

        if (localAuth == null) {
            throw new HTTPException(400, "Invalid verification token");
        }

        if (localAuth.getVerificationTokenExpiry().before(Timestamp.from(Instant.now()))) {
            throw new HTTPException(400, "Verification token expired");
        }

        localAuth.setEmailVerified(true);
        localAuth.setVerificationToken(null);
        localAuth.setVerificationTokenExpiry(null);
        localAuth.save();

        return new ActionResponse(true);
    }

    @Post("/forgot-password")
    @With({"rate-limiter"})
    public ActionResponse forgotPassword(@Body ForgotPasswordRequest request) {
        LocalAuth localAuth = Repo.get(LocalAuth.class)
            .where("email", request.email.toLowerCase())
            .first();

        if (localAuth == null) {
            // Don't reveal if email exists
            return new ActionResponse(true);
        }

        // Generate reset token
        String resetToken = RandomUtil.string(64);
        localAuth.setResetToken(resetToken);
        localAuth.setResetTokenExpiry(
            Timestamp.from(Instant.now().plusSeconds(3600)) // 1 hour
        );
        localAuth.save();

        // TODO: Send password reset email

        return new ActionResponse(true);
    }

    @Post("/reset-password")
    @With({"rate-limiter"})
    public ActionResponse resetPassword(@Body ResetPasswordRequest request) {
        LocalAuth localAuth = Repo.get(LocalAuth.class)
            .where("resetToken", request.token)
            .first();

        if (localAuth == null) {
            throw new HTTPException(400, "Invalid reset token");
        }

        if (localAuth.getResetTokenExpiry().before(Timestamp.from(Instant.now()))) {
            throw new HTTPException(400, "Reset token expired");
        }

        localAuth.setPasswordHash(PasswordHasher.hash(request.newPassword));
        localAuth.setResetToken(null);
        localAuth.setResetTokenExpiry(null);
        localAuth.save();

        return new ActionResponse(true);
    }

    @Post("/change-password")
    @With({"auth"})
    public ActionResponse changePassword(@Body ChangePasswordRequest request, @Attrib("user") User user) {
        LocalAuth localAuth = Repo.get(LocalAuth.class)
            .where("userId", user.getId())
            .first();

        if (localAuth == null) {
            throw new HTTPException(400, "No local authentication set up");
        }

        if (!PasswordHasher.verify(request.currentPassword, localAuth.getPasswordHash())) {
            throw new HTTPException(400, "Current password incorrect");
        }

        localAuth.setPasswordHash(PasswordHasher.hash(request.newPassword));
        localAuth.save();

        return new ActionResponse(true);
    }
}
