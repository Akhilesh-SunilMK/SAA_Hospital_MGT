package com.hms.billing.factory;

import com.hms.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        String cardRef = request.reference();
        if (cardRef == null || !cardRef.replaceAll("\\s", "").matches("\\d{12,19}")) {
            throw new BusinessRuleException("Invalid card reference/number");
        }
        String masked = "**** **** **** " + cardRef.substring(cardRef.length() - 4);
        return new PaymentResult(true, masked, "CARD_RECEIPT", "Card payment settled");
    }
}
