package com.hms.doctor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ScheduleRequest(@NotEmpty @Valid List<ScheduleSlot> slots) {

    public record ScheduleSlot(
            String dayOfWeek,
            String startTime,
            String endTime,
            Integer slotDurationMin
    ) {
    }
}
