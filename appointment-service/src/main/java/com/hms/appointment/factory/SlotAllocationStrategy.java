package com.hms.appointment.factory;

import com.hms.appointment.entity.AppointmentType;
import com.hms.appointment.entity.Priority;

/**
 * Product interface for the SlotAllocator Factory Method (pattern-traceability matrix, SRS 4.5).
 * One strategy per AppointmentType decides slot-collision semantics and default priority —
 * adding a new AppointmentType means adding a new @Component, never touching the factory or the
 * booking service (same Open/Closed idiom as NotificationSenderFactory in SRS 4.1.1).
 */
public interface SlotAllocationStrategy {

    AppointmentType getType();

    /** False for EMERGENCY: FR-AP-06 bypasses normal slot restrictions. */
    boolean requiresSlotUniquenessCheck();

    Priority defaultPriority();
}
