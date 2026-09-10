package com.hms.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Generic envelope for the async, event-driven flows in SRS 3.3 (AppointmentConfirmedEvent,
 * InvoiceGeneratedEvent, LabResultReadyEvent, StockBelowThresholdEvent). Published to the
 * "hms.events" topic exchange with routing key equal to {@link #eventType()} (e.g.
 * "appointment.confirmed") so consumers bind only to the events they care about.
 */
public record DomainEvent(
        String eventType,
        String traceId,
        Instant occurredAt,
        Map<String, Object> payload
) implements Serializable {

    public static DomainEvent of(String eventType, String traceId, Map<String, Object> payload) {
        return new DomainEvent(eventType, traceId, Instant.now(), payload);
    }
}
