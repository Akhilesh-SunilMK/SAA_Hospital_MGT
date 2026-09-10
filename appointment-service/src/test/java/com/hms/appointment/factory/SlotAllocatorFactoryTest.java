package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotAllocatorFactoryTest {

    private final SlotAllocatorFactory factory = new SlotAllocatorFactory(List.of(
            new OpdSlotAllocationStrategy(),
            new FollowUpSlotAllocationStrategy(),
            new EmergencySlotAllocationStrategy(),
            new TeleSlotAllocationStrategy()
    ));

    @Test
    void resolvesEmergencyStrategy_bypassingUniquenessCheck_withCriticalPriority() {
        SlotAllocationStrategy strategy = factory.resolve(AppointmentType.EMERGENCY);
        assertThat(strategy.requiresSlotUniquenessCheck()).isFalse();
        assertThat(strategy.defaultPriority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void resolvesOpdStrategy_requiringUniquenessCheck() {
        SlotAllocationStrategy strategy = factory.resolve(AppointmentType.OPD);
        assertThat(strategy.requiresSlotUniquenessCheck()).isTrue();
        assertThat(strategy.defaultPriority()).isEqualTo(Priority.NORMAL);
    }

    @Test
    void unregisteredType_throwsClearException() {
        SlotAllocatorFactory emptyFactory = new SlotAllocatorFactory(List.of(new OpdSlotAllocationStrategy()));
        assertThatThrownBy(() -> emptyFactory.resolve(AppointmentType.TELE))
                .isInstanceOf(UnsupportedAppointmentTypeException.class)
                .hasMessageContaining("TELE");
    }

    @Test
    void nullType_throwsNullPointerException() {
        assertThatThrownBy(() -> factory.resolve(null)).isInstanceOf(NullPointerException.class);
    }
}
