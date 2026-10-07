package com.hms.patient.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.dto.PageResponse;
import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.common.web.TraceIdSupport;
import com.hms.patient.dto.*;
import com.hms.patient.entity.Admission;
import com.hms.patient.entity.EmergencyContact;
import com.hms.patient.entity.Patient;
import com.hms.patient.entity.PatientAllergy;
import com.hms.patient.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base path matches SRS 6.2.2 exactly so the gateway route {@code Path=/api/v1/patients/**}
 * forwards without rewriting.
 */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private static final String STAFF_ROLES =
            "hasAnyRole('DOCTOR','NURSE','RECEPTIONIST','LAB_TECH','PHARMACIST','ACCOUNTANT','ADMIN')";

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponse>> register(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(PatientResponse.from(patient), "Patient registered successfully", TraceIdSupport.current()));
    }

    /**
     * The caller's own patient profile. Auth user ids and patient ids are separate sequences, so
     * other services resolve "is this caller the patient?" through here (see PatientIdentityResolver).
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> me(HttpServletRequest request) {
        Long userId = callerUserId(request);
        if (userId == null) {
            throw new AccessDeniedException("Token carries no user id");
        }
        return ResponseEntity.ok(ApiResponse.ok(PatientResponse.from(patientService.getByUserId(userId)), TraceIdSupport.current()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(@PathVariable Long id, HttpServletRequest request) {
        Patient patient = patientService.getById(id);
        patientService.assertOwnerOrStaff(patient, callerUserId(request), isStaff());
        return ResponseEntity.ok(ApiResponse.ok(PatientResponse.from(patient), TraceIdSupport.current()));
    }

    @GetMapping
    @PreAuthorize(STAFF_ROLES)
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Patient> result = patientService.search(search, page, size);
        PageResponse<PatientResponse> body = PageResponse.from(result.map(PatientResponse::from));
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody PatientUpdateRequest request) {
        Patient patient = patientService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(PatientResponse.from(patient), TraceIdSupport.current()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        patientService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/admit")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','DOCTOR')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> admit(@PathVariable Long id,
                                                                 @Valid @RequestBody AdmitRequest request) {
        Admission admission = patientService.admit(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(AdmissionResponse.from(admission), "Patient admitted to IPD", TraceIdSupport.current()));
    }

    @PostMapping("/{id}/discharge")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> discharge(@PathVariable Long id) {
        Admission admission = patientService.discharge(id);
        return ResponseEntity.ok(ApiResponse.ok(AdmissionResponse.from(admission), TraceIdSupport.current()));
    }

    @PostMapping("/{id}/allergies")
    @PreAuthorize(STAFF_ROLES)
    public ResponseEntity<ApiResponse<AllergyResponse>> addAllergy(@PathVariable Long id,
                                                                    @Valid @RequestBody AllergyRequest request) {
        PatientAllergy allergy = patientService.addAllergy(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(AllergyResponse.from(allergy), "Allergy recorded", TraceIdSupport.current()));
    }

    // open-in-view is disabled (see application.yml), so the lazy `allergies`/`emergencyContacts`
    // collections must be touched inside an active transaction, not after getById() returns.
    @GetMapping("/{id}/allergies")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<AllergyResponse>>> listAllergies(@PathVariable Long id, HttpServletRequest request) {
        Patient patient = patientService.getById(id);
        patientService.assertOwnerOrStaff(patient, callerUserId(request), isStaff());
        List<AllergyResponse> body = patient.getAllergies().stream().map(AllergyResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @PostMapping("/{id}/emergency-contacts")
    @PreAuthorize(STAFF_ROLES)
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> addEmergencyContact(
            @PathVariable Long id, @Valid @RequestBody EmergencyContactRequest request) {
        EmergencyContact contact = patientService.addEmergencyContact(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EmergencyContactResponse.from(contact), "Emergency contact added", TraceIdSupport.current()));
    }

    @GetMapping("/{id}/emergency-contacts")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> listEmergencyContacts(
            @PathVariable Long id, HttpServletRequest request) {
        Patient patient = patientService.getById(id);
        patientService.assertOwnerOrStaff(patient, callerUserId(request), isStaff());
        List<EmergencyContactResponse> body = patient.getEmergencyContacts().stream()
                .map(EmergencyContactResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    private Long callerUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return attr instanceof Long l ? l : null;
    }

    private boolean isStaff() {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> !a.equals("ROLE_PATIENT"));
    }
}
