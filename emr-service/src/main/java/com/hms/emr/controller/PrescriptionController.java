package com.hms.emr.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.common.web.TraceIdSupport;
import com.hms.emr.dto.PrescriptionRequest;
import com.hms.emr.dto.PrescriptionResponse;
import com.hms.emr.entity.Prescription;
import com.hms.emr.service.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emr")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(@Valid @RequestBody PrescriptionRequest request) {
        Prescription prescription = prescriptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(PrescriptionResponse.from(prescription), "Prescription created", TraceIdSupport.current()));
    }

    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Prescription prescription = prescriptionService.getById(id);
        assertOwnerOrDoctorOrPharmacist(prescription.getPatientId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(PrescriptionResponse.from(prescription), TraceIdSupport.current()));
    }

    private void assertOwnerOrDoctorOrPharmacist(Long patientId, HttpServletRequest request) {
        Long callerId = callerUserId(request);
        boolean owner = callerId != null && callerId.equals(patientId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean doctorOrPharmacist = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DOCTOR") || a.equals("ROLE_PHARMACIST") || a.equals("ROLE_ADMIN"));
        if (!owner && !doctorOrPharmacist) {
            throw new AccessDeniedException("Not authorised to view this prescription");
        }
    }

    private Long callerUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return attr instanceof Long l ? l : null;
    }
}
