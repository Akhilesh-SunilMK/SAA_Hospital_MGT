package com.hms.auth.factory;

import com.hms.auth.dto.RegistrationRequest;
import com.hms.auth.entity.User;
import com.hms.common.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TC-F-01/02/03 analogue for UserFactoryRegistry: resolves the right factory per role, rejects
 * an unregistered role, and proves a brand-new factory bean needs no registry code change.
 */
class UserFactoryRegistryTest {

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void resolvesPatientFactoryForPatientRole() {
        UserFactoryRegistry registry = new UserFactoryRegistry(
                List.of(new PatientUserFactory(), new DoctorUserFactory(), new AdminUserFactory(), new StaffUserFactory()));

        UserFactory resolved = registry.resolve(Role.PATIENT);

        assertThat(resolved).isInstanceOf(PatientUserFactory.class);
    }

    @Test
    void staffFactoryCoversAllFiveOperationalRoles() {
        StaffUserFactory staffFactory = new StaffUserFactory();
        UserFactoryRegistry registry = new UserFactoryRegistry(List.of(staffFactory));

        for (Role role : EnumSet.of(Role.NURSE, Role.RECEPTIONIST, Role.LAB_TECH, Role.PHARMACIST, Role.ACCOUNTANT)) {
            assertThat(registry.resolve(role)).isSameAs(staffFactory);
        }
    }

    @Test
    void throwsForUnregisteredRole() {
        UserFactoryRegistry registry = new UserFactoryRegistry(List.of(new PatientUserFactory()));

        assertThatThrownBy(() -> registry.resolve(Role.DOCTOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DOCTOR");
    }

    @Test
    void addingANewFactoryRequiresNoRegistryCodeChange() {
        // Simulates adding a brand-new role/factory: the registry constructor is the only
        // integration point, and it is driven entirely by supportedRoles() — no branching here.
        UserFactory inlineNewFactory = new UserFactory() {
            @Override
            protected User createUser(RegistrationRequest req, PasswordEncoder encoder) {
                return null;
            }

            @Override
            protected Set<String> defaultPermissions(Role role) {
                return Set.of("CUSTOM_PERMISSION");
            }

            @Override
            public Set<Role> supportedRoles() {
                return EnumSet.of(Role.ADMIN);
            }
        };

        UserFactoryRegistry registry = new UserFactoryRegistry(List.of(inlineNewFactory));

        assertThat(registry.resolve(Role.ADMIN)).isSameAs(inlineNewFactory);
    }

    @Test
    void createAndRegisterAppliesRoleSpecificDefaultPermissions() {
        when(encoder.encode("secret123")).thenReturn("hashed");
        RegistrationRequest req = new RegistrationRequest("jdoe", "jdoe@hms.test", "secret123",
                "John", "Doe", Role.PATIENT);

        User user = new PatientUserFactory().createAndRegister(req, encoder);

        assertThat(user.getPasswordHash()).isEqualTo("hashed");
        assertThat(user.getPermissions()).contains("BOOK_APPOINTMENT", "VIEW_OWN_RECORDS");
    }
}
