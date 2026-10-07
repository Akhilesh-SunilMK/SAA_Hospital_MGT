package com.hms.webui.dto;

import java.math.BigDecimal;
import java.util.List;

public final class EmrDtos {
    private EmrDtos() {}

    public record DiagnosisRequest(String icd10Code, String description, String type) {}

    public record CreateRecordRequest(Long patientId, Long doctorId, Long appointmentId, String recordType,
                                       String chiefComplaint, String notes, List<DiagnosisRequest> diagnoses,
                                       boolean finalise) {}

    public record RecordResponse(Long id, Long patientId, Long doctorId, Long appointmentId, String recordType,
                                  String chiefComplaint, String notes, boolean finalised, String createdAt,
                                  List<String> diagnoses) {}

    public record AmendRecordRequest(String newValue, String reason) {}

    public record PrescriptionItemRequest(String drugName, String dosage, String frequency,
                                           Integer durationDays, String instructions) {}

    public record PrescriptionRequest(Long recordId, Long patientId, Long doctorId, List<PrescriptionItemRequest> items) {}

    public record PrescriptionItemView(String drugName, String dosage, String frequency,
                                        Integer durationDays, String instructions) {}

    public record PrescriptionResponse(Long id, Long recordId, Long patientId, Long doctorId, String issuedAt,
                                        String status, List<PrescriptionItemView> items) {}

    public record VitalsRequest(Long patientId, Long recordId, Integer bpSystolic, Integer bpDiastolic,
                                 Integer pulse, BigDecimal temperature, Integer spo2, BigDecimal heightCm,
                                 BigDecimal weightKg) {}

    public record VitalsResponse(Long id, Long patientId, Long recordId, Integer bpSystolic, Integer bpDiastolic,
                                  Integer pulse, BigDecimal temperature, Integer spo2, BigDecimal heightCm,
                                  BigDecimal weightKg, BigDecimal bmi, String recordedAt) {}
}
