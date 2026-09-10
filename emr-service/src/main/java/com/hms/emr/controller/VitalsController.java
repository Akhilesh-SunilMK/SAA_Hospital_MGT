package com.hms.emr.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.web.TraceIdSupport;
import com.hms.emr.dto.VitalsRequest;
import com.hms.emr.dto.VitalsResponse;
import com.hms.emr.entity.Vitals;
import com.hms.emr.service.VitalsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emr")
public class VitalsController {

    private final VitalsService vitalsService;

    public VitalsController(VitalsService vitalsService) {
        this.vitalsService = vitalsService;
    }

    @PostMapping("/vitals")
    @PreAuthorize("hasAnyRole('NURSE','DOCTOR')")
    public ResponseEntity<ApiResponse<VitalsResponse>> record(@Valid @RequestBody VitalsRequest request) {
        Vitals vitals = vitalsService.record(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(VitalsResponse.from(vitals), "Vitals recorded", TraceIdSupport.current()));
    }
}
