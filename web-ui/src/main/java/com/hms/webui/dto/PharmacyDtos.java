package com.hms.webui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PharmacyDtos {
    private PharmacyDtos() {}

    public record DrugRequest(String code, String genericName, String brandName, String form, String strength,
                               String manufacturer, BigDecimal unitPrice, Integer reorderLevel) {}

    public record DrugResponse(Long id, String code, String genericName, String brandName, String form,
                                String strength, String manufacturer, BigDecimal unitPrice, Integer reorderLevel) {}

    public record StockAdjustRequest(String batchNo, Integer quantity, LocalDate expiryDate) {}

    public record DispenseItemRequest(Long drugId, Integer quantity) {}

    public record DispenseRequest(Long patientId, Long prescriptionId, String patientCategory,
                                   List<DispenseItemRequest> items) {}

    public record DispenseResponse(Long id, Long patientId, Long prescriptionId, BigDecimal totalAmount,
                                    String dispensedAt, Integer itemCount) {}

    public record LowStockResponse(Long drugId, String drugCode, String drugName, Integer totalQuantity,
                                    Integer reorderLevel) {}
}
