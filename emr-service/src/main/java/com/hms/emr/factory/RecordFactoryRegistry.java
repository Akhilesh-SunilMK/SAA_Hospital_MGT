package com.hms.emr.factory;

import com.hms.emr.entity.RecordType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves the right {@link RecordFactory} by {@link RecordType}. Adding a new record type
 * requires only a new {@code @Component} implementing RecordFactory — no change here (mirrors
 * NotificationSenderFactory's registry idiom, SRS 4.1.1).
 */
@Component
public class RecordFactoryRegistry {

    private final Map<RecordType, RecordFactory> registry;

    public RecordFactoryRegistry(List<RecordFactory> factories) {
        this.registry = factories.stream()
                .collect(Collectors.toMap(RecordFactory::getType, f -> f));
    }

    public RecordFactory resolve(RecordType type) {
        RecordFactory factory = registry.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No RecordFactory registered for type: " + type);
        }
        return factory;
    }
}
