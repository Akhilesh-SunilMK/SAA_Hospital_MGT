package com.hms.common.web;

import org.slf4j.MDC;

public final class TraceIdSupport {

    private TraceIdSupport() {
    }

    public static String current() {
        String traceId = MDC.get(CorrelationIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "n/a";
    }
}
