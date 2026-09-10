package com.hms.auth.factory;

import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.entity.User;
import com.hms.common.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class DoctorUserFactory extends UserFactory {

    @Override
    protected User createUser(RegistrationRequest req, PasswordEncoder encoder) {
        return new User(req.username(), req.email(), encoder.encode(req.password()),
                req.firstName(), req.lastName(), Role.DOCTOR);
    }

    @Override
    protected Set<String> defaultPermissions(Role role) {
        return Set.of("VIEW_SCHEDULE", "RECORD_DIAGNOSIS", "ORDER_TEST", "ISSUE_PRESCRIPTION",
                "VIEW_PATIENT_HISTORY");
    }

    @Override
    public Set<Role> supportedRoles() {
        return EnumSet.of(Role.DOCTOR);
    }
}
