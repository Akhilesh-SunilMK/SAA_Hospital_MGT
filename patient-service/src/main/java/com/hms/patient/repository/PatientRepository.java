package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByMrn(String mrn);

    Optional<Patient> findByIdAndDeletedFalse(Long id);

    Optional<Patient> findByUserIdAndDeletedFalse(Long userId);

    @Query("select p from Patient p where p.deleted = false and (" +
            "lower(p.mrn) = lower(:term) or " +
            "lower(p.firstName) like lower(concat('%', :term, '%')) or " +
            "lower(p.lastName) like lower(concat('%', :term, '%')) or " +
            "p.phone = :term or " +
            "lower(p.email) = lower(:term))")
    Page<Patient> search(@Param("term") String term, Pageable pageable);
}
