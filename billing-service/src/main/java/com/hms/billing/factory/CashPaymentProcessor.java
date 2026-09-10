package com.hms.billing.factory;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CashPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        String ref = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, ref, "CASH_RECEIPT", "Cash payment recorded");
    }
}
