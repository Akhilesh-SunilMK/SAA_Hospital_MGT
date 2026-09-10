package com.hms.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DoctorRequest(
        @NotNull Long userId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String registrationNo,
        @NotBlank String qualification,
        @NotBlank String specialisation,
        @NotBlank String department,
        @NotNull @Positive BigDecimal consultationFee
) {
}
