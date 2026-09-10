package com.hms.emr.dto;

import com.hms.emr.entity.RecordType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRecordRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        Long appointmentId,
        @NotNull RecordType recordType,
        String chiefComplaint,
        String notes,
        List<DiagnosisRequest> diagnoses,
        boolean finalise
) {
    public record DiagnosisRequest(@NotNull String icd10Code, @NotNull String description, String type) {
    }
}
