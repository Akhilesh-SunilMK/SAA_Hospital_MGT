package com.hms.appointment.dto;

public record DoctorResponse(boolean success, int statusCode, String message, DoctorSummary data) {
}
