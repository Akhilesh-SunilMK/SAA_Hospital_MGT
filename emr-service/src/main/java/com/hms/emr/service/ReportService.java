package com.hms.emr.service;

import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.entity.Prescription;
import com.hms.emr.factory.FactoryProvider;
import com.hms.emr.factory.PatientSummaryData;
import com.hms.emr.factory.ReportComponentFactory;
import com.hms.emr.factory.ReportFormat;
import com.hms.emr.repository.MedicalRecordRepository;
import com.hms.emr.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** FR-EM-09: exports a patient summary via the Abstract Factory report pipeline (SRS 4.2). */
@Service
public class ReportService {

    private final MedicalRecordRepository recordRepository;
    private final PrescriptionRepository prescriptionRepository;

    public ReportService(MedicalRecordRepository recordRepository, PrescriptionRepository prescriptionRepository) {
        this.recordRepository = recordRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateSummary(Long patientId, ReportFormat format) {
        PatientSummaryData data = buildSummaryData(patientId);
        ReportComponentFactory factory = FactoryProvider.of(format);
        String formatted = factory.createFormatter().format(data, factory.createStyler());
        return factory.createExporter().export(formatted);
    }

    private PatientSummaryData buildSummaryData(Long patientId) {
        List<MedicalRecord> records = recordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        List<PatientSummaryData.RecordLine> recordLines = records.stream()
                .map(r -> new PatientSummaryData.RecordLine(
                        r.getId(), String.valueOf(r.getCreatedAt()), r.getChiefComplaint(),
                        r.getDiagnoses().stream().map(d -> d.getIcd10Code() + " - " + d.getDescription()).toList()))
                .toList();

        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        List<PatientSummaryData.PrescriptionLine> prescriptionLines = prescriptions.stream()
                .map(p -> new PatientSummaryData.PrescriptionLine(
                        p.getId(), String.valueOf(p.getIssuedAt()),
                        p.getItems().stream().map(i -> i.getDrugName() + " " + i.getDosage() + " " + i.getFrequency()).toList()))
                .toList();

        return new PatientSummaryData(patientId, recordLines, prescriptionLines);
    }
}
