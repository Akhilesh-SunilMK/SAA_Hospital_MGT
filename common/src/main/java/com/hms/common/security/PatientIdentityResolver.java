package com.hms.common.security;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Answers "is the caller this patient?" for owner-or-staff checks outside patient-service.
 *
 * <p>A PATIENT's JWT carries their auth-service user id, while clinical/billing/lab rows store
 * the patient-service patient id; the two are independent sequences, so they can't be compared
 * directly. patient-service owns the user → patient link, so we ask it ({@code GET /patients/me},
 * forwarding the caller's own token) and cache the answer — the link never changes once made.
 * Lookup failures resolve to "not the owner", so they deny access rather than grant it.
 */
@Component
public class PatientIdentityResolver {

    private static final Logger log = LoggerFactory.getLogger(PatientIdentityResolver.class);

    private static final long INTERNAL_TOKEN_TTL_MS = 60_000L;

    private final RestClient restClient;
    private final JwtUtil jwtUtil;
    private final Map<Long, Long> patientIdByUserId = new ConcurrentHashMap<>();
    private final Map<Long, Long> userIdByPatientId = new ConcurrentHashMap<>();

    public PatientIdentityResolver(@Value("${PATIENT_SERVICE_URL:http://localhost:8082}") String patientServiceUrl,
                                   JwtUtil jwtUtil) {
        this.restClient = RestClient.builder().baseUrl(patientServiceUrl).build();
        this.jwtUtil = jwtUtil;
    }

    /**
     * The auth user id linked to a patient, for addressing that patient outside a request (e.g. a
     * notification triggered by a domain event). Null if the patient has no login account.
     */
    public Long userIdForPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        Long cached = userIdByPatientId.get(patientId);
        if (cached != null) {
            return cached;
        }
        String internalToken = "Bearer " + jwtUtil.generateToken(
                "internal-patient-lookup", Map.of("role", "ADMIN", "userId", 0L), INTERNAL_TOKEN_TTL_MS);
        Long resolved = fetchLong("/api/v1/patients/" + patientId, internalToken, "userId");
        if (resolved != null) {
            userIdByPatientId.put(patientId, resolved);
        }
        return resolved;
    }

    /** As {@link #isCallerPatient(Long, HttpServletRequest)}, for the request bound to the current thread. */
    public boolean isCallerPatient(Long patientId) {
        return isCallerPatient(patientId, currentRequest());
    }

    /** True when the caller is a PATIENT whose linked patient profile has the given id. */
    public boolean isCallerPatient(Long patientId, HttpServletRequest request) {
        if (patientId == null) {
            return false;
        }
        return patientId.equals(callerPatientId(request));
    }

    /** As {@link #callerPatientId(HttpServletRequest)}, for the request bound to the current thread. */
    public Long callerPatientId() {
        return callerPatientId(currentRequest());
    }

    /** The caller's patient id, or null if they aren't a PATIENT or have no linked profile yet. */
    public Long callerPatientId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!(attr instanceof Long userId) || authorization == null || !callerHasPatientRole()) {
            return null;
        }
        Long cached = patientIdByUserId.get(userId);
        if (cached != null) {
            return cached;
        }
        Long resolved = fetchLong("/api/v1/patients/me", authorization, "id");
        if (resolved != null) {
            patientIdByUserId.put(userId, resolved);
        }
        return resolved;
    }

    private static HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private static boolean callerHasPatientRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()));
    }

    private Long fetchLong(String uri, String authorization, String field) {
        try {
            JsonNode body = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode value = body == null ? null : body.path("data").path(field);
            return value != null && value.canConvertToLong() ? value.asLong() : null;
        } catch (HttpClientErrorException e) {
            // 403 = caller isn't a PATIENT, 404 = no such profile / none linked; neither is an error here.
            return null;
        } catch (RestClientException e) {
            log.warn("Patient identity lookup {} failed: {}", uri, e.getMessage());
            return null;
        }
    }
}
