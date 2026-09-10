package com.hms.appointment.dto;

import java.math.BigDecimal;

/** Body for the Saga's CreateProvisionalInvoice step (POST /api/v1/billing/invoices). */
public record CreateInvoiceRequest(
        Long patientId,
        Long appointmentId,
        String category,
        String description,
        BigDecimal amount
) {
}
