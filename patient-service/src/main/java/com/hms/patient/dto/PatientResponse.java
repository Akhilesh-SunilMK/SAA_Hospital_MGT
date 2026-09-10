package com.hms.patient.dto;

import com.hms.patient.entity.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String mrn,
        Long userId,
        String firstName,
        String lastName,
        LocalDate dob,
        String gender,
        String bloodGroup,
        String phone,
        String email,
        String address,
        String allergiesSummary,
        String chronicConditions,
        LocalDateTime createdAt,
        boolean deleted
) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(p.getId(), p.getMrn(), p.getUserId(), p.getFirstName(), p.getLastName(),
                p.getDob(), p.getGender(), p.getBloodGroup(), p.getPhone(), p.getEmail(), p.getAddress(),
                p.getAllergiesSummary(), p.getChronicConditions(), p.getCreatedAt(), p.isDeleted());
    }
}
