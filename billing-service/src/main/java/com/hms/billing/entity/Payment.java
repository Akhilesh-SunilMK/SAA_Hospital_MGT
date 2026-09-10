package com.hms.billing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String status;

    public Payment(Long invoiceId, String method, BigDecimal amount, String transactionRef, String status) {
        this.invoiceId = invoiceId;
        this.method = method;
        this.amount = amount;
        this.transactionRef = transactionRef;
        this.status = status;
        this.paidAt = LocalDateTime.now();
    }
}
