package com.hms.common.event;

import com.hms.common.web.TraceIdSupport;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnClass(RabbitTemplate.class)
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * @param eventType routing key, e.g. "appointment.confirmed" — see SRS 3.3 for the canonical
     *                  event names (AppointmentConfirmedEvent, InvoiceGeneratedEvent,
     *                  LabResultReadyEvent, StockBelowThresholdEvent).
     */
    public void publish(String eventType, Map<String, Object> payload) {
        DomainEvent event = DomainEvent.of(eventType, TraceIdSupport.current(), payload);
        rabbitTemplate.convertAndSend(RabbitEventConfig.EXCHANGE_NAME, eventType, event);
    }
}
