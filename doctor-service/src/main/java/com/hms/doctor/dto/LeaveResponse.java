package com.hms.doctor.dto;

import com.hms.doctor.entity.Leave;

import java.time.LocalDate;

public record LeaveResponse(Long id, LocalDate fromDate, LocalDate toDate, String reason) {
    public static LeaveResponse from(Leave l) {
        return new LeaveResponse(l.getId(), l.getFromDate(), l.getToDate(), l.getReason());
    }
}
