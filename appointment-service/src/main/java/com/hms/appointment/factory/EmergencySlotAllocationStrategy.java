package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;
import org.springframework.stereotype.Component;

/** FR-AP-06: EMERGENCY appointments bypass normal slot restrictions and get CRITICAL priority. */
@Component
public class EmergencySlotAllocationStrategy implements SlotAllocationStrategy {
    @Override
    public AppointmentType getType() {
        return AppointmentType.EMERGENCY;
    }

    @Override
    public boolean requiresSlotUniquenessCheck() {
        return false;
    }

    @Override
    public Priority defaultPriority() {
        return Priority.CRITICAL;
    }
}
