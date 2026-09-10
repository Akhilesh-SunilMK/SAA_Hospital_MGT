package com.hms.billing.factory;

import com.hms.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.NET_BANKING;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request.reference() == null || request.reference().isBlank()) {
            throw new BusinessRuleException("A bank transaction reference is required");
        }
        return new PaymentResult(true, "NB-" + request.reference(), "NET_BANKING_RECEIPT",
                "Net banking payment settled");
    }
}
