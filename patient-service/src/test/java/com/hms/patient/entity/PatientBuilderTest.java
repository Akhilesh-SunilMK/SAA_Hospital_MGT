package com.hms.patient.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientBuilderTest {

    private Patient.Builder validBuilder() {
        return Patient.builder()
                .mrn("MRN-20260101-0001")
                .firstName("Asha")
                .lastName("Verma")
                .dob(LocalDate.of(1990, 5, 20))
                .gender("FEMALE")
                .phone("9876543210");
    }

    @Test
    void buildsWithAllMandatoryFields_defaultsApplied() {
        Patient patient = validBuilder().build();

        assertThat(patient.getMrn()).isEqualTo("MRN-20260101-0001");
        assertThat(patient.isDeleted()).isFalse();
        assertThat(patient.getCreatedAt()).isNotNull();
        assertThat(patient.getBloodGroup()).isNull();
    }

    @Test
    void missingMrn_throwsNullPointerException() {
        assertThatThrownBy(() -> Patient.builder()
                .firstName("Asha").lastName("Verma")
                .dob(LocalDate.of(1990, 5, 20)).gender("FEMALE").phone("9876543210")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("mrn is mandatory");
    }

    @Test
    void missingFirstName_throwsNullPointerException() {
        assertThatThrownBy(() -> Patient.builder()
                .mrn("MRN-1").lastName("Verma")
                .dob(LocalDate.of(1990, 5, 20)).gender("FEMALE").phone("9876543210")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("firstName is mandatory");
    }

    @Test
    void futureDob_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> validBuilder().dob(LocalDate.now().plusDays(1)).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void optionalFieldsCanBeSet() {
        Patient patient = validBuilder()
                .email("asha@example.com")
                .bloodGroup("O+")
                .allergiesSummary("Penicillin")
                .build();

        assertThat(patient.getEmail()).isEqualTo("asha@example.com");
        assertThat(patient.getBloodGroup()).isEqualTo("O+");
        assertThat(patient.getAllergiesSummary()).isEqualTo("Penicillin");
    }
}
