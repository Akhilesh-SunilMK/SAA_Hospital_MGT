package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AllergyRequest(
        @NotBlank String allergen,
        @NotBlank String severity,
        @NotNull LocalDate notedOn
) {
}
