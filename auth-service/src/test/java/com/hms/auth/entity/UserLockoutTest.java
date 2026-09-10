package com.hms.auth.entity;

import com.hms.common.security.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** FR-AU-07: lock an account after 5 consecutive failed login attempts for 15 minutes. */
class UserLockoutTest {

    @Test
    void locksAccountOnFifthConsecutiveFailure() {
        User user = new User("jdoe", "jdoe@hms.test", "hash", "John", "Doe", Role.PATIENT);

        for (int i = 0; i < 4; i++) {
            user.registerFailedLogin(5, 15);
            assertThat(user.isCurrentlyLocked()).isFalse();
        }
        user.registerFailedLogin(5, 15);

        assertThat(user.isCurrentlyLocked()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void successfulLoginResetsFailureCounterAndLock() {
        User user = new User("jdoe", "jdoe@hms.test", "hash", "John", "Doe", Role.PATIENT);
        for (int i = 0; i < 5; i++) {
            user.registerFailedLogin(5, 15);
        }
        assertThat(user.isCurrentlyLocked()).isTrue();

        user.registerSuccessfulLogin();

        assertThat(user.isCurrentlyLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void unlockIfExpiredClearsStateOncePastLockoutWindow() {
        User user = new User("jdoe", "jdoe@hms.test", "hash", "John", "Doe", Role.PATIENT);
        for (int i = 0; i < 5; i++) {
            user.registerFailedLogin(5, 15);
        }
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // simulate window having elapsed

        user.unlockIfExpired();

        assertThat(user.isLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();
    }
}
