package com.hms.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInMs,
        UserResponse user
) {
}
