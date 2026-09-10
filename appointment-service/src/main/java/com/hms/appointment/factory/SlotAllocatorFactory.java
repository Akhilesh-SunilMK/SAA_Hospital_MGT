package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SlotAllocator (Factory Method) — resolves the correct {@link SlotAllocationStrategy} for a
 * given AppointmentType. Registry is built from every SlotAllocationStrategy bean in the
 * context, so a new appointment type only requires a new @Component, never a change here.
 */
@Component
public class SlotAllocatorFactory {

    private final Map<AppointmentType, SlotAllocationStrategy> registry;

    public SlotAllocatorFactory(List<SlotAllocationStrategy> strategies) {
        this.registry = strategies.stream()
                .collect(Collectors.toMap(SlotAllocationStrategy::getType, s -> s));
    }

    public SlotAllocationStrategy resolve(AppointmentType type) {
        Objects.requireNonNull(type, "type is mandatory");
        SlotAllocationStrategy strategy = registry.get(type);
        if (strategy == null) {
            throw new UnsupportedAppointmentTypeException("No slot allocation strategy registered for type: " + type);
        }
        return strategy;
    }
}
