package com.hms.billing.factory;

import java.math.BigDecimal;

public record PaymentRequest(
        Long invoiceId,
        PaymentMethod method,
        BigDecimal amount,
        String reference // card number / UPI VPA / bank ref / insurance policy no, depending on method
) {
}
