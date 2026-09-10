package com.hms.patient.dto;

import com.hms.patient.entity.Admission;

import java.time.LocalDateTime;

public record AdmissionResponse(
        Long id,
        Long patientId,
        String wardId,
        String bedNo,
        LocalDateTime admittedAt,
        LocalDateTime dischargedAt,
        String status
) {
    public static AdmissionResponse from(Admission a) {
        return new AdmissionResponse(a.getId(), a.getPatient().getId(), a.getWardId(), a.getBedNo(),
                a.getAdmittedAt(), a.getDischargedAt(), a.getStatus().name());
    }
}
