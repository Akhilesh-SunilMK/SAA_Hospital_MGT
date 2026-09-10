package com.hms.billing.factory;

import com.hms.common.exception.BusinessRuleException;

/** Extends the common BusinessRuleException so GlobalExceptionHandler maps it to 422 automatically. */
public class UnsupportedPaymentMethodException extends BusinessRuleException {
    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
