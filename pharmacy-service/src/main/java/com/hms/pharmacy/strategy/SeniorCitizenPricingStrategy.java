package com.hms.pharmacy.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SeniorCitizenPricingStrategy implements PricingStrategy {

    private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.90");

    @Override
    public PatientCategory getCategory() {
        return PatientCategory.SENIOR_CITIZEN;
    }

    @Override
    public BigDecimal price(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .multiply(DISCOUNT_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
