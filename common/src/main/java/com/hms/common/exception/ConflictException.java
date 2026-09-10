package com.hms.common.exception;

/**
 * Raised for HTTP 409 conflicts (e.g. slot already booked). See SRS 6.2.10.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
