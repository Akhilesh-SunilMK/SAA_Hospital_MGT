package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;

public record AdmitRequest(
        @NotBlank String wardId,
        @NotBlank String bedNo
) {
}
