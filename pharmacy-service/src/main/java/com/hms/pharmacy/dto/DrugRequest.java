package com.hms.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DrugRequest(
        @NotBlank String code,
        @NotBlank String genericName,
        String brandName,
        @NotBlank String form,
        @NotBlank String strength,
        String manufacturer,
        @NotNull @Positive BigDecimal unitPrice,
        Integer reorderLevel
) {
}
