package com.hms.auth.entity;

import com.hms.common.security.Role;
import jakarta.persistence.CollectionTable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * FR-AU-01/03/04/07: registered user with a fixed role, BCrypt hash, and account-lockout state.
 * Permissions are seeded once at creation time by the {@link com.hms.auth.factory.UserFactory}
 * that built this aggregate (FR-AU-05).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>();

    public User(String username, String email, String passwordHash, String firstName,
                String lastName, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public boolean isCurrentlyLocked() {
        if (!locked) {
            return false;
        }
        if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            // lockout window elapsed — caller (AuthService) is responsible for clearing state
            return false;
        }
        return true;
    }

    public void registerFailedLogin(int maxAttempts, long lockoutMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.locked = true;
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
        }
    }

    public void registerSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.locked = false;
        this.lockedUntil = null;
    }

    public void unlockIfExpired() {
        if (locked && lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            locked = false;
            lockedUntil = null;
            failedLoginAttempts = 0;
        }
    }
}
