package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;

public record EmergencyContactRequest(
        @NotBlank String name,
        @NotBlank String relation,
        @NotBlank String phone
) {
}
