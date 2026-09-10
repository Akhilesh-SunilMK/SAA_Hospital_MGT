package com.hms.emr.dto;

import com.hms.emr.entity.Vitals;

import java.math.BigDecimal;

public record VitalsResponse(
        Long id, Long patientId, Long recordId, Integer bpSystolic, Integer bpDiastolic, Integer pulse,
        BigDecimal temperature, Integer spo2, BigDecimal heightCm, BigDecimal weightKg, BigDecimal bmi
) {
    public static VitalsResponse from(Vitals v) {
        return new VitalsResponse(v.getId(), v.getPatientId(), v.getRecordId(), v.getBpSystolic(),
                v.getBpDiastolic(), v.getPulse(), v.getTemperature(), v.getSpo2(), v.getHeightCm(),
                v.getWeightKg(), v.computeBmi());
    }
}
