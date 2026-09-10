package com.hms.emr.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** FR-EM-08 / NFR-08: every read and write of an EMR record is written to an immutable audit log. */
@Entity
@Table(name = "emr_audit_log")
public class EmrAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    protected EmrAuditLog() {
        // JPA only
    }

    public EmrAuditLog(Long recordId, Long userId, AuditAction action, String ipAddress) {
        this.recordId = recordId;
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.accessedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRecordId() { return recordId; }
    public Long getUserId() { return userId; }
    public AuditAction getAction() { return action; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getAccessedAt() { return accessedAt; }
}
