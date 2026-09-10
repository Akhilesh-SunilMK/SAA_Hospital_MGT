package com.hms.pharmacy.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the right {@link PricingStrategy} for a {@link PatientCategory} — same registry
 * idiom as NotificationSenderFactory (SRS 4.1.1): adding a new category only requires a new
 * {@code @Component} implementing {@link PricingStrategy}, no change here.
 */
@Component
public class PricingStrategyFactory {

    private final Map<PatientCategory, PricingStrategy> registry;

    public PricingStrategyFactory(List<PricingStrategy> strategies) {
        this.registry = strategies.stream()
                .collect(Collectors.toMap(PricingStrategy::getCategory, Function.identity()));
    }

    public PricingStrategy resolve(PatientCategory category) {
        PricingStrategy strategy = registry.get(category);
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy registered for category: " + category);
        }
        return strategy;
    }
}
