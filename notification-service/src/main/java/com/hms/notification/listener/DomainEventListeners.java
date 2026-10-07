package com.hms.notification.listener;

import com.hms.common.event.DomainEvent;
import com.hms.common.security.PatientIdentityResolver;
import com.hms.notification.config.RabbitQueueConfig;
import com.hms.notification.model.ChannelType;
import com.hms.notification.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes the four async, event-driven flows in SRS 3.3. Each handler is defensive: a
 * dispatch failure (e.g. a downstream template missing) is logged, not rethrown, so a single
 * bad event doesn't repeatedly requeue and block the listener container.
 */
@Component
public class DomainEventListeners {

    private static final Logger log = LoggerFactory.getLogger(DomainEventListeners.class);

    /** Placeholder recipient for events with no specific patient/user target (e.g. stock alerts). */
    private static final long PHARMACY_ADMIN_USER_ID = 0L;

    private final NotificationDispatchService dispatchService;
    private final PatientIdentityResolver patientIdentityResolver;

    public DomainEventListeners(NotificationDispatchService dispatchService,
                                PatientIdentityResolver patientIdentityResolver) {
        this.dispatchService = dispatchService;
        this.patientIdentityResolver = patientIdentityResolver;
    }

    @RabbitListener(queues = RabbitQueueConfig.APPOINTMENT_CONFIRMED_QUEUE)
    public void onAppointmentConfirmed(DomainEvent event) {
        Map<String, Object> payload = event.payload();
        Long patientId = toLong(payload.get("patientId"));
        Map<String, String> variables = Map.of(
                "tokenNumber", str(payload.get("tokenNumber")),
                "slot", str(payload.get("slot"))
        );
        dispatchToPatient(patientId, ChannelType.EMAIL, "APPOINTMENT_CONFIRMED", variables, event);
    }

    @RabbitListener(queues = RabbitQueueConfig.INVOICE_GENERATED_QUEUE)
    public void onInvoiceGenerated(DomainEvent event) {
        Map<String, Object> payload = event.payload();
        Long patientId = toLong(payload.get("patientId"));
        Map<String, String> variables = Map.of(
                "invoiceNo", str(payload.get("invoiceNo")),
                "amount", str(payload.get("amount"))
        );
        dispatchToPatient(patientId, ChannelType.EMAIL, "INVOICE_GENERATED", variables, event);
    }

    @RabbitListener(queues = RabbitQueueConfig.LAB_RESULT_READY_QUEUE)
    public void onLabResultReady(DomainEvent event) {
        Map<String, Object> payload = event.payload();
        Long patientId = toLong(payload.get("patientId"));
        Map<String, String> variables = Map.of(
                "orderId", str(payload.get("orderId"))
        );
        dispatchToPatient(patientId, ChannelType.EMAIL, "LAB_RESULT_READY", variables, event);
    }

    @RabbitListener(queues = RabbitQueueConfig.STOCK_BELOW_THRESHOLD_QUEUE)
    public void onStockBelowThreshold(DomainEvent event) {
        Map<String, Object> payload = event.payload();
        Map<String, String> variables = Map.of(
                "drugName", str(payload.get("drugName")),
                "currentQuantity", str(payload.get("currentQuantity")),
                "reorderLevel", str(payload.get("reorderLevel"))
        );
        safeDispatch(PHARMACY_ADMIN_USER_ID, ChannelType.EMAIL, "STOCK_LOW", variables, event);
    }

    /**
     * Events carry the patient-service patientId, but notifications (delivery log, channel
     * preferences) are keyed by the auth userId, so map one to the other first.
     */
    private void dispatchToPatient(Long patientId, ChannelType channel, String templateCode,
                                   Map<String, String> variables, DomainEvent event) {
        Long userId = patientIdentityResolver.userIdForPatient(patientId);
        if (userId == null) {
            log.info("Skipping {} for patient {}: no linked user account (traceId={})",
                    templateCode, patientId, event.traceId());
            return;
        }
        safeDispatch(userId, channel, templateCode, variables, event);
    }

    private void safeDispatch(Long userId, ChannelType channel, String templateCode,
                               Map<String, String> variables, DomainEvent event) {
        try {
            dispatchService.dispatch(userId, channel, templateCode, variables);
        } catch (Exception ex) {
            log.error("Failed to dispatch notification for event '{}' (traceId={}): {}",
                    event.eventType(), event.traceId(), ex.getMessage(), ex);
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
