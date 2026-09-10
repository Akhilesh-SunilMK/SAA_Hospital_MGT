package com.hms.billing.factory;

public record PaymentResult(
        boolean success,
        String transactionRef,
        String receiptFormat,
        String message
) {
}
