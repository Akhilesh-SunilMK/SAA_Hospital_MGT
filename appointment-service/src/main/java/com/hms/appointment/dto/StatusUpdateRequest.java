package com.hms.appointment.dto;

import com.hms.appointment.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull AppointmentStatus status) {
}
