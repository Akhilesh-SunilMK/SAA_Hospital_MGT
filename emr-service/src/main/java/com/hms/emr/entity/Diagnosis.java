package com.hms.emr.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Column(name = "icd10_code", nullable = false, length = 10)
    private String icd10Code;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 30)
    private String type;

    protected Diagnosis() {
        // JPA only
    }

    public Diagnosis(String icd10Code, String description, String type) {
        this.icd10Code = icd10Code;
        this.description = description;
        this.type = type;
    }

    void assignTo(MedicalRecord record) {
        this.medicalRecord = record;
    }

    public Long getId() {
        return id;
    }

    public String getIcd10Code() {
        return icd10Code;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
