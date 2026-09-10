package com.hms.appointment.dto;

public record CreateInvoiceResponse(boolean success, int statusCode, String message, InvoiceSummary data) {

    public record InvoiceSummary(Long id, String invoiceNo) {
    }
}
