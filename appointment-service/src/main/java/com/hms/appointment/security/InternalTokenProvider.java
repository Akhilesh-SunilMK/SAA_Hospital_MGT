package com.hms.appointment.security;

import com.hms.common.security.JwtUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mints short-lived internal service-to-service tokens for Saga calls (e.g. to billing-service)
 * where the original caller's role (e.g. PATIENT) would be rejected by the downstream service's
 * own role check, but the orchestrating service itself is a trusted party sharing the same
 * JWT signing secret.
 */
@Component
public class InternalTokenProvider {

    private static final long INTERNAL_TOKEN_TTL_MS = 60_000L;

    private final JwtUtil jwtUtil;

    public InternalTokenProvider(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String bearerToken() {
        String token = jwtUtil.generateToken(
                "appointment-service",
                Map.of("role", "ADMIN", "userId", 0L),
                INTERNAL_TOKEN_TTL_MS);
        return "Bearer " + token;
    }
}
