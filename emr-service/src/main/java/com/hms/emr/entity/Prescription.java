package com.hms.emr.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FR-EM-05: constructed via {@link Builder} — variable-length drug lines, each with dosage,
 * frequency, duration and instructions, make a telescoping constructor unworkable (SRS 4.3.2).
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PrescriptionItem> items = new ArrayList<>();

    protected Prescription() {
        // JPA only
    }

    private Prescription(Builder b) {
        this.recordId = b.recordId;
        this.patientId = b.patientId;
        this.doctorId = b.doctorId;
        this.issuedAt = LocalDateTime.now();
        this.status = PrescriptionStatus.ISSUED;
        for (PrescriptionItem item : b.items) {
            addItem(item);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long recordId;
        private Long patientId;
        private Long doctorId;
        private final List<PrescriptionItem> items = new ArrayList<>();

        public Builder recordId(Long v) { this.recordId = v; return this; }
        public Builder patientId(Long v) { this.patientId = v; return this; }
        public Builder doctorId(Long v) { this.doctorId = v; return this; }
        public Builder addItem(PrescriptionItem item) { this.items.add(item); return this; }
        public Builder items(List<PrescriptionItem> values) { this.items.addAll(values); return this; }

        public Prescription build() {
            Objects.requireNonNull(recordId, "recordId is mandatory");
            Objects.requireNonNull(patientId, "patientId is mandatory");
            Objects.requireNonNull(doctorId, "doctorId is mandatory");
            if (items.isEmpty()) {
                throw new IllegalArgumentException("A prescription must contain at least one drug line");
            }
            return new Prescription(this);
        }
    }

    public void addItem(PrescriptionItem item) {
        items.add(item);
        item.assignTo(this);
    }

    public void markDispensed() {
        this.status = PrescriptionStatus.DISPENSED;
    }

    public void cancel() {
        this.status = PrescriptionStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public Long getRecordId() { return recordId; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public PrescriptionStatus getStatus() { return status; }
    public List<PrescriptionItem> getItems() { return items; }
}
