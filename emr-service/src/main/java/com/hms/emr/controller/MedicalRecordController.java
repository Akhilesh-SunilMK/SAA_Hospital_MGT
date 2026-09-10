package com.hms.emr.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.common.web.TraceIdSupport;
import com.hms.emr.dto.AmendRecordRequest;
import com.hms.emr.dto.CreateRecordRequest;
import com.hms.emr.dto.RecordResponse;
import com.hms.emr.entity.MedicalRecord;
import com.hms.emr.service.MedicalRecordService;
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

import java.util.List;

/** Base path matches SRS 6.2.5 exactly so the gateway route {@code Path=/api/v1/emr/**} forwards without rewriting. */
@RestController
@RequestMapping("/api/v1/emr")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping("/records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<RecordResponse>> create(@Valid @RequestBody CreateRecordRequest request,
                                                               HttpServletRequest httpRequest) {
        MedicalRecord record = medicalRecordService.create(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(RecordResponse.from(record), "Medical record created", TraceIdSupport.current()));
    }

    /**
     * Method-security only requires an authenticated caller; {@link #assertOwnerOrClinicalStaff}
     * does the finer-grained check below. ADMIN passes that check too, so billing-service's
     * internal Feign call (SRS 3.3, minted with role=ADMIN) for charge-item lookup succeeds.
     */
    @GetMapping("/records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RecordResponse>> getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        MedicalRecord record = medicalRecordService.getById(id, httpRequest);
        assertOwnerOrClinicalStaff(record.getPatientId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(RecordResponse.from(record), TraceIdSupport.current()));
    }

    @GetMapping("/patients/{patientId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> history(@PathVariable Long patientId, HttpServletRequest httpRequest) {
        assertOwnerOrClinicalStaff(patientId, httpRequest);
        List<RecordResponse> body = medicalRecordService.history(patientId).stream().map(RecordResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @PostMapping("/records/{id}/amend")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<RecordResponse>> amend(@PathVariable Long id,
                                                              @Valid @RequestBody AmendRecordRequest request,
                                                              HttpServletRequest httpRequest) {
        Long amendedBy = callerUserId(httpRequest);
        MedicalRecord record = medicalRecordService.amend(id, request, amendedBy, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(RecordResponse.from(record), TraceIdSupport.current()));
    }

    // Simplification (documented): patientId on the record is compared directly against the
    // caller's hms.userId for the OWNER check, since emr-service has no cross-service call to
    // patient-service to resolve patient.userId (out of scope per SRS 3.3's dependency list).
    private void assertOwnerOrClinicalStaff(Long patientId, HttpServletRequest request) {
        Long callerId = callerUserId(request);
        boolean owner = callerId != null && callerId.equals(patientId);
        boolean clinicalStaff = isClinicalStaffOrAdmin();
        if (!owner && !clinicalStaff) {
            throw new AccessDeniedException("Not authorised to view this patient's clinical data");
        }
    }

    private boolean isClinicalStaffOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> !a.equals("ROLE_PATIENT"));
    }

    private Long callerUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return attr instanceof Long l ? l : null;
    }
}
