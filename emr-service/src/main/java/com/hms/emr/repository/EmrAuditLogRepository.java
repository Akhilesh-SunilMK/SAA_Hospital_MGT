package com.hms.emr.repository;

import com.hms.emr.entity.EmrAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrAuditLogRepository extends JpaRepository<EmrAuditLog, Long> {
}
