package com.hms.emr.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vitals")
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "bp_systolic")
    private Integer bpSystolic;

    @Column(name = "bp_diastolic")
    private Integer bpDiastolic;

    private Integer pulse;

    private BigDecimal temperature;

    private Integer spo2;

    @Column(name = "height_cm")
    private BigDecimal heightCm;

    @Column(name = "weight_kg")
    private BigDecimal weightKg;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    protected Vitals() {
        // JPA only
    }

    public Vitals(Long patientId, Long recordId, Integer bpSystolic, Integer bpDiastolic, Integer pulse,
                  BigDecimal temperature, Integer spo2, BigDecimal heightCm, BigDecimal weightKg) {
        this.patientId = patientId;
        this.recordId = recordId;
        this.bpSystolic = bpSystolic;
        this.bpDiastolic = bpDiastolic;
        this.pulse = pulse;
        this.temperature = temperature;
        this.spo2 = spo2;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.recordedAt = LocalDateTime.now();
    }

    public BigDecimal computeBmi() {
        if (heightCm == null || weightKg == null || heightCm.signum() == 0) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100));
        return weightKg.divide(heightM.multiply(heightM), 1, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getRecordId() { return recordId; }
    public Integer getBpSystolic() { return bpSystolic; }
    public Integer getBpDiastolic() { return bpDiastolic; }
    public Integer getPulse() { return pulse; }
    public BigDecimal getTemperature() { return temperature; }
    public Integer getSpo2() { return spo2; }
    public BigDecimal getHeightCm() { return heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
