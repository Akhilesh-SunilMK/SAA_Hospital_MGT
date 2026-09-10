package com.hms.billing.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Method (SRS 4.1.3, FR-BL-05). Same registry idiom as NotificationSenderFactory
 * (SRS 4.1.1): adding a new PaymentMethod only requires a new {@code @Component} implementing
 * {@link PaymentProcessor} — no change here or in the calling service.
 */
@Component
public class PaymentProcessorFactory {

    private final Map<PaymentMethod, PaymentProcessor> registry;

    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        this.registry = processors.stream()
                .collect(Collectors.toMap(PaymentProcessor::getMethod, Function.identity()));
    }

    public PaymentProcessor getProcessor(PaymentMethod method) {
        PaymentProcessor processor = registry.get(method);
        if (processor == null) {
            throw new UnsupportedPaymentMethodException("No payment processor registered for method: " + method);
        }
        return processor;
    }
}
