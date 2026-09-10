package com.hms.appointment.entity;

import com.hms.appointment.exception.InvalidAppointmentException;
import jakarta.persistence.Access;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * FR-AP-02: constructed exclusively via {@link Builder#build()} with invariant validation.
 * No public setters (NFR-12) — state transitions go through the explicit methods below.
 * SRS 4.3.1.
 */
@Entity
@Table(name = "appointments")
@Access(AccessType.FIELD)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private final Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private final Long doctorId;

    // Not final: reschedule() is a controlled, intention-revealing mutation (still no public
    // setter), same idiom as the status/priority/tokenNumber transitions below.
    @Column(name = "slot", nullable = false)
    private LocalDateTime slot;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private final AppointmentType type;

    @Column(name = "reason", length = 500)
    private final String reason;

    @Column(name = "duration_min", nullable = false)
    private final Integer durationMinutes;

    @Column(name = "referred_by")
    private final Long referredByDoctorId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority;

    @Column(name = "insurance_claimed", nullable = false)
    private final boolean insuranceClaimed;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "token_no", length = 30)
    private String tokenNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** JPA-only constructor. */
    protected Appointment() {
        this.patientId = null;
        this.doctorId = null;
        this.slot = null;
        this.type = null;
        this.reason = null;
        this.durationMinutes = null;
        this.referredByDoctorId = null;
        this.insuranceClaimed = false;
    }

    private Appointment(Builder b) {
        this.patientId = b.patientId;
        this.doctorId = b.doctorId;
        this.slot = b.slot;
        this.type = b.type;
        this.reason = b.reason;
        this.durationMinutes = b.durationMinutes;
        this.referredByDoctorId = b.referredByDoctorId;
        this.priority = b.priority;
        this.insuranceClaimed = b.insuranceClaimed;
        this.status = b.status;
        this.createdAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long patientId;
        private Long doctorId;
        private LocalDateTime slot;
        private AppointmentType type;
        private String reason;
        private Integer durationMinutes = 15;
        private Long referredByDoctorId;
        private Priority priority = Priority.NORMAL;
        private boolean insuranceClaimed = false;
        private AppointmentStatus status = AppointmentStatus.SCHEDULED;

        public Builder patientId(Long v) {
            this.patientId = v;
            return this;
        }

        public Builder doctorId(Long v) {
            this.doctorId = v;
            return this;
        }

        public Builder slot(LocalDateTime v) {
            this.slot = v;
            return this;
        }

        public Builder type(AppointmentType v) {
            this.type = v;
            return this;
        }

        public Builder reason(String v) {
            this.reason = v;
            return this;
        }

        public Builder durationMinutes(Integer v) {
            this.durationMinutes = v;
            return this;
        }

        public Builder referredBy(Long v) {
            this.referredByDoctorId = v;
            return this;
        }

        public Builder priority(Priority v) {
            this.priority = v;
            return this;
        }

        public Builder insuranceClaimed(boolean v) {
            this.insuranceClaimed = v;
            return this;
        }

        public Appointment build() {
            Objects.requireNonNull(patientId, "patientId is mandatory");
            Objects.requireNonNull(doctorId, "doctorId is mandatory");
            Objects.requireNonNull(slot, "slot is mandatory");
            Objects.requireNonNull(type, "type is mandatory");
            if (slot.isBefore(LocalDateTime.now())) {
                throw new InvalidAppointmentException("Slot must be in the future");
            }
            if (type == AppointmentType.EMERGENCY) {
                this.priority = Priority.CRITICAL;
            }
            return new Appointment(this);
        }
    }

    // ---- Explicit, intention-revealing state transitions (no generic setters) ----

    public void assignTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public void reschedule(LocalDateTime newSlot) {
        this.slot = newSlot;
    }

    public void checkIn() {
        this.status = AppointmentStatus.CHECKED_IN;
    }

    public void startConsultation() {
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public void markNoShow() {
        this.status = AppointmentStatus.NO_SHOW;
    }

    public void changeStatus(AppointmentStatus status) {
        this.status = status;
    }

    // ---- Accessors (read-only) ----

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDateTime getSlot() {
        return slot;
    }

    public AppointmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Long getReferredByDoctorId() {
        return referredByDoctorId;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isInsuranceClaimed() {
        return insuranceClaimed;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }
}
