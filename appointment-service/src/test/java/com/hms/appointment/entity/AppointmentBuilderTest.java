package com.hms.appointment.entity;

import com.hms.appointment.exception.InvalidAppointmentException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** SRS 10.1 pattern-specific test cases TC-B-01..06 for the Appointment Builder. */
class AppointmentBuilderTest {

    private final LocalDateTime futureSlot = LocalDateTime.now().plusDays(1);

    @Test
    void tcB01_buildWithAllMandatoryFields_appliesDefaults() {
        Appointment appointment = Appointment.builder()
                .patientId(1024L)
                .doctorId(58L)
                .slot(futureSlot)
                .type(AppointmentType.FOLLOW_UP)
                .build();

        assertThat(appointment.getPatientId()).isEqualTo(1024L);
        assertThat(appointment.getDoctorId()).isEqualTo(58L);
        assertThat(appointment.getDurationMinutes()).isEqualTo(15);
        assertThat(appointment.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void tcB02_omittingPatientId_throwsNullPointerExceptionWithClearMessage() {
        assertThatThrownBy(() -> Appointment.builder()
                .doctorId(58L)
                .slot(futureSlot)
                .type(AppointmentType.OPD)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("patientId");
    }

    @Test
    void tcB03_pastSlot_throwsInvalidAppointmentException() {
        assertThatThrownBy(() -> Appointment.builder()
                .patientId(1L)
                .doctorId(2L)
                .slot(LocalDateTime.now().minusDays(1))
                .type(AppointmentType.OPD)
                .build())
                .isInstanceOf(InvalidAppointmentException.class);
    }

    @Test
    void tcB04_emergencyAppointmentWithoutExplicitPriority_isCritical() {
        Appointment appointment = Appointment.builder()
                .patientId(1L)
                .doctorId(2L)
                .slot(futureSlot)
                .type(AppointmentType.EMERGENCY)
                .build();

        assertThat(appointment.getPriority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void tcB05_omittingDurationMinutes_defaultsTo15() {
        Appointment appointment = Appointment.builder()
                .patientId(1L)
                .doctorId(2L)
                .slot(futureSlot)
                .type(AppointmentType.TELE)
                .build();

        assertThat(appointment.getDurationMinutes()).isEqualTo(15);
    }

    @Test
    void tcB06_noPublicSettersOnAppointment() {
        Method[] methods = Appointment.class.getMethods();
        boolean hasGenericSetter = Arrays.stream(methods)
                .anyMatch(m -> m.getName().startsWith("set"));
        assertThat(hasGenericSetter)
                .as("Appointment must expose no generic public setters (NFR-12); use intention-revealing methods instead")
                .isFalse();
    }
}
