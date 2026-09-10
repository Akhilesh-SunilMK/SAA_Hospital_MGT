package com.hms.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "dispense_items")
@Getter
@NoArgsConstructor
public class DispenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispense_order_id", nullable = false)
    private DispenseOrder dispenseOrder;

    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    DispenseItem(Long drugId, Long batchId, Integer quantity, BigDecimal unitPrice) {
        this.drugId = drugId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void assignOrder(DispenseOrder order) {
        this.dispenseOrder = order;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
