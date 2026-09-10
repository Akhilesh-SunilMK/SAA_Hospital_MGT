package com.hms.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "processed_by", nullable = false)
    private Long processedBy;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    public Refund(Long paymentId, BigDecimal amount, String reason, Long processedBy) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reason = reason;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }
}
