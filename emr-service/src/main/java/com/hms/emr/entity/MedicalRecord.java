package com.hms.emr.entity;

import com.hms.common.exception.BusinessRuleException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FR-EM-02: constructed exclusively via {@link Builder} — 15+ potential fields (chief complaint,
 * notes, diagnoses, follow-up plan, record type seeding) make a telescoping constructor
 * unreadable (SRS 4.3.2, NFR-12). No public setters: once {@link #finalise()} is called the
 * record is immutable (FR-EM-07) and further changes must go through {@link #amend}.
 */
@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "record_type", length = 20)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private RecordType recordType;

    @Column(name = "chief_complaint", length = 500)
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean finalised;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecordAmendment> amendments = new ArrayList<>();

    protected MedicalRecord() {
        // JPA only
    }

    private MedicalRecord(Builder b) {
        this.patientId = b.patientId;
        this.doctorId = b.doctorId;
        this.appointmentId = b.appointmentId;
        this.recordType = b.recordType;
        this.chiefComplaint = b.chiefComplaint;
        this.notes = b.notes;
        this.finalised = false;
        this.createdAt = LocalDateTime.now();
        for (Diagnosis d : b.diagnoses) {
            addDiagnosis(d);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long patientId;
        private Long doctorId;
        private Long appointmentId;
        private RecordType recordType = RecordType.CONSULTATION;
        private String chiefComplaint;
        private String notes;
        private final List<Diagnosis> diagnoses = new ArrayList<>();

        public Builder patientId(Long v) { this.patientId = v; return this; }
        public Builder doctorId(Long v) { this.doctorId = v; return this; }
        public Builder appointmentId(Long v) { this.appointmentId = v; return this; }
        public Builder recordType(RecordType v) { this.recordType = v; return this; }
        public Builder chiefComplaint(String v) { this.chiefComplaint = v; return this; }
        public Builder notes(String v) { this.notes = v; return this; }
        public Builder addDiagnosis(Diagnosis d) { this.diagnoses.add(d); return this; }

        public MedicalRecord build() {
            Objects.requireNonNull(patientId, "patientId is mandatory");
            Objects.requireNonNull(doctorId, "doctorId is mandatory");
            Objects.requireNonNull(recordType, "recordType is mandatory");
            return new MedicalRecord(this);
        }
    }

    // -- intention-revealing mutation methods (no generic setters) --

    public void updateDraft(String chiefComplaint, String notes) {
        requireNotFinalised("update");
        this.chiefComplaint = chiefComplaint;
        this.notes = notes;
    }

    public void addDiagnosis(Diagnosis diagnosis) {
        requireNotFinalised("add a diagnosis to");
        diagnoses.add(diagnosis);
        diagnosis.assignTo(this);
    }

    public void finalise() {
        if (finalised) {
            throw new BusinessRuleException("Medical record " + id + " is already finalised");
        }
        this.finalised = true;
    }

    public void amend(RecordAmendment amendment) {
        if (!finalised) {
            throw new BusinessRuleException("Only a finalised record can be amended — edit the draft directly instead");
        }
        amendments.add(amendment);
        amendment.assignTo(this);
        this.notes = amendment.getNewValue();
    }

    private void requireNotFinalised(String action) {
        if (finalised) {
            throw new BusinessRuleException("Cannot " + action + " a finalised record " + id + " — use an amendment instead");
        }
    }

    // -- getters only --

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public Long getAppointmentId() { return appointmentId; }
    public RecordType getRecordType() { return recordType; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getNotes() { return notes; }
    public boolean isFinalised() { return finalised; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Diagnosis> getDiagnoses() { return diagnoses; }
    public List<RecordAmendment> getAmendments() { return amendments; }
}
