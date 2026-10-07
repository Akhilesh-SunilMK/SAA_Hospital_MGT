package com.hms.webui.web;

import com.hms.common.dto.PageResponse;
import com.hms.common.security.Role;
import com.hms.webui.client.DownloadResult;
import com.hms.webui.dto.BillingDtos.*;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.CurrentUser;
import com.hms.webui.security.Guard;
import com.hms.webui.security.SessionUser;
import com.hms.webui.service.BillingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/billing")
public class BillingWebController {

    private final BillingService billingService;

    public BillingWebController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/invoices")
    public String list(@RequestParam(required = false) Long patientId, @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.PATIENT, Role.ACCOUNTANT, Role.ADMIN);
        Long effectivePatientId = user.isPatient() ? user.patientId() : patientId;
        PageResponse<InvoiceResponse> results = billingService.search(effectivePatientId, status, page, 20, user);
        model.addAttribute("results", results);
        model.addAttribute("patientId", patientId);
        model.addAttribute("status", status);
        return "billing/invoices";
    }

    @GetMapping("/invoices/new")
    public String newForm(@RequestParam(required = false) Long patientId, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.ACCOUNTANT, Role.RECEPTIONIST, Role.ADMIN);
        model.addAttribute("patientId", patientId);
        return "billing/invoice-form";
    }

    @PostMapping("/invoices/new")
    public String create(@CurrentUser SessionUser user,
                          @RequestParam Long patientId, @RequestParam(required = false) Long appointmentId,
                          @RequestParam String category, @RequestParam(required = false) String description,
                          @RequestParam BigDecimal amount, @RequestParam(required = false) BigDecimal taxRate,
                          @RequestParam(required = false) BigDecimal discount, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ACCOUNTANT, Role.RECEPTIONIST, Role.ADMIN);
        try {
            InvoiceResponse invoice = billingService.createInvoice(new InvoiceRequest(patientId, appointmentId, category,
                    description, amount, taxRate, discount, null), user);
            redirectAttributes.addFlashAttribute("success", "Invoice " + invoice.invoiceNo() + " created (balance "
                    + invoice.balance() + ").");
            // RECEPTIONIST can't view GET /invoices/{id} on the backend (see the same note on
            // recordPayment below) — send them straight to record a payment against it instead.
            if (user.isAccountant() || user.isAdmin()) {
                return "redirect:/billing/invoices/" + invoice.id();
            }
            return "redirect:/billing/payments/new?invoiceId=" + invoice.id();
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing/invoices/new?patientId=" + patientId;
        }
    }

    @GetMapping("/invoices/{id}")
    public String detail(@PathVariable Long id, @CurrentUser SessionUser user, Model model) {
        model.addAttribute("invoice", billingService.getInvoice(id, user));
        return "billing/invoice-detail";
    }

    // Standalone entry point: the backend allows RECEPTIONIST to record payments but not to
    // view GET /invoices/{id}, so they can't reach the payment form embedded in the invoice
    // detail page (see invoice-detail.html) — this page lets them record one by invoice number
    // alone.
    @GetMapping("/payments/new")
    public String newPaymentForm(@RequestParam(required = false) Long invoiceId, @CurrentUser SessionUser user, Model model) {
        Guard.require(user, Role.ACCOUNTANT, Role.RECEPTIONIST);
        model.addAttribute("invoiceId", invoiceId);
        return "billing/payment-form";
    }

    @PostMapping("/payments/new")
    public String recordPayment(@CurrentUser SessionUser user,
                                 @RequestParam Long invoiceId, @RequestParam String method, @RequestParam BigDecimal amount,
                                 @RequestParam(required = false) String reference, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ACCOUNTANT, Role.RECEPTIONIST);
        try {
            PaymentResponse p = billingService.recordPayment(new PaymentRequestDto(invoiceId, method, amount, reference), user);
            redirectAttributes.addFlashAttribute("success", "Payment #" + p.paymentId() + " recorded. Invoice balance: " + p.invoiceBalance() + ".");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing/payments/new?invoiceId=" + invoiceId;
        }
        // RECEPTIONIST can't view GET /invoices/{id} on the backend, so send them back to the
        // standalone form (with the success flash) instead of a page that would 403 on them.
        return user.isAccountant() ? "redirect:/billing/invoices/" + invoiceId : "redirect:/billing/payments/new";
    }

    @PostMapping("/refunds/new")
    public String recordRefund(@CurrentUser SessionUser user,
                                @RequestParam Long paymentId, @RequestParam BigDecimal amount, @RequestParam String reason,
                                @RequestParam Long invoiceId, RedirectAttributes redirectAttributes) {
        Guard.require(user, Role.ACCOUNTANT, Role.ADMIN);
        try {
            billingService.recordRefund(new RefundRequest(paymentId, amount, reason), user);
            redirectAttributes.addFlashAttribute("success", "Refund recorded.");
        } catch (ApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billing/invoices/" + invoiceId;
    }

    @GetMapping("/reports/revenue")
    public String revenueForm(@CurrentUser SessionUser user) {
        Guard.require(user, Role.ACCOUNTANT, Role.ADMIN);
        return "billing/revenue-report";
    }

    @GetMapping("/reports/revenue/download")
    public ResponseEntity<byte[]> downloadRevenue(@RequestParam LocalDate from, @RequestParam LocalDate to,
                                                   @RequestParam(defaultValue = "PDF") String format,
                                                   @CurrentUser SessionUser user) {
        Guard.require(user, Role.ACCOUNTANT, Role.ADMIN);
        DownloadResult result = billingService.revenueReport(from, to, format, user);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .body(result.bytes());
    }
}
