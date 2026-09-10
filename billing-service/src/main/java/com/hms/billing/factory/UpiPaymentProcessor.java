package com.hms.billing.factory;

import com.hms.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    private static final Pattern VPA_PATTERN = Pattern.compile("^[\\w.\\-]{2,}@[a-zA-Z]{2,}$");

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        String vpa = request.reference();
        if (vpa == null || !VPA_PATTERN.matcher(vpa).matches()) {
            throw new BusinessRuleException("Invalid UPI VPA: " + vpa);
        }
        return new PaymentResult(true, "UPI-" + vpa, "UPI_RECEIPT", "UPI payment settled instantly");
    }
}
