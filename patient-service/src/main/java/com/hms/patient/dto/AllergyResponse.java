package com.hms.patient.dto;

import com.hms.patient.entity.PatientAllergy;

import java.time.LocalDate;

public record AllergyResponse(Long id, String allergen, String severity, LocalDate notedOn) {
    public static AllergyResponse from(PatientAllergy a) {
        return new AllergyResponse(a.getId(), a.getAllergen(), a.getSeverity(), a.getNotedOn());
    }
}
