package com.hms.billing.factory;

/**
 * Factory Method product interface (SRS 4.1.3) — one implementation per {@link PaymentMethod},
 * each with its own validation rules, settlement flow, and receipt format.
 */
public interface PaymentProcessor {

    PaymentMethod getMethod();

    PaymentResult process(PaymentRequest request);
}
