package com.hms.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard response envelope mandated by SRS section 6.2:
 * { success, statusCode, message, data, errors, timestamp, traceId }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        int statusCode,
        String message,
        T data,
        List<String> errors,
        Instant timestamp,
        String traceId
) {

    public static <T> ApiResponse<T> success(int statusCode, String message, T data, String traceId) {
        return new ApiResponse<>(true, statusCode, message, data, null, Instant.now(), traceId);
    }

    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return success(200, "Success", data, traceId);
    }

    public static <T> ApiResponse<T> created(T data, String message, String traceId) {
        return success(201, message, data, traceId);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, List<String> errors, String traceId) {
        return new ApiResponse<>(false, statusCode, message, null, errors, Instant.now(), traceId);
    }
}
