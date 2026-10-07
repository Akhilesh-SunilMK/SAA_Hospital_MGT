package com.hms.appointment.controller;

import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.dto.BookAppointmentRequest;
import com.hms.appointment.dto.RescheduleRequest;
import com.hms.appointment.dto.StatusUpdateRequest;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentStatus;
import com.hms.common.security.PatientIdentityResolver;
import com.hms.appointment.service.AppointmentService;
import com.hms.common.dto.ApiResponse;
import com.hms.common.dto.PageResponse;
import com.hms.common.exception.UnauthorizedException;
import com.hms.common.web.TraceIdSupport;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientIdentityResolver patientIdentityResolver;

    public AppointmentController(AppointmentService appointmentService, PatientIdentityResolver patientIdentityResolver) {
        this.appointmentService = appointmentService;
        this.patientIdentityResolver = patientIdentityResolver;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(@Valid @RequestBody BookAppointmentRequest request,
                                                                  Authentication authentication) {
        if (!isStaff(authentication) && !patientIdentityResolver.isCallerPatient(request.patientId())) {
            throw new AccessDeniedException("Patients may only book appointments for themselves");
        }
        AppointmentResponse response = appointmentService.book(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Appointment booked successfully", TraceIdSupport.current()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> get(@PathVariable Long id, Authentication authentication) {
        Appointment appointment = appointmentService.getOrThrow(id);
        assertOwnerOrStaff(appointment, authentication);
        return ResponseEntity.ok(ApiResponse.ok(AppointmentResponse.from(appointment, null), TraceIdSupport.current()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> query(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        int cappedSize = Math.min(size, 100);
        if (!isStaff(authentication)) {
            patientId = patientIdentityResolver.callerPatientId();
            if (patientId == null) {
                // No linked patient profile: an unfiltered query here would leak everyone's appointments.
                return ResponseEntity.ok(ApiResponse.ok(
                        new PageResponse<>(List.of(), page, cappedSize, 0, 0, true), TraceIdSupport.current()));
            }
        }
        Page<Appointment> result = appointmentService.query(patientId, doctorId, status, PageRequest.of(page, cappedSize));
        Page<AppointmentResponse> mapped = result.map(a -> AppointmentResponse.from(a, null));
        if (date != null) {
            List<AppointmentResponse> filtered = mapped.getContent().stream()
                    .filter(a -> a.slot() != null && a.slot().toLocalDate().equals(date))
                    .toList();
            return ResponseEntity.ok(ApiResponse.ok(
                    new PageResponse<>(filtered, page, cappedSize, filtered.size(), 1, true), TraceIdSupport.current()));
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(mapped), TraceIdSupport.current()));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(@PathVariable Long id,
                                                                        @Valid @RequestBody RescheduleRequest request,
                                                                        Authentication authentication) {
        Appointment existing = appointmentService.getOrThrow(id);
        assertOwnerOrReceptionist(existing, authentication);
        Appointment updated = appointmentService.reschedule(id, request.newSlot());
        return ResponseEntity.ok(ApiResponse.ok(AppointmentResponse.from(updated, null), TraceIdSupport.current()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id, Authentication authentication) {
        Appointment existing = appointmentService.getOrThrow(id);
        assertOwnerOrReceptionist(existing, authentication);
        appointmentService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok(null, TraceIdSupport.current()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(@PathVariable Long id,
                                                                          @Valid @RequestBody StatusUpdateRequest request) {
        Appointment updated = appointmentService.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.ok(AppointmentResponse.from(updated, null), TraceIdSupport.current()));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','RECEPTIONIST','ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> queue(@RequestParam Long doctorId) {
        List<AppointmentResponse> queue = appointmentService.liveQueue(doctorId).stream()
                .map(a -> AppointmentResponse.from(a, null))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(queue, TraceIdSupport.current()));
    }

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a ->
                List.of("ROLE_DOCTOR", "ROLE_NURSE", "ROLE_RECEPTIONIST", "ROLE_ADMIN", "ROLE_ACCOUNTANT")
                        .contains(a.getAuthority()));
    }

    private void assertOwnerOrStaff(Appointment appointment, Authentication authentication) {
        if (isStaff(authentication)) {
            return;
        }
        if (!patientIdentityResolver.isCallerPatient(appointment.getPatientId())) {
            throw new UnauthorizedException("You are not permitted to view this appointment");
        }
    }

    private void assertOwnerOrReceptionist(Appointment appointment, Authentication authentication) {
        boolean receptionist = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST") || a.getAuthority().equals("ROLE_ADMIN"));
        if (receptionist) {
            return;
        }
        if (!patientIdentityResolver.isCallerPatient(appointment.getPatientId())) {
            throw new UnauthorizedException("You are not permitted to modify this appointment");
        }
    }
}
