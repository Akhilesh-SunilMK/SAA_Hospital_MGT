package com.hms.appointment.dto;

import com.hms.appointment.entity.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookAppointmentRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotNull @Future LocalDateTime slot,
        @NotNull AppointmentType type,
        String reason,
        Integer durationMinutes,
        Long referredByDoctorId,
        Boolean insuranceClaimed
) {
}
