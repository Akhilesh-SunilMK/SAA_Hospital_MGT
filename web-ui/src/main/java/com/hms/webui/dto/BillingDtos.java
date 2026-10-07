package com.hms.webui.dto;

import java.math.BigDecimal;
import java.util.List;

public final class BillingDtos {
    private BillingDtos() {}

    public record InvoiceItemRequest(String description, String category, Integer quantity,
                                      BigDecimal unitPrice, BigDecimal taxRate) {}

    public record InvoiceRequest(Long patientId, Long appointmentId, String category, String description,
                                  BigDecimal amount, BigDecimal taxRate, BigDecimal discount,
                                  List<InvoiceItemRequest> items) {}

    public record InvoiceResponse(Long id, String invoiceNo, Long patientId, Long appointmentId,
                                   BigDecimal subtotal, BigDecimal tax, BigDecimal discount, BigDecimal total,
                                   BigDecimal balance, String status) {}

    public record PaymentRequestDto(Long invoiceId, String method, BigDecimal amount, String reference) {}

    public record PaymentResponse(Long paymentId, Long invoiceId, String method, BigDecimal amount,
                                   String transactionRef, String status, String invoiceStatus,
                                   BigDecimal invoiceBalance) {}

    public record RefundRequest(Long paymentId, BigDecimal amount, String reason) {}

    public record RefundResponse(Long refundId, BigDecimal amount, String reason) {}
}
