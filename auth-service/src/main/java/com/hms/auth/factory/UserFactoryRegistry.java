package com.hms.auth.factory;

import com.hms.common.security.Role;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the role-specific {@link UserFactory} at registration time — the same
 * inject-a-List-and-index-by-key idiom as {@code NotificationSenderFactory} (SRS 4.1.1).
 * Adding a new role/factory only means adding a new {@code @Component} UserFactory bean; this
 * class never changes (Open/Closed — FR-AU-05).
 */
@Component
public class UserFactoryRegistry {

    private final Map<Role, UserFactory> registry = new EnumMap<>(Role.class);

    public UserFactoryRegistry(List<UserFactory> factories) {
        for (UserFactory factory : factories) {
            for (Role role : factory.supportedRoles()) {
                registry.put(role, factory);
            }
        }
    }

    public UserFactory resolve(Role role) {
        UserFactory factory = registry.get(role);
        if (factory == null) {
            throw new IllegalArgumentException("No UserFactory registered for role: " + role);
        }
        return factory;
    }
}
