package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;

public record PatientUpdateRequest(
        @NotBlank String phone,
        String email,
        String address,
        String bloodGroup,
        String allergiesSummary,
        String chronicConditions
) {
}
