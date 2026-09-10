package com.hms.pharmacy.dto;

public record LowStockResponse(
        Long drugId,
        String drugCode,
        String drugName,
        int totalQuantity,
        int reorderLevel
) {
}
