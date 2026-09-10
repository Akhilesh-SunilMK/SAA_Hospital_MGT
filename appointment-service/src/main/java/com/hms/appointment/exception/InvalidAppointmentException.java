package com.hms.appointment.exception;

import com.hms.common.exception.BusinessRuleException;

/**
 * Raised by Appointment.Builder#build() when an invariant is violated (e.g. a past slot).
 * Extends the common BusinessRuleException so GlobalExceptionHandler maps it to 422 without
 * needing a service-local @ExceptionHandler.
 */
public class InvalidAppointmentException extends BusinessRuleException {
    public InvalidAppointmentException(String message) {
        super(message);
    }
}
