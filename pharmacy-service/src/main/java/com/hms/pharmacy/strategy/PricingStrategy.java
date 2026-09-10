package com.hms.pharmacy.strategy;

import java.math.BigDecimal;

/** Strategy pattern (SRS 4.4, FR-PH-06) — one implementation per {@link PatientCategory}. */
public interface PricingStrategy {

    PatientCategory getCategory();

    BigDecimal price(BigDecimal unitPrice, int quantity);
}
