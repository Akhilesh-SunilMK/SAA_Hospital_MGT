package com.hms.pharmacy.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.dto.PageResponse;
import com.hms.common.web.TraceIdSupport;
import com.hms.pharmacy.dto.*;
import com.hms.pharmacy.service.DispenseService;
import com.hms.pharmacy.service.DrugService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
public class PharmacyController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String USER_ID_ATTR = "hms.userId";

    private final DrugService drugService;
    private final DispenseService dispenseService;

    public PharmacyController(DrugService drugService, DispenseService dispenseService) {
        this.drugService = drugService;
        this.dispenseService = dispenseService;
    }

    @GetMapping("/drugs")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','RECEPTIONIST','LAB_TECH','PHARMACIST','ACCOUNTANT','ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<DrugResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        PageResponse<DrugResponse> result = PageResponse.from(drugService.search(search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(result, TraceIdSupport.current()));
    }

    @PostMapping("/drugs")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<DrugResponse>> createDrug(@Valid @RequestBody DrugRequest request) {
        DrugResponse created = drugService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(created, "Drug added successfully", TraceIdSupport.current()));
    }

    @PatchMapping("/stock/{drugId}")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<Void>> adjustStock(@PathVariable Long drugId,
                                                          @Valid @RequestBody StockAdjustRequest request) {
        drugService.adjustStock(drugId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, TraceIdSupport.current()));
    }

    @PostMapping("/dispense")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<DispenseResponse>> dispense(@Valid @RequestBody DispenseRequest request,
                                                                   HttpServletRequest httpRequest) {
        Long dispensedBy = (Long) httpRequest.getAttribute(USER_ID_ATTR);
        DispenseResponse response = dispenseService.dispense(request, dispensedBy);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(response, "Prescription dispensed successfully", TraceIdSupport.current()));
    }

    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<ApiResponse<List<LowStockResponse>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.ok(drugService.lowStock(), TraceIdSupport.current()));
    }
}
