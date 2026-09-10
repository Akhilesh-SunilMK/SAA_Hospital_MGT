package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;
import org.springframework.stereotype.Component;

@Component
public class FollowUpSlotAllocationStrategy implements SlotAllocationStrategy {
    @Override
    public AppointmentType getType() {
        return AppointmentType.FOLLOW_UP;
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
