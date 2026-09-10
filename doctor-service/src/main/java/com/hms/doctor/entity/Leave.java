package com.hms.doctor.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/** FR-DR-04: leave marking invalidates affected slots for that date range. */
@Entity
@Table(name = "leaves")
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(length = 255)
    private String reason;

    protected Leave() {
    }

    public Leave(Doctor doctor, LocalDate fromDate, LocalDate toDate, String reason) {
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate cannot be before fromDate");
        }
        this.doctor = doctor;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
    }

    public boolean covers(LocalDate date) {
        return !date.isBefore(fromDate) && !date.isAfter(toDate);
    }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public String getReason() { return reason; }
}
