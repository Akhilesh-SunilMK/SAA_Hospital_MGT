package com.hms.auth.factory;

import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.entity.User;
import com.hms.common.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Shared onboarding workflow for front-line/operational staff roles. */
@Component
public class StaffUserFactory extends UserFactory {

    private static final Map<Role, Set<String>> PERMISSIONS_BY_ROLE = new EnumMap<>(Role.class);

    static {
        PERMISSIONS_BY_ROLE.put(Role.NURSE, Set.of("RECORD_VITALS", "UPDATE_PATIENT_STATUS"));
        PERMISSIONS_BY_ROLE.put(Role.RECEPTIONIST, Set.of("REGISTER_PATIENT", "MANAGE_FRONT_DESK_QUEUE",
                "BOOK_APPOINTMENT"));
        PERMISSIONS_BY_ROLE.put(Role.LAB_TECH, Set.of("PROCESS_TEST_ORDER", "UPLOAD_RESULT"));
        PERMISSIONS_BY_ROLE.put(Role.PHARMACIST, Set.of("DISPENSE_DRUG", "MANAGE_STOCK"));
        PERMISSIONS_BY_ROLE.put(Role.ACCOUNTANT, Set.of("GENERATE_INVOICE", "RECONCILE_PAYMENT"));
    }

    @Override
    protected User createUser(RegistrationRequest req, PasswordEncoder encoder) {
        return new User(req.username(), req.email(), encoder.encode(req.password()),
                req.firstName(), req.lastName(), req.role());
    }

    @Override
    protected Set<String> defaultPermissions(Role role) {
        return PERMISSIONS_BY_ROLE.getOrDefault(role, Set.of());
    }

    @Override
    public Set<Role> supportedRoles() {
        return EnumSet.copyOf(PERMISSIONS_BY_ROLE.keySet());
    }
}
