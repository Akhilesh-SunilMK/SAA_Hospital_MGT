package com.hms.billing.dto;

import com.hms.billing.entity.Payment;
import com.hms.billing.entity.InvoiceStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long invoiceId,
        String method,
        BigDecimal amount,
        String transactionRef,
        String status,
        InvoiceStatus invoiceStatus,
        BigDecimal invoiceBalance
) {
    public static PaymentResponse of(Payment payment, InvoiceStatus invoiceStatus, BigDecimal balance) {
        return new PaymentResponse(payment.getId(), payment.getInvoiceId(), payment.getMethod(),
                payment.getAmount(), payment.getTransactionRef(), payment.getStatus(), invoiceStatus, balance);
    }
}
