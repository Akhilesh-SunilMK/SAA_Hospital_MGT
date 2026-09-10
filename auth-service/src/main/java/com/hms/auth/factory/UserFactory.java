package com.hms.auth.factory;

import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.entity.User;
import com.hms.common.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Factory Method pattern (SRS 4.1.2, FR-AU-05): role-specific user aggregates are created by
 * subclasses/registry, never by a conditional in the calling service. {@link #createAndRegister}
 * is the template method invoking the factory method {@link #createUser}.
 *
 * <p>{@link #supportedRoles()} (rather than a single {@code role()}) lets one factory serve a
 * family of related roles that share an onboarding workflow but differ in default permissions
 * (see {@link com.hms.auth.factory.StaffUserFactory}, which covers NURSE, RECEPTIONIST,
 * LAB_TECH, PHARMACIST and ACCOUNTANT) — the pattern intent (Open/Closed: a new role/factory
 * requires no change to {@link UserFactoryRegistry}) is unchanged.
 */
public abstract class UserFactory {

    public final User createAndRegister(RegistrationRequest req, PasswordEncoder encoder) {
        User user = createUser(req, encoder);          // factory method
        user.setPermissions(defaultPermissions(req.role()));
        return user;
    }

    protected abstract User createUser(RegistrationRequest req, PasswordEncoder encoder);

    protected abstract Set<String> defaultPermissions(Role role);

    public abstract Set<Role> supportedRoles();
}
