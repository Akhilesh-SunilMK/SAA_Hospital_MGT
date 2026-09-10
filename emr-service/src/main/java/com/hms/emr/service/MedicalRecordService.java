package com.hms.emr.service;

import com.hms.common.exception.BusinessRuleException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.emr.dto.AmendRecordRequest;
import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.entity.AuditAction;
import com.hms.emr.entity.Diagnosis;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.RecordAmendment;
import com.hms.emr.entity.RecordType;
import com.hms.emr.factory.RecordFactoryRegistry;
import com.hms.emr.repository.MedicalRecordRepository;
import com.hms.emr.repository.VitalsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final VitalsRepository vitalsRepository;
    private final RecordFactoryRegistry recordFactoryRegistry;
    private final AuditLogService auditLogService;

    public MedicalRecordService(MedicalRecordRepository recordRepository,
                                 VitalsRepository vitalsRepository,
                                 RecordFactoryRegistry recordFactoryRegistry,
                                 AuditLogService auditLogService) {
        this.recordRepository = recordRepository;
        this.vitalsRepository = vitalsRepository;
        this.recordFactoryRegistry = recordFactoryRegistry;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public MedicalRecord create(CreateRecordRequest request, HttpServletRequest httpRequest) {
        MedicalRecord.Builder builder = recordFactoryRegistry.resolve(request.recordType()).newBuilder(request);
        if (request.diagnoses() != null) {
            for (var d : request.diagnoses()) {
                builder.addDiagnosis(new Diagnosis(d.icd10Code(), d.description(), d.type()));
            }
        }
        MedicalRecord record = builder.build();
        record = recordRepository.save(record);
        if (request.finalise()) {
            finalise(record.getId(), httpRequest);
            record = recordRepository.findById(record.getId()).orElseThrow();
        }
        auditLogService.record(record.getId(), AuditAction.CREATE, httpRequest);
        return record;
    }

    @Transactional
    public MedicalRecord finalise(Long recordId, HttpServletRequest httpRequest) {
        MedicalRecord record = getById(recordId, null);
        if (record.getRecordType() == RecordType.EMERGENCY && !vitalsRepository.existsByRecordId(recordId)) {
            throw new BusinessRuleException("An EMERGENCY record cannot be finalised without at least one vitals reading attached");
        }
        record.finalise();
        return recordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getById(Long id, HttpServletRequest httpRequest) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found: " + id));
        if (httpRequest != null) {
            auditLogService.record(id, AuditAction.READ, httpRequest);
        }
        return record;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> history(Long patientId) {
        return recordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public MedicalRecord amend(Long recordId, AmendRecordRequest request, Long amendedBy, HttpServletRequest httpRequest) {
        MedicalRecord record = getById(recordId, null);
        RecordAmendment amendment = new RecordAmendment(amendedBy, record.getNotes(), request.newValue(), request.reason());
        record.amend(amendment);
        MedicalRecord saved = recordRepository.save(record);
        auditLogService.record(recordId, AuditAction.AMEND, httpRequest);
        return saved;
    }
}
