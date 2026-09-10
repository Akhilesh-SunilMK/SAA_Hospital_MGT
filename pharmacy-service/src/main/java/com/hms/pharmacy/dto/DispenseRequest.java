package com.hms.pharmacy.dto;

import com.hms.pharmacy.strategy.PatientCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DispenseRequest(
        @NotNull Long patientId,
        Long prescriptionId,
        PatientCategory patientCategory,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(@NotNull Long drugId, @NotNull Integer quantity) {
    }

    public PatientCategory categoryOrDefault() {
        return patientCategory != null ? patientCategory : PatientCategory.GENERAL;
    }
}
