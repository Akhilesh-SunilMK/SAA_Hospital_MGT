package com.hms.lab.entity;

import com.hms.common.exception.BusinessRuleException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FR-LB-02: constructed via {@link Builder} — "test panel composition, sample type, priority,
 * fasting requirement" (SRS 4.3.2). Referred to as the LabTestOrder builder in the SRS
 * traceability matrix; the entity/table is named {@code LabOrder}/{@code lab_orders} to match
 * SRS 8.1 exactly. No public setters — status moves only through {@link #markStatus}.
 */
@Entity
@Table(name = "lab_orders")
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false, length = 10)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private LabPriority priority;

    @Column(nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private LabOrderStatus status;

    @Column(name = "fasting_required", nullable = false)
    private boolean fastingRequired;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LabOrderItem> items = new ArrayList<>();

    protected LabOrder() {
        // JPA only
    }

    private LabOrder(Builder b) {
        this.patientId = b.patientId;
        this.doctorId = b.doctorId;
        this.orderedAt = LocalDateTime.now();
        this.priority = b.priority;
        this.status = LabOrderStatus.ORDERED;
        this.fastingRequired = b.fastingRequired;
        for (Long testId : b.testIds) {
            addItem(new LabOrderItem(testId));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long patientId;
        private Long doctorId;
        private LabPriority priority = LabPriority.NORMAL;
        private boolean fastingRequired = false;
        private final List<Long> testIds = new ArrayList<>();

        public Builder patientId(Long v) { this.patientId = v; return this; }
        public Builder doctorId(Long v) { this.doctorId = v; return this; }
        public Builder priority(LabPriority v) { this.priority = v; return this; }
        public Builder fastingRequired(boolean v) { this.fastingRequired = v; return this; }
        public Builder addTest(Long testId) { this.testIds.add(testId); return this; }
        public Builder testIds(List<Long> ids) { this.testIds.addAll(ids); return this; }

        public LabOrder build() {
            Objects.requireNonNull(patientId, "patientId is mandatory");
            Objects.requireNonNull(doctorId, "doctorId is mandatory");
            Objects.requireNonNull(priority, "priority is mandatory");
            if (testIds.isEmpty()) {
                throw new IllegalArgumentException("A lab order must contain at least one test");
            }
            return new LabOrder(this);
        }
    }

    public void addItem(LabOrderItem item) {
        items.add(item);
        item.assignTo(this);
    }

    public void markStatus(LabOrderStatus newStatus) {
        validateTransition(this.status, newStatus);
        this.status = newStatus;
    }

    public void refreshStatusFromItems() {
        if (items.stream().allMatch(i -> i.getStatus() == LabOrderStatus.COMPLETED)) {
            this.status = LabOrderStatus.COMPLETED;
        }
    }

    private void validateTransition(LabOrderStatus from, LabOrderStatus to) {
        if (from == LabOrderStatus.CANCELLED || from == LabOrderStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot transition a " + from + " order to " + to);
        }
        boolean forwardMove = switch (from) {
            case ORDERED -> to == LabOrderStatus.COLLECTED || to == LabOrderStatus.CANCELLED;
            case COLLECTED -> to == LabOrderStatus.PROCESSING || to == LabOrderStatus.CANCELLED;
            case PROCESSING -> to == LabOrderStatus.COMPLETED || to == LabOrderStatus.CANCELLED;
            default -> false;
        };
        if (!forwardMove) {
            throw new BusinessRuleException("Invalid status transition from " + from + " to " + to);
        }
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public LabPriority getPriority() { return priority; }
    public LabOrderStatus getStatus() { return status; }
    public boolean isFastingRequired() { return fastingRequired; }
    public List<LabOrderItem> getItems() { return items; }
}
