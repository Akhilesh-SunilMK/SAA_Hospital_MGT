package com.hms.appointment.dto;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long appointmentId,
        String tokenNumber,
        Long patientId,
        Long doctorId,
        String doctorName,
        LocalDateTime slot,
        AppointmentType type,
        Priority priority,
        AppointmentStatus status,
        String reason,
        Integer durationMinutes
) {
    public static AppointmentResponse from(Appointment a, String doctorName) {
        return new AppointmentResponse(
                a.getId(), a.getTokenNumber(), a.getPatientId(), a.getDoctorId(), doctorName,
                a.getSlot(), a.getType(), a.getPriority(), a.getStatus(), a.getReason(), a.getDurationMinutes());
    }
}
