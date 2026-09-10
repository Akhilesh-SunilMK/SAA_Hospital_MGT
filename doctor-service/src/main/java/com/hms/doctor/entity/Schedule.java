package com.hms.doctor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Builder pattern deliverable for doctor-service (pattern-traceability matrix, SRS 4.5):
 * doctorId/dayOfWeek/startTime/endTime are mandatory, slotDurationMin is optional (default 15),
 * with invariant validation (endTime after startTime) enforced in {@code build()} — same idiom
 * as the Appointment builder in SRS 4.3.1. No public setters (NFR-12).
 */
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_min", nullable = false)
    private Integer slotDurationMin;

    protected Schedule() {
    }

    private Schedule(Builder b) {
        this.doctor = b.doctor;
        this.dayOfWeek = b.dayOfWeek;
        this.startTime = b.startTime;
        this.endTime = b.endTime;
        this.slotDurationMin = b.slotDurationMin;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Doctor doctor;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMin = 15;

        public Builder doctor(Doctor v) { this.doctor = v; return this; }
        public Builder dayOfWeek(DayOfWeek v) { this.dayOfWeek = v; return this; }
        public Builder startTime(LocalTime v) { this.startTime = v; return this; }
        public Builder endTime(LocalTime v) { this.endTime = v; return this; }
        public Builder slotDurationMin(Integer v) { this.slotDurationMin = v; return this; }

        public Schedule build() {
            Objects.requireNonNull(doctor, "doctor is mandatory");
            Objects.requireNonNull(dayOfWeek, "dayOfWeek is mandatory");
            Objects.requireNonNull(startTime, "startTime is mandatory");
            Objects.requireNonNull(endTime, "endTime is mandatory");
            if (slotDurationMin == null) {
                slotDurationMin = 15;
            }
            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("endTime must be after startTime");
            }
            if (slotDurationMin <= 0) {
                throw new IllegalArgumentException("slotDurationMin must be positive");
            }
            return new Schedule(this);
        }
    }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Integer getSlotDurationMin() { return slotDurationMin; }
}
