package com.hms.emr.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.emr.dto.PrescriptionRequest;
import com.hms.emr.entity.Prescription;
import com.hms.emr.entity.PrescriptionItem;
import com.hms.emr.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional
    public Prescription create(PrescriptionRequest request) {
        Prescription.Builder builder = Prescription.builder()
                .recordId(request.recordId())
                .patientId(request.patientId())
                .doctorId(request.doctorId());
        for (var item : request.items()) {
            builder.addItem(new PrescriptionItem(item.drugName(), item.dosage(), item.frequency(),
                    item.durationDays(), item.instructions()));
        }
        return prescriptionRepository.save(builder.build());
    }

    @Transactional(readOnly = true)
    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
    }
}
