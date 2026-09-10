package com.hms.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleRequest(@NotNull @Future LocalDateTime newSlot) {
}
