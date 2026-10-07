package com.hms.webui.dto;

import java.time.LocalDateTime;

public final class AppointmentDtos {
    private AppointmentDtos() {}

    public record BookAppointmentRequest(Long patientId, Long doctorId, LocalDateTime slot, String type,
                                          String reason, Integer durationMinutes, Long referredByDoctorId,
                                          Boolean insuranceClaimed) {}

    public record AppointmentResponse(Long appointmentId, String tokenNumber, Long patientId, Long doctorId,
                                       String doctorName, String slot, String type, String priority,
                                       String status, String reason, Integer durationMinutes) {}

    public record RescheduleRequest(LocalDateTime newSlot) {}

    public record StatusUpdateRequest(String status) {}
}
