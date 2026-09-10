package com.hms.pharmacy.dto;

import com.hms.pharmacy.entity.Drug;

import java.math.BigDecimal;

public record DrugResponse(
        Long id,
        String code,
        String genericName,
        String brandName,
        String form,
        String strength,
        String manufacturer,
        BigDecimal unitPrice,
        Integer reorderLevel
) {
    public static DrugResponse from(Drug d) {
        return new DrugResponse(d.getId(), d.getCode(), d.getGenericName(), d.getBrandName(),
                d.getForm(), d.getStrength(), d.getManufacturer(), d.getUnitPrice(), d.getReorderLevel());
    }
}
