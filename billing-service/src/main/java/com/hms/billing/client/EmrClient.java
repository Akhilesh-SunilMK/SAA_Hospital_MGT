package com.hms.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * SRS 3.3: "Billing Service -> EMR Service (GET .../emr/consultations/{id} for charge items)".
 * EMR only exposes GET /api/v1/emr/records/{id} (no separate "consultations" resource), so this
 * targets that endpoint instead - see billing-service's coordination note with emr-service.
 */
@FeignClient(name = "emr-service")
public interface EmrClient {

    @GetMapping("/api/v1/emr/records/{id}")
    Map<String, Object> getRecord(@PathVariable("id") Long recordId, @RequestHeader("Authorization") String bearerToken);
}
