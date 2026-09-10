package com.hms.common.exception;

/**
 * Raised for HTTP 422 business-rule violations (e.g. dispensing an expired batch,
 * finalising an already-finalised record). See SRS 6.2.10 status code convention.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
