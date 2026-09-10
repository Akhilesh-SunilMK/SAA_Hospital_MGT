package com.hms.doctor.dto;

import com.hms.doctor.entity.Doctor;

import java.math.BigDecimal;

public record DoctorResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String registrationNo,
        String qualification,
        String specialisation,
        String department,
        BigDecimal consultationFee
) {
    public static DoctorResponse from(Doctor d) {
        return new DoctorResponse(d.getId(), d.getUserId(), d.getFirstName(), d.getLastName(),
                d.getRegistrationNo(), d.getQualification(), d.getSpecialisation(), d.getDepartment(),
                d.getConsultationFee());
    }
}
