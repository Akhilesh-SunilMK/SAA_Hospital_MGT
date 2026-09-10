package com.hms.emr.repository;

import com.hms.emr.entity.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VitalsRepository extends JpaRepository<Vitals, Long> {
    boolean existsByRecordId(Long recordId);
}
