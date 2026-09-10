package com.hms.lab.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_results")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(length = 30)
    private String unit;

    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    @Column(name = "abnormal_flag", nullable = false)
    private boolean abnormalFlag;

    @Column(name = "reported_by", nullable = false)
    private Long reportedBy;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    protected LabResult() {
        // JPA only
    }

    public LabResult(Long orderItemId, String value, String unit, String referenceRange,
                      boolean abnormalFlag, Long reportedBy) {
        this.orderItemId = orderItemId;
        this.value = value;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.abnormalFlag = abnormalFlag;
        this.reportedBy = reportedBy;
        this.reportedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getOrderItemId() { return orderItemId; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
    public String getReferenceRange() { return referenceRange; }
    public boolean isAbnormalFlag() { return abnormalFlag; }
    public Long getReportedBy() { return reportedBy; }
    public LocalDateTime getReportedAt() { return reportedAt; }
}
