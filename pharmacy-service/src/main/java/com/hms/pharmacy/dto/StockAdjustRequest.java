package com.hms.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record StockAdjustRequest(
        @NotBlank String batchNo,
        @NotNull @Positive Integer quantity,
        @NotNull LocalDate expiryDate
) {
}
