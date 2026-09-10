package com.hms.doctor.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.web.TraceIdSupport;
import com.hms.doctor.dto.*;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.entity.Leave;
import com.hms.doctor.entity.Schedule;
import com.hms.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Base path matches SRS 6.2.3 so the gateway route {@code Path=/api/v1/doctors/**} forwards as-is. */
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = doctorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(DoctorResponse.from(doctor), "Doctor profile created", TraceIdSupport.current()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(@PathVariable Long id) {
        Doctor doctor = doctorService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(DoctorResponse.from(doctor), TraceIdSupport.current()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> filter(
            @RequestParam(required = false) String specialisation,
            @RequestParam(required = false) String department) {
        List<DoctorResponse> body = doctorService.filter(specialisation, department).stream()
                .map(DoctorResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AvailabilityResponse body = doctorService.availability(id, date);
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> updateSchedule(
            @PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        List<Schedule> schedules = doctorService.replaceSchedule(id, request);
        List<ScheduleResponse> body = schedules.stream().map(ScheduleResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> markLeave(
            @PathVariable Long id, @Valid @RequestBody LeaveRequest request) {
        Leave leave = doctorService.markLeave(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(LeaveResponse.from(leave), "Leave recorded", TraceIdSupport.current()));
    }
}
