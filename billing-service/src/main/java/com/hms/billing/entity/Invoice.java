package com.hms.billing.entity;

import com.hms.common.exception.BusinessRuleException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder-constructed per SRS 4.3.2 ("Invoice — Billing — Line items, tax components, discounts,
 * insurance split, payment terms") and FR-BL-02. The builder derives subtotal/tax/total/balance
 * from the line items rather than accepting them pre-computed, mirroring how the Appointment
 * builder (SRS 4.3.1) derives CRITICAL priority rather than trusting caller input. No public
 * setters (NFR-12) — payment application goes through {@link #applyPayment(BigDecimal)}.
 */
@Entity
@Table(name = "invoices")
@Getter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", nullable = false, unique = true, length = 30)
    private String invoiceNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceItem> items = new ArrayList<>();

    protected Invoice() {
        // required by Hibernate
    }

    private Invoice(Builder b) {
        this.invoiceNo = b.invoiceNo;
        this.patientId = b.patientId;
        this.appointmentId = b.appointmentId;
        this.items = b.items;
        this.items.forEach(item -> item.assignInvoice(this));
        this.subtotal = b.items.stream().map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        this.tax = b.items.stream().map(InvoiceItem::taxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        this.discount = b.discount;
        this.total = subtotal.add(tax).subtract(discount).setScale(2, RoundingMode.HALF_UP);
        this.balance = this.total;
        this.status = InvoiceStatus.ISSUED;
        this.createdAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** FR-BL-06: partial payments supported; status derived from remaining balance. */
    public void applyPayment(BigDecimal amount) {
        if (this.status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot apply payment to a cancelled invoice");
        }
        BigDecimal newBalance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Payment amount exceeds outstanding balance");
        }
        this.balance = newBalance;
        this.status = newBalance.compareTo(BigDecimal.ZERO) == 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
    }

    public void markRefunded() {
        this.status = InvoiceStatus.REFUNDED;
    }

    public static class Builder {
        private String invoiceNo;
        private Long patientId;
        private Long appointmentId;
        private BigDecimal discount = BigDecimal.ZERO;
        private final List<InvoiceItem> items = new ArrayList<>();

        public Builder invoiceNo(String v) {
            this.invoiceNo = v;
            return this;
        }

        public Builder patientId(Long v) {
            this.patientId = v;
            return this;
        }

        public Builder appointmentId(Long v) {
            this.appointmentId = v;
            return this;
        }

        public Builder discount(BigDecimal v) {
            this.discount = v != null ? v : BigDecimal.ZERO;
            return this;
        }

        public Builder addItem(String description, String category, Integer quantity,
                                BigDecimal unitPrice, BigDecimal taxRate) {
            this.items.add(new InvoiceItem(description, category, quantity, unitPrice, taxRate));
            return this;
        }

        public Invoice build() {
            Objects.requireNonNull(invoiceNo, "invoiceNo is mandatory");
            Objects.requireNonNull(patientId, "patientId is mandatory");
            if (items.isEmpty()) {
                throw new IllegalArgumentException("An invoice must contain at least one line item");
            }
            return new Invoice(this);
        }
    }
}
