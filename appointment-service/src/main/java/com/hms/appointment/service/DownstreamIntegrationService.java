package com.hms.appointment.service;

import com.hms.appointment.client.BillingClient;
import com.hms.appointment.client.DoctorClient;
import com.hms.appointment.client.PatientClient;
import com.hms.appointment.dto.CreateInvoiceRequest;
import com.hms.appointment.dto.CreateInvoiceResponse;
import com.hms.appointment.dto.DoctorAvailabilityResponse;
import com.hms.appointment.dto.DoctorResponse;
import com.hms.appointment.dto.DoctorSummary;
import com.hms.appointment.dto.PatientResponse;
import com.hms.appointment.security.InternalTokenProvider;
import com.hms.appointment.security.RequestAuthHeaderProvider;
import com.hms.common.exception.BusinessRuleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Wraps every outbound Feign call in a Resilience4j circuit breaker (SRS 3.3/NFR-19) so a
 * downstream outage degrades to a clear error instead of hanging or cascading.
 */
@Service
public class DownstreamIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(DownstreamIntegrationService.class);

    private final DoctorClient doctorClient;
    private final PatientClient patientClient;
    private final BillingClient billingClient;
    private final RequestAuthHeaderProvider authHeaderProvider;
    private final InternalTokenProvider internalTokenProvider;

    public DownstreamIntegrationService(DoctorClient doctorClient, PatientClient patientClient,
                                         BillingClient billingClient, RequestAuthHeaderProvider authHeaderProvider,
                                         InternalTokenProvider internalTokenProvider) {
        this.doctorClient = doctorClient;
        this.patientClient = patientClient;
        this.billingClient = billingClient;
        this.authHeaderProvider = authHeaderProvider;
        this.internalTokenProvider = internalTokenProvider;
    }

    @CircuitBreaker(name = "doctorService", fallbackMethod = "doctorFallback")
    public DoctorSummary fetchDoctor(Long doctorId) {
        DoctorResponse response = doctorClient.getDoctor(doctorId, authHeaderProvider.currentAuthorizationHeader());
        return response != null ? response.data() : null;
    }

    @SuppressWarnings("unused")
    private DoctorSummary doctorFallback(Long doctorId, Throwable ex) {
        log.warn("doctor-service unavailable while fetching doctor {}: {}", doctorId, ex.toString());
        throw new BusinessRuleException("Doctor service is currently unavailable. Please try again shortly.");
    }

    @CircuitBreaker(name = "doctorService", fallbackMethod = "availabilityFallback")
    public DoctorAvailabilityResponse.AvailabilityData fetchAvailability(Long doctorId, java.time.LocalDate date) {
        DoctorAvailabilityResponse response = doctorClient.getAvailability(doctorId, date.toString(), authHeaderProvider.currentAuthorizationHeader());
        return response != null ? response.data() : null;
    }

    @SuppressWarnings("unused")
    private DoctorAvailabilityResponse.AvailabilityData availabilityFallback(Long doctorId, java.time.LocalDate date, Throwable ex) {
        log.warn("doctor-service unavailable while checking availability for doctor {} on {}: {}", doctorId, date, ex.toString());
        throw new BusinessRuleException("Doctor service is currently unavailable. Please try again shortly.");
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "patientFallback")
    public PatientResponse.PatientSummary fetchPatient(Long patientId) {
        PatientResponse response = patientClient.getPatient(patientId, authHeaderProvider.currentAuthorizationHeader());
        return response != null ? response.data() : null;
    }

    @SuppressWarnings("unused")
    private PatientResponse.PatientSummary patientFallback(Long patientId, Throwable ex) {
        log.warn("patient-service unavailable while fetching patient {}: {}", patientId, ex.toString());
        throw new BusinessRuleException("Patient service is currently unavailable. Please try again shortly.");
    }

    @CircuitBreaker(name = "billingService", fallbackMethod = "invoiceFallback")
    public CreateInvoiceResponse createProvisionalInvoice(CreateInvoiceRequest request) {
        return billingClient.createInvoice(request, internalTokenProvider.bearerToken());
    }

    @SuppressWarnings("unused")
    private CreateInvoiceResponse invoiceFallback(CreateInvoiceRequest request, Throwable ex) {
        log.error("billing-service unavailable while creating provisional invoice for appointment {}: {}",
                request.appointmentId(), ex.toString());
        throw new BusinessRuleException("Unable to create the provisional invoice; the booking has been rolled back.");
    }
}
