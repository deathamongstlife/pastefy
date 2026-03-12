package cc.allyapps.pastely.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.*;
import java.sql.Timestamp;

@Dates
@Table("local_auth")
public class LocalAuth extends Model {
    @Column(size = 8)
    private String userId;

    @Column(size = 255)
    private String email;

    @Column(size = 255)
    private String passwordHash;

    @Column
    private boolean emailVerified = false;

    @Column(size = 64)
    private String verificationToken;

    @Column
    private Timestamp verificationTokenExpiry;

    @Column(size = 64)
    private String resetToken;

    @Column
    private Timestamp resetTokenExpiry;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email.toLowerCase(); }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public Timestamp getVerificationTokenExpiry() { return verificationTokenExpiry; }
    public void setVerificationTokenExpiry(Timestamp verificationTokenExpiry) { this.verificationTokenExpiry = verificationTokenExpiry; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Timestamp getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Timestamp resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() {
        return User.get(userId);
    }
}
