package com.hms.emr.service;

import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.emr.entity.AuditAction;
import com.hms.emr.entity.EmrAuditLog;
import com.hms.emr.repository.EmrAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/** FR-EM-08 / NFR-08: every read and write of an EMR record is written to an immutable audit log. */
@Service
public class AuditLogService {

    private final EmrAuditLogRepository auditLogRepository;

    public AuditLogService(EmrAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(Long recordId, AuditAction action, HttpServletRequest request) {
        Long userId = callerUserId(request);
        String ip = request != null ? request.getRemoteAddr() : null;
        auditLogRepository.save(new EmrAuditLog(recordId, userId != null ? userId : 0L, action, ip));
    }

    private Long callerUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return attr instanceof Long l ? l : null;
    }
}
