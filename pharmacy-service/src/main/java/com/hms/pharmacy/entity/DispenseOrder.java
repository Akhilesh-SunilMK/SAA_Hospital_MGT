package com.hms.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder-constructed per the pattern-traceability matrix (pharmacy-service: Builder ->
 * DispenseOrder). A dispense order has a variable-length list of items, each independently
 * priced against a resolved batch — a telescoping constructor would be unreadable, and the
 * total amount must be derived, not supplied, so a Builder is the right fit (SRS 4.3, NFR-12).
 * No public setters: the order is immutable once built.
 */
@Entity
@Table(name = "dispense_orders")
@Getter
public class DispenseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "dispensed_by", nullable = false)
    private Long dispensedBy;

    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "dispenseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DispenseItem> items = new ArrayList<>();

    protected DispenseOrder() {
        // required by Hibernate
    }

    private DispenseOrder(Builder b) {
        this.prescriptionId = b.prescriptionId;
        this.patientId = b.patientId;
        this.dispensedBy = b.dispensedBy;
        this.dispensedAt = LocalDateTime.now();
        this.items = b.items;
        this.items.forEach(item -> item.assignOrder(this));
        this.totalAmount = b.items.stream()
                .map(DispenseItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long prescriptionId;
        private Long patientId;
        private Long dispensedBy;
        private final List<DispenseItem> items = new ArrayList<>();

        public Builder prescriptionId(Long v) {
            this.prescriptionId = v;
            return this;
        }

        public Builder patientId(Long v) {
            this.patientId = v;
            return this;
        }

        public Builder dispensedBy(Long v) {
            this.dispensedBy = v;
            return this;
        }

        public Builder addItem(Long drugId, Long batchId, Integer quantity, BigDecimal unitPrice) {
            this.items.add(new DispenseItem(drugId, batchId, quantity, unitPrice));
            return this;
        }

        public DispenseOrder build() {
            Objects.requireNonNull(patientId, "patientId is mandatory");
            Objects.requireNonNull(dispensedBy, "dispensedBy is mandatory");
            if (items.isEmpty()) {
                throw new IllegalArgumentException("A dispense order must contain at least one item");
            }
            return new DispenseOrder(this);
        }
    }
}
