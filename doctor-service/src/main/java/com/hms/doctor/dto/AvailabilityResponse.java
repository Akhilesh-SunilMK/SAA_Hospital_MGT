package com.hms.doctor.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(Long doctorId, LocalDate date, boolean onLeave, List<LocalTime> availableSlots) {
}
