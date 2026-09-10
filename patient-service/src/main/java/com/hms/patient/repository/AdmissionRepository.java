package com.hms.patient.repository;

import com.hms.patient.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {
    Optional<Admission> findFirstByPatientIdAndStatus(Long patientId, Admission.Status status);
}
