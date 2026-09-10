package com.hms.emr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PrescriptionRequest(
        @NotNull Long recordId,
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotEmpty List<ItemRequest> items
) {
    public record ItemRequest(
            @NotNull String drugName,
            @NotNull String dosage,
            @NotNull String frequency,
            @NotNull Integer durationDays,
            String instructions
    ) {
    }
}
