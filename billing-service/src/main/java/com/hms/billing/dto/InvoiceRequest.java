package com.hms.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Supports both a full multi-line invoice (FR-BL-01: consultation + lab + pharmacy + room
 * charges) via {@code items}, and the simplified single-charge shape used by appointment-service's
 * Saga call ({@code category}/{@code description}/{@code amount}) when {@code items} is omitted.
 */
public record InvoiceRequest(
        @NotNull Long patientId,
        Long appointmentId,
        String category,
        String description,
        BigDecimal amount,
        BigDecimal taxRate,
        BigDecimal discount,
        List<ItemRequest> items
) {
    public record ItemRequest(String description, String category, Integer quantity,
                               BigDecimal unitPrice, BigDecimal taxRate) {
    }

    public List<ItemRequest> resolvedItems() {
        if (items != null && !items.isEmpty()) {
            return items;
        }
        String desc = description != null ? description : "Charge";
        String cat = category != null ? category : "CONSULTATION";
        BigDecimal unitPrice = amount != null ? amount : BigDecimal.valueOf(500);
        return List.of(new ItemRequest(desc, cat, 1, unitPrice, taxRate));
    }
}
