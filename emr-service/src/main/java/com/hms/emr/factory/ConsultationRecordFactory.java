package com.hms.emr.factory;

import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordType;
import org.springframework.stereotype.Component;

@Component
public class ConsultationRecordFactory implements RecordFactory {

    @Override
    public RecordType getType() {
        return RecordType.CONSULTATION;
    }

    @Override
    public MedicalRecord.Builder newBuilder(CreateRecordRequest request) {
        return MedicalRecord.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .appointmentId(request.appointmentId())
                .recordType(RecordType.CONSULTATION)
                .chiefComplaint(request.chiefComplaint())
                .notes(request.notes());
    }
}
