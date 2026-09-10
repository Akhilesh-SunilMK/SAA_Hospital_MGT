package com.hms.appointment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

/**
 * Concurrency-safe slot guard: a unique index on (doctor_id, slot) here (not on the appointments
 * table itself, since EMERGENCY appointments must bypass slot uniqueness per FR-AP-06) turns a
 * race between two concurrent bookings into a DB constraint violation the service translates to
 * a 409 Conflict (FR-AP-03).
 */
@Entity
@Table(name = "slot_reservations", uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "slot"}))
public class SlotReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;

    private LocalDateTime slot;

    private Long appointmentId;

    @Version
    private Long version;

    protected SlotReservation() {
    }

    public SlotReservation(Long doctorId, LocalDateTime slot, Long appointmentId) {
        this.doctorId = doctorId;
        this.slot = slot;
        this.appointmentId = appointmentId;
    }

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDateTime getSlot() {
        return slot;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }
}
