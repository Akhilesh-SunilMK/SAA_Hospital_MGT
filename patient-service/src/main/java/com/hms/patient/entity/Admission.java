package com.hms.patient.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** FR-PT-06: IPD admission and discharge, including ward and bed assignment. */
@Entity
@Table(name = "admissions")
public class Admission {

    public enum Status { ADMITTED, DISCHARGED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "ward_id", nullable = false, length = 50)
    private String wardId;

    @Column(name = "bed_no", nullable = false, length = 20)
    private String bedNo;

    @Column(name = "admitted_at", nullable = false)
    private LocalDateTime admittedAt;

    @Column(name = "discharged_at")
    private LocalDateTime dischargedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected Admission() {
    }

    public Admission(Patient patient, String wardId, String bedNo) {
        this.patient = patient;
        this.wardId = wardId;
        this.bedNo = bedNo;
        this.admittedAt = LocalDateTime.now();
        this.status = Status.ADMITTED;
    }

    public void discharge() {
        this.dischargedAt = LocalDateTime.now();
        this.status = Status.DISCHARGED;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getWardId() { return wardId; }
    public String getBedNo() { return bedNo; }
    public LocalDateTime getAdmittedAt() { return admittedAt; }
    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public Status getStatus() { return status; }
}
