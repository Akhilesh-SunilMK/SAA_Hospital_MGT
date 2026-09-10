package com.hms.billing.dto;

import com.hms.billing.factory.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequestDto(
        @NotNull Long invoiceId,
        @NotNull PaymentMethod method,
        @NotNull @Positive BigDecimal amount,
        String reference
) {
}
