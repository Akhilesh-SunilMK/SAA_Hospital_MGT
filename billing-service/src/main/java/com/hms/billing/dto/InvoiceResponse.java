package com.hms.billing.dto;

import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.InvoiceStatus;

import java.math.BigDecimal;

public record InvoiceResponse(
        Long id,
        String invoiceNo,
        Long patientId,
        Long appointmentId,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal balance,
        InvoiceStatus status
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNo(), invoice.getPatientId(),
                invoice.getAppointmentId(), invoice.getSubtotal(), invoice.getTax(), invoice.getDiscount(),
                invoice.getTotal(), invoice.getBalance(), invoice.getStatus());
    }
}
