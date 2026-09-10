package com.hms.pharmacy.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RetailPricingStrategy implements PricingStrategy {

    @Override
    public PatientCategory getCategory() {
        return PatientCategory.GENERAL;
    }

    @Override
    public BigDecimal price(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
