package com.hms.lab.dto;

import com.hms.lab.entity.TestCatalogue;

import java.math.BigDecimal;

public record TestCatalogueResponse(Long id, String code, String name, String sampleType, BigDecimal price, Integer turnaroundHours) {
    public static TestCatalogueResponse from(TestCatalogue t) {
        return new TestCatalogueResponse(t.getId(), t.getCode(), t.getName(), t.getSampleType(), t.getPrice(), t.getTurnaroundHours());
    }
}
