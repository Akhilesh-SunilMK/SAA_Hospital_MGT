package com.hms.emr.service;

import com.hms.emr.dto.VitalsRequest;
import com.hms.emr.entity.Vitals;
import com.hms.emr.repository.VitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VitalsService {

    private final VitalsRepository vitalsRepository;

    public VitalsService(VitalsRepository vitalsRepository) {
        this.vitalsRepository = vitalsRepository;
    }

    @Transactional
    public Vitals record(VitalsRequest request) {
        Vitals vitals = new Vitals(request.patientId(), request.recordId(), request.bpSystolic(),
                request.bpDiastolic(), request.pulse(), request.temperature(), request.spo2(),
                request.heightCm(), request.weightKg());
        return vitalsRepository.save(vitals);
    }
}
