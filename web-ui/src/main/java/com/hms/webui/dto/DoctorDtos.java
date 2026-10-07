package com.hms.webui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class DoctorDtos {
    private DoctorDtos() {}

    public record DoctorRequest(Long userId, String firstName, String lastName, String registrationNo,
                                 String qualification, String specialisation, String department,
                                 BigDecimal consultationFee) {}

    public record DoctorResponse(Long id, Long userId, String firstName, String lastName, String registrationNo,
                                  String qualification, String specialisation, String department,
                                  BigDecimal consultationFee) {}

    public record AvailabilityResponse(Long doctorId, LocalDate date, boolean onLeave, List<LocalTime> availableSlots) {}

    public record SlotRequest(String dayOfWeek, LocalTime startTime, LocalTime endTime, Integer slotDurationMin) {}

    public record ScheduleRequest(List<SlotRequest> slots) {}

    public record ScheduleResponse(Long id, String dayOfWeek, LocalTime startTime, LocalTime endTime, Integer slotDurationMin) {}

    public record LeaveRequest(LocalDate fromDate, LocalDate toDate, String reason) {}

    public record LeaveResponse(Long id, LocalDate fromDate, LocalDate toDate, String reason) {}
}
