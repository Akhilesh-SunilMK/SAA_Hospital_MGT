package com.hms.doctor.dto;

import com.hms.doctor.entity.Schedule;

public record ScheduleResponse(Long id, String dayOfWeek, String startTime, String endTime, Integer slotDurationMin) {
    public static ScheduleResponse from(Schedule s) {
        return new ScheduleResponse(s.getId(), s.getDayOfWeek().name(), s.getStartTime().toString(),
                s.getEndTime().toString(), s.getSlotDurationMin());
    }
}
