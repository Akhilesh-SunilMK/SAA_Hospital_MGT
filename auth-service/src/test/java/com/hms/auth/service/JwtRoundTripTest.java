package com.hms.auth.service;

import com.hms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Confirms auth-service issues tokens that decode correctly with role/userId claims intact. */
class JwtRoundTripTest {

    private final JwtUtil jwtUtil = new JwtUtil("test-only-signing-secret-at-least-32-bytes-long!!");

    @Test
    void generatedTokenRoundTripsSubjectRoleAndUserId() {
        String token = jwtUtil.generateToken("jdoe", Map.of("role", "DOCTOR", "userId", 42L), 60_000);

        assertThat(jwtUtil.isValid(token)).isTrue();
        assertThat(jwtUtil.isExpired(token)).isFalse();
        assertThat(jwtUtil.extractSubject(token)).isEqualTo("jdoe");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("DOCTOR");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void expiredTokenIsReportedAsExpired() {
        String token = jwtUtil.generateToken("jdoe", Map.of("role", "PATIENT", "userId", 1L), -1_000);

        assertThat(jwtUtil.isExpired(token)).isTrue();
    }

    @Test
    void tamperedTokenIsInvalid() {
        String token = jwtUtil.generateToken("jdoe", Map.of("role", "PATIENT", "userId", 1L), 60_000);

        assertThat(jwtUtil.isValid(token + "tampered")).isFalse();
    }
}
