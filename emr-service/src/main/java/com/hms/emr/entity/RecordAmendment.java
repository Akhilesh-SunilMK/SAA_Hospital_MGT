package com.hms.emr.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** FR-EM-07: a finalised MedicalRecord is immutable — corrections are recorded as amendments. */
@Entity
@Table(name = "record_amendments")
public class RecordAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Column(name = "amended_by", nullable = false)
    private Long amendedBy;

    @Column(name = "previous_value", nullable = false, columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "amended_at", nullable = false)
    private LocalDateTime amendedAt;

    protected RecordAmendment() {
        // JPA only
    }

    public RecordAmendment(Long amendedBy, String previousValue, String newValue, String reason) {
        this.amendedBy = amendedBy;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.reason = reason;
        this.amendedAt = LocalDateTime.now();
    }

    void assignTo(MedicalRecord record) {
        this.medicalRecord = record;
    }

    public Long getId() {
        return id;
    }

    public Long getAmendedBy() {
        return amendedBy;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getAmendedAt() {
        return amendedAt;
    }
}
