package com.hms.billing.client;

import com.hms.common.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Optional SRS 3.3 integration: billing-service calling emr-service for real consultation
 * charge items. Not wired into the core invoice-creation flow (the Saga call from
 * appointment-service supplies its own amount) - available for a future "generate invoice from
 * consultation" flow. Demonstrates the internal-service-token pattern used across this system:
 * a short-lived ADMIN-role token minted locally, since the original caller's JWT may not carry
 * a role emr-service's own endpoint accepts for a service-to-service call.
 */
@Service
public class ConsultationChargeLookupService {

    private final EmrClient emrClient;
    private final JwtUtil jwtUtil;

    public ConsultationChargeLookupService(EmrClient emrClient, JwtUtil jwtUtil) {
        this.emrClient = emrClient;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> lookup(Long emrRecordId) {
        String internalToken = jwtUtil.generateToken("billing-service",
                Map.of("role", "ADMIN", "userId", 0L), 60_000L);
        return emrClient.getRecord(emrRecordId, "Bearer " + internalToken);
    }
}
