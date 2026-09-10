package com.hms.patient.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_allergies")
public class PatientAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 150)
    private String allergen;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "noted_on", nullable = false)
    private LocalDate notedOn;

    protected PatientAllergy() {
    }

    public PatientAllergy(String allergen, String severity, LocalDate notedOn) {
        this.allergen = allergen;
        this.severity = severity;
        this.notedOn = notedOn;
    }

    void assignTo(Patient patient) {
        this.patient = patient;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getAllergen() { return allergen; }
    public String getSeverity() { return severity; }
    public LocalDate getNotedOn() { return notedOn; }
}
