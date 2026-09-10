package com.hms.billing.factory;

import com.hms.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class InsurancePaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.INSURANCE;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request.reference() == null || request.reference().isBlank()) {
            throw new BusinessRuleException("An insurance policy reference is required");
        }
        return new PaymentResult(true, "INS-" + request.reference(), "INSURANCE_CLAIM_RECEIPT",
                "Insurance claim submitted for settlement (v1.0: sandbox adapter, per A3)");
    }
}
