package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;
import org.springframework.stereotype.Component;

@Component
public class OpdSlotAllocationStrategy implements SlotAllocationStrategy {
    @Override
    public AppointmentType getType() {
        return AppointmentType.OPD;
    }

    @Override
    public boolean requiresSlotUniquenessCheck() {
        return true;
    }

    @Override
    public Priority defaultPriority() {
        return Priority.NORMAL;
    }
}
