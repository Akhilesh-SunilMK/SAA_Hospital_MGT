package com.hms.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Mirrors doctor-service's ApiResponse<AvailabilityResponse> payload exactly (see
 * doctor-service's com.hms.doctor.dto.AvailabilityResponse). */
public record DoctorAvailabilityResponse(
        boolean success,
        int statusCode,
        String message,
        AvailabilityData data
) {
    public record AvailabilityData(Long doctorId, LocalDate date, boolean onLeave, List<LocalTime> availableSlots) {
    }
}
