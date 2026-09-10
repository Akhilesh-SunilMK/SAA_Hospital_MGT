package com.hms.pharmacy.entity;

import com.hms.common.exception.BusinessRuleException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Optimistic-locked (SRS 8.2: "Optimistic locking (@Version) SHALL be applied to ...
 * stock_batches") so concurrent dispensing against the same batch is caught safely.
 * No public setter for quantity — decrements only via {@link #decrement(int)}, which also
 * enforces "stock quantity SHALL never fall below zero" (SRS 8.2).
 */
@Entity
@Table(name = "stock_batches")
@Getter
@NoArgsConstructor
public class StockBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Version
    private Long version;

    public StockBatch(Long drugId, String batchNo, Integer quantity, LocalDate expiryDate) {
        this.drugId = drugId;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public void increment(int amount) {
        this.quantity += amount;
    }

    public void decrement(int amount) {
        if (amount > this.quantity) {
            throw new BusinessRuleException("Insufficient quantity in batch " + batchNo);
        }
        int result = this.quantity - amount;
        if (result < 0) {
            throw new BusinessRuleException("Stock quantity cannot go below zero for batch " + batchNo);
        }
        this.quantity = result;
    }
}
