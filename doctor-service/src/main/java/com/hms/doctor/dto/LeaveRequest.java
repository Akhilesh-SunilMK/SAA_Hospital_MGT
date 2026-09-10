package com.hms.doctor.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        String reason
) {
}
