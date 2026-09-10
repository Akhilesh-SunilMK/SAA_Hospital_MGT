package com.hms.billing.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    @Test
    void computesSubtotalTaxTotalAndBalanceFromItems() {
        Invoice invoice = Invoice.builder()
                .invoiceNo("INV-2026-00001")
                .patientId(10L)
                .addItem("Consultation", "CONSULTATION", 1, new BigDecimal("500.00"), new BigDecimal("10"))
                .addItem("Paracetamol", "PHARMACY", 2, new BigDecimal("25.00"), BigDecimal.ZERO)
                .build();

        assertThat(invoice.getSubtotal()).isEqualByComparingTo("550.00");
        assertThat(invoice.getTax()).isEqualByComparingTo("50.00");
        assertThat(invoice.getTotal()).isEqualByComparingTo("600.00");
        assertThat(invoice.getBalance()).isEqualByComparingTo("600.00");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void rejectsEmptyItems() {
        assertThatThrownBy(() -> Invoice.builder().invoiceNo("INV-1").patientId(1L).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresMandatoryFields() {
        assertThatThrownBy(() -> Invoice.builder()
                .patientId(1L)
                .addItem("x", "CONSULTATION", 1, BigDecimal.ONE, null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void partialPaymentMovesToPartiallyPaidThenPaid() {
        Invoice invoice = Invoice.builder()
                .invoiceNo("INV-2")
                .patientId(1L)
                .addItem("x", "CONSULTATION", 1, new BigDecimal("100.00"), null)
                .build();

        invoice.applyPayment(new BigDecimal("40.00"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(invoice.getBalance()).isEqualByComparingTo("60.00");

        invoice.applyPayment(new BigDecimal("60.00"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void entityHasNoPublicSetters() {
        boolean hasSetter = Arrays.stream(Invoice.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("set"));
        assertThat(hasSetter).isFalse();
    }
}
