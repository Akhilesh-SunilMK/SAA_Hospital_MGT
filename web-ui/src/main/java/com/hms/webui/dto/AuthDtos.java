package com.hms.webui.dto;

import com.hms.common.security.Role;

import java.util.List;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegistrationRequest(String username, String email, String password,
                                       String firstName, String lastName, Role role) {}

    public record LoginRequest(String usernameOrEmail, String password) {}

    public record RefreshRequest(String refreshToken) {}

    public record ForgotPasswordRequest(String email) {}

    public record UserResponse(Long id, String username, String email, String firstName, String lastName,
                                Role role, List<String> permissions) {}

    public record AuthResponse(String accessToken, String refreshToken, Long expiresInMs, UserResponse user) {}
}
