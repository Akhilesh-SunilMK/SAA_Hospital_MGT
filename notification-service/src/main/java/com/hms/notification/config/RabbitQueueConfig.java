package com.hms.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds one durable queue per event this service consumes to the shared "hms.events" topic
 * exchange (declared by com.hms.common.event.RabbitEventConfig — not redeclared here).
 */
@Configuration
public class RabbitQueueConfig {

    public static final String APPOINTMENT_CONFIRMED_QUEUE = "notification.appointment-confirmed.queue";
    public static final String INVOICE_GENERATED_QUEUE = "notification.invoice-generated.queue";
    public static final String LAB_RESULT_READY_QUEUE = "notification.lab-result-ready.queue";
    public static final String STOCK_BELOW_THRESHOLD_QUEUE = "notification.stock-below-threshold.queue";

    private static final String ROUTING_APPOINTMENT_CONFIRMED = "appointment.confirmed";
    private static final String ROUTING_INVOICE_GENERATED = "invoice.generated";
    private static final String ROUTING_LAB_RESULT_READY = "lab.result.ready";
    private static final String ROUTING_STOCK_BELOW_THRESHOLD = "stock.below.threshold";

    @Bean
    public Queue appointmentConfirmedQueue() {
        return new Queue(APPOINTMENT_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue invoiceGeneratedQueue() {
        return new Queue(INVOICE_GENERATED_QUEUE, true);
    }

    @Bean
    public Queue labResultReadyQueue() {
        return new Queue(LAB_RESULT_READY_QUEUE, true);
    }

    @Bean
    public Queue stockBelowThresholdQueue() {
        return new Queue(STOCK_BELOW_THRESHOLD_QUEUE, true);
    }

    @Bean
    public Binding appointmentConfirmedBinding(Queue appointmentConfirmedQueue, TopicExchange hmsEventsExchange) {
        return BindingBuilder.bind(appointmentConfirmedQueue).to(hmsEventsExchange).with(ROUTING_APPOINTMENT_CONFIRMED);
    }

    @Bean
    public Binding invoiceGeneratedBinding(Queue invoiceGeneratedQueue, TopicExchange hmsEventsExchange) {
        return BindingBuilder.bind(invoiceGeneratedQueue).to(hmsEventsExchange).with(ROUTING_INVOICE_GENERATED);
    }

    @Bean
    public Binding labResultReadyBinding(Queue labResultReadyQueue, TopicExchange hmsEventsExchange) {
        return BindingBuilder.bind(labResultReadyQueue).to(hmsEventsExchange).with(ROUTING_LAB_RESULT_READY);
    }

    @Bean
    public Binding stockBelowThresholdBinding(Queue stockBelowThresholdQueue, TopicExchange hmsEventsExchange) {
        return BindingBuilder.bind(stockBelowThresholdQueue).to(hmsEventsExchange).with(ROUTING_STOCK_BELOW_THRESHOLD);
    }
}
