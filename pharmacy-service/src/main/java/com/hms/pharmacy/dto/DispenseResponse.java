package com.hms.pharmacy.dto;

import com.hms.pharmacy.entity.DispenseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DispenseResponse(
        Long id,
        Long patientId,
        Long prescriptionId,
        BigDecimal totalAmount,
        LocalDateTime dispensedAt,
        int itemCount
) {
    public static DispenseResponse from(DispenseOrder order) {
        return new DispenseResponse(order.getId(), order.getPatientId(), order.getPrescriptionId(),
                order.getTotalAmount(), order.getDispensedAt(), order.getItems().size());
    }
}
