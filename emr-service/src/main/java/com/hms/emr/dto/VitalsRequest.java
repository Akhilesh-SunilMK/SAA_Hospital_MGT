package com.hms.emr.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VitalsRequest(
        @NotNull Long patientId,
        Long recordId,
        Integer bpSystolic,
        Integer bpDiastolic,
        Integer pulse,
        BigDecimal temperature,
        Integer spo2,
        BigDecimal heightCm,
        BigDecimal weightKg
) {
}
