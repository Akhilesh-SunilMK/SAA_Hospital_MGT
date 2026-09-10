package com.hms.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RefundRequest(
        @NotNull Long paymentId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String reason
) {
}
