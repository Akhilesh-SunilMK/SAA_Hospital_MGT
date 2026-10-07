package com.hms.webui.service;

import com.hms.common.dto.PageResponse;
import com.hms.webui.client.ApiClient;
import com.hms.webui.client.DownloadResult;
import com.hms.webui.dto.BillingDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BillingService {

    private final ApiClient api;

    public BillingService(ApiClient api) {
        this.api = api;
    }

    public InvoiceResponse createInvoice(InvoiceRequest request, SessionUser user) {
        return api.post("/api/v1/billing/invoices", request, InvoiceResponse.class, user);
    }

    public InvoiceResponse getInvoice(Long id, SessionUser user) {
        return api.get("/api/v1/billing/invoices/" + id, InvoiceResponse.class, user);
    }

    public PageResponse<InvoiceResponse> search(Long patientId, String status, int page, int size, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/billing/invoices")
                .queryParamIfPresent("patientId", Optional.ofNullable(patientId))
                .queryParamIfPresent("status", Optional.ofNullable(status).filter(s -> !s.isBlank()))
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        return api.get(uri, api.pageType(InvoiceResponse.class), user);
    }

    public PaymentResponse recordPayment(PaymentRequestDto request, SessionUser user) {
        return api.post("/api/v1/billing/payments", request, PaymentResponse.class, user);
    }

    public RefundResponse recordRefund(RefundRequest request, SessionUser user) {
        return api.post("/api/v1/billing/refunds", request, RefundResponse.class, user);
    }

    public DownloadResult revenueReport(LocalDate from, LocalDate to, String format, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/billing/reports/revenue")
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("format", format)
                .toUriString();
        return api.download(uri, user);
    }
}
