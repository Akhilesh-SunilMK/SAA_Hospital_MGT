package com.hms.emr.dto;

import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordType;

import java.time.LocalDateTime;
import java.util.List;

public record RecordResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long appointmentId,
        RecordType recordType,
        String chiefComplaint,
        String notes,
        boolean finalised,
        LocalDateTime createdAt,
        List<String> diagnoses
) {
    public static RecordResponse from(MedicalRecord r) {
        return new RecordResponse(
                r.getId(), r.getPatientId(), r.getDoctorId(), r.getAppointmentId(), r.getRecordType(),
                r.getChiefComplaint(), r.getNotes(), r.isFinalised(), r.getCreatedAt(),
                r.getDiagnoses().stream().map(d -> d.getIcd10Code() + " - " + d.getDescription()).toList()
        );
    }
}
