package com.hms.appointment.dto;

/** Local mirror of the subset of doctor-service's Doctor payload this service actually needs. */
public record DoctorSummary(Long id, String firstName, String lastName, java.math.BigDecimal consultationFee) {

    public String fullName() {
        return "Dr. " + (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
