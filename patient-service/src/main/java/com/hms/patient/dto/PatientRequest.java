package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Past LocalDate dob,
        @NotBlank String gender,
        @NotBlank String phone,
        String email,
        String address,
        String bloodGroup,
        String allergiesSummary,
        String chronicConditions,
        Long userId
) {
}
