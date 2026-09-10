package com.hms.notification.exception;

import com.hms.common.exception.BusinessRuleException;

/**
 * SRS 4.1.1 / TC-F-02. Extends the common BusinessRuleException so GlobalExceptionHandler
 * (already on the component scan) maps it to 422 without a service-local @ExceptionHandler.
 */
public class UnsupportedChannelException extends BusinessRuleException {
    public UnsupportedChannelException(String message) {
        super(message);
    }
}
