package com.hms.auth.service;

import com.hms.auth.dto.AuthResponse;
import com.hms.auth.dto.ForgotPasswordRequest;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.RefreshRequest;
import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.dto.UserResponse;
import com.hms.auth.entity.PasswordResetToken;
import com.hms.auth.entity.RefreshToken;
import com.hms.auth.entity.User;
import com.hms.auth.factory.UserFactoryRegistry;
import com.hms.auth.repository.PasswordResetTokenRepository;
import com.hms.auth.repository.RefreshTokenRepository;
import com.hms.auth.repository.UserRepository;
import com.hms.common.exception.ConflictException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.common.security.JwtUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FR-AU-01..08: registration (delegated to {@link UserFactoryRegistry}), authentication,
 * token issue/refresh/revocation, account lockout, and password reset.
 */
@Service
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15;
    private static final long PASSWORD_RESET_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserFactoryRegistry userFactoryRegistry;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        UserFactoryRegistry userFactoryRegistry,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                        @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userFactoryRegistry = userFactoryRegistry;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public UserResponse register(RegistrationRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        User user = userFactoryRegistry.resolve(req.role()).createAndRegister(req, passwordEncoder);
        user = userRepository.save(user);
        log.info("Registered new {} user '{}' (id={})", user.getRole(), user.getUsername(), user.getId());
        return UserResponse.from(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsernameOrEmail(req.usernameOrEmail(), req.usernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        user.unlockIfExpired();
        if (user.isCurrentlyLocked()) {
            throw new UnauthorizedException("Account is locked. Try again after "
                    + LOCKOUT_MINUTES + " minutes.");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            user.registerFailedLogin(MAX_FAILED_ATTEMPTS, LOCKOUT_MINUTES);
            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.registerSuccessfulLogin();
        userRepository.save(user);

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        String hash = TokenHasher.sha256(req.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!stored.isUsable()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokenPair(stored.getUser());
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        List<RefreshToken> active = refreshTokenRepository.findByUserAndRevokedFalse(user);
        active.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(active);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(user -> {
            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken token = new PasswordResetToken(user, TokenHasher.sha256(rawToken),
                    LocalDateTime.now().plusMinutes(PASSWORD_RESET_EXPIRY_MINUTES));
            passwordResetTokenRepository.save(token);
            // No SMTP relay is provisioned in this environment (SRS A2) — log instead of sending.
            log.info("Password reset token for {}: {} (expires in {} min)",
                    user.getEmail(), rawToken, PASSWORD_RESET_EXPIRY_MINUTES);
        });
        // Always the same outward response, whether or not the email exists (NFR-25 / no account enumeration).
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return UserResponse.from(user);
    }

    private AuthResponse issueTokenPair(User user) {
        Map<String, Object> claims = Map.of("role", user.getRole().name(), "userId", user.getId());
        String accessToken = jwtUtil.generateToken(user.getUsername(), claims, accessTokenExpirationMs);

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(user, TokenHasher.sha256(rawRefreshToken),
                LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, accessTokenExpirationMs, UserResponse.from(user));
    }
}
