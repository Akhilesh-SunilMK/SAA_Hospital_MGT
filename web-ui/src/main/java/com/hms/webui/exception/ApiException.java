package com.hms.webui.exception;

import java.util.List;

/** Raised by ApiClient whenever the gateway/backend returns a non-2xx or {success:false} envelope. */
public class ApiException extends RuntimeException {

    private final int statusCode;
    private final List<String> errors;

    public ApiException(int statusCode, String message, List<String> errors) {
        super(message);
        this.statusCode = statusCode;
        this.errors = errors == null ? List.of() : errors;
    }

    public int statusCode() { return statusCode; }
    public List<String> errors() { return errors; }
}
