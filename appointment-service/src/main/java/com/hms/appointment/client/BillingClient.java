package com.hms.appointment.client;

import com.hms.appointment.dto.CreateInvoiceRequest;
import com.hms.appointment.dto.CreateInvoiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Saga step 2 (SRS 3.4): CreateProvisionalInvoice. Called with a short-lived internal token
 * (see BookingOrchestrationService) since billing's own role check requires
 * ACCOUNTANT/RECEPTIONIST/ADMIN, which the original caller (e.g. a PATIENT) may not hold.
 */
@FeignClient(name = "billing-service")
public interface BillingClient {

    @PostMapping("/api/v1/billing/invoices")
    CreateInvoiceResponse createInvoice(@RequestBody CreateInvoiceRequest request,
                                         @RequestHeader("Authorization") String authorization);
}
