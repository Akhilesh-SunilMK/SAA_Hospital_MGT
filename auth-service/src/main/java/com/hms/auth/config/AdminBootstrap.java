package com.hms.auth.config;

import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.repository.UserRepository;
import com.hms.auth.service.AuthService;
import com.hms.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN account at startup. Public registration refuses the ADMIN role, so
 * without this there would be no way to get an admin into a fresh database. Does nothing unless
 * HMS_ADMIN_EMAIL and HMS_ADMIN_PASSWORD are set, and never touches an existing account.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AuthService authService;
    private final UserRepository userRepository;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(AuthService authService, UserRepository userRepository,
                          @Value("${HMS_ADMIN_USERNAME:admin}") String username,
                          @Value("${HMS_ADMIN_EMAIL:}") String email,
                          @Value("${HMS_ADMIN_PASSWORD:}") String password) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            return;
        }
        authService.register(new RegistrationRequest(username, email, password, "System", "Administrator", Role.ADMIN));
        log.info("Created bootstrap ADMIN account '{}' from HMS_ADMIN_* settings", username);
    }
}
