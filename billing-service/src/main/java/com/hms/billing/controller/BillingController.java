package com.hms.billing.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.dto.PageResponse;
import com.hms.common.security.PatientIdentityResolver;
import com.hms.common.web.TraceIdSupport;
import com.hms.billing.dto.*;
import com.hms.billing.entity.Invoice;
import com.hms.billing.entity.InvoiceStatus;
import com.hms.billing.entity.Refund;
import com.hms.billing.factory.report.ReportData;
import com.hms.billing.factory.report.ReportFormat;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.billing.service.InvoiceService;
import com.hms.billing.service.PaymentService;
import com.hms.billing.service.ReportService;
import com.hms.billing.service.RefundService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String USER_ID_ATTR = "hms.userId";

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final ReportService reportService;
    private final InvoiceRepository invoiceRepository;
    private final PatientIdentityResolver patientIdentityResolver;

    public BillingController(InvoiceService invoiceService, PaymentService paymentService,
                              RefundService refundService, ReportService reportService,
                              InvoiceRepository invoiceRepository, PatientIdentityResolver patientIdentityResolver) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.reportService = reportService;
        this.invoiceRepository = invoiceRepository;
        this.patientIdentityResolver = patientIdentityResolver;
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','RECEPTIONIST','ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse created = invoiceService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(created, "Invoice generated successfully", TraceIdSupport.current()));
    }

    /**
     * OWNER check simplification (documented, same as elsewhere in the system): the invoice's
     * patient_id is compared directly against the caller's hms.userId rather than resolving it
     * through patient-service's user_id mapping.
     */
    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN','PATIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Long id, HttpServletRequest httpRequest) {
        InvoiceResponse invoice = invoiceService.getById(id);
        assertOwnerOrStaff(invoice.patientId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(invoice, TraceIdSupport.current()));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN','PATIENT')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> queryInvoices(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            HttpServletRequest httpRequest) {
        if (isPatientRole()) {
            // a PATIENT caller may only query their own invoices
            patientId = patientIdentityResolver.callerPatientId(httpRequest);
        }
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        if (isPatientRole() && patientId == null) {
            // No linked patient profile: an unfiltered query here would leak everyone's invoices.
            return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(Page.<InvoiceResponse>empty(pageable)), TraceIdSupport.current()));
        }
        PageResponse<InvoiceResponse> result = PageResponse.from(invoiceService.query(patientId, status, pageable));
        return ResponseEntity.ok(ApiResponse.ok(result, TraceIdSupport.current()));
    }

    private void assertOwnerOrStaff(Long invoicePatientId, HttpServletRequest httpRequest) {
        if (!isPatientRole()) {
            return;
        }
        if (!patientIdentityResolver.isCallerPatient(invoicePatientId, httpRequest)) {
            throw new org.springframework.security.access.AccessDeniedException("Not the owner of this invoice");
        }
    }

    private boolean isPatientRole() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody PaymentRequestDto request) {
        PaymentResponse response = paymentService.record(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(response, "Payment recorded successfully", TraceIdSupport.current()));
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> issueRefund(@Valid @RequestBody RefundRequest request,
                                                                         HttpServletRequest httpRequest) {
        Long processedBy = (Long) httpRequest.getAttribute(USER_ID_ATTR);
        Refund refund = refundService.issue(request, processedBy);
        Map<String, Object> body = Map.of("refundId", refund.getId(), "amount", refund.getAmount(),
                "reason", refund.getReason());
        return ResponseEntity.status(201)
                .body(ApiResponse.created(body, "Refund issued successfully", TraceIdSupport.current()));
    }

    @GetMapping("/reports/revenue")
    @PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN')")
    public ResponseEntity<byte[]> revenueReport(
            @RequestParam LocalDate from, @RequestParam LocalDate to,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(inv -> !inv.getCreatedAt().toLocalDate().isBefore(from)
                        && !inv.getCreatedAt().toLocalDate().isAfter(to))
                .toList();

        List<Map<String, Object>> rows = invoices.stream()
                .map(inv -> Map.<String, Object>of(
                        "invoiceNo", inv.getInvoiceNo(),
                        "patientId", inv.getPatientId(),
                        "total", inv.getTotal(),
                        "status", inv.getStatus().name()))
                .toList();

        byte[] content = reportService.generate(format, new ReportData("Revenue Report " + from + " to " + to, rows));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=revenue-report." + format.name().toLowerCase())
                .body(content);
    }
}
