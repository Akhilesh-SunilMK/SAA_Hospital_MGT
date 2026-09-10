package com.hms.emr.factory;

import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordType;
import org.springframework.stereotype.Component;

@Component
public class FollowUpRecordFactory implements RecordFactory {

    @Override
    public RecordType getType() {
        return RecordType.FOLLOW_UP;
    }

    @Override
    public MedicalRecord.Builder newBuilder(CreateRecordRequest request) {
        String complaint = request.chiefComplaint() != null
                ? request.chiefComplaint()
                : "Follow-up review";
        return MedicalRecord.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .appointmentId(request.appointmentId())
                .recordType(RecordType.FOLLOW_UP)
                .chiefComplaint(complaint)
                .notes(request.notes());
    }
}
