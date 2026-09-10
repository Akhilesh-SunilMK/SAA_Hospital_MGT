package com.hms.emr.factory;

import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordType;
import org.springframework.stereotype.Component;

/**
 * Emergency records carry a distinct chief-complaint template and — enforced in
 * {@code MedicalRecordService.finalise} rather than here, since it needs to check the separate
 * {@code vitals} table — cannot be finalised until at least one Vitals reading is attached.
 */
@Component
public class EmergencyRecordFactory implements RecordFactory {

    @Override
    public RecordType getType() {
        return RecordType.EMERGENCY;
    }

    @Override
    public MedicalRecord.Builder newBuilder(CreateRecordRequest request) {
        String complaint = request.chiefComplaint() != null
                ? "[EMERGENCY] " + request.chiefComplaint()
                : "[EMERGENCY] Unspecified presentation";
        return MedicalRecord.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .appointmentId(request.appointmentId())
                .recordType(RecordType.EMERGENCY)
                .chiefComplaint(complaint)
                .notes(request.notes());
    }
}
