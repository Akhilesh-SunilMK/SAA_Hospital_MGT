package com.hms.emr.dto;

import com.hms.emr.entity.Prescription;
import com.hms.emr.entity.PrescriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponse(
        Long id, Long recordId, Long patientId, Long doctorId, LocalDateTime issuedAt,
        PrescriptionStatus status, List<ItemResponse> items
) {
    public record ItemResponse(String drugName, String dosage, String frequency, Integer durationDays, String instructions) {
    }

    public static PrescriptionResponse from(Prescription p) {
        return new PrescriptionResponse(
                p.getId(), p.getRecordId(), p.getPatientId(), p.getDoctorId(), p.getIssuedAt(), p.getStatus(),
                p.getItems().stream()
                        .map(i -> new ItemResponse(i.getDrugName(), i.getDosage(), i.getFrequency(), i.getDurationDays(), i.getInstructions()))
                        .toList()
        );
    }
}
