package com.hms.appointment.dto;

public record PatientResponse(boolean success, int statusCode, String message, PatientSummary data) {

    public record PatientSummary(Long id, String mrn, Long userId, String firstName, String lastName) {
    }
}
