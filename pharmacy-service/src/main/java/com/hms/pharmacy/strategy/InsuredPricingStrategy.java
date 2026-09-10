package com.hms.pharmacy.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InsuredPricingStrategy implements PricingStrategy {

    private static final BigDecimal PATIENT_SHARE = new BigDecimal("0.80");

    @Override
    public PatientCategory getCategory() {
        return PatientCategory.INSURED;
    }

    @Override
    public BigDecimal price(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .multiply(PATIENT_SHARE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
