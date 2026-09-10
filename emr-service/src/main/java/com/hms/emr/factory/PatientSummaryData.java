package com.hms.emr.factory;

import java.util.List;

/** Aggregated view fed into the Abstract Factory report pipeline. */
public record PatientSummaryData(
        Long patientId,
        List<RecordLine> records,
        List<PrescriptionLine> prescriptions
) {
    public record RecordLine(Long recordId, String createdAt, String chiefComplaint, List<String> diagnoses) {
    }

    public record PrescriptionLine(Long prescriptionId, String issuedAt, List<String> drugLines) {
    }
}
