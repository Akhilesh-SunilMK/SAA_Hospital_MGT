package com.hms.lab.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.common.web.TraceIdSupport;
import com.hms.lab.dto.LabOrderRequest;
import com.hms.lab.dto.LabOrderResponse;
import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.dto.StatusUpdateRequest;
import com.hms.lab.entity.LabOrder;
import com.hms.lab.entity.LabResult;
import com.hms.lab.service.LabOrderService;
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

/** Base path matches SRS 6.2.6 exactly so the gateway route {@code Path=/api/v1/lab/**} forwards without rewriting. */
@RestController
@RequestMapping("/api/v1/lab")
public class LabOrderController {

    private final LabOrderService labOrderService;

    public LabOrderController(LabOrderService labOrderService) {
        this.labOrderService = labOrderService;
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> create(@Valid @RequestBody LabOrderRequest request) {
        LabOrder order = labOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(LabOrderResponse.from(order), "Lab order created", TraceIdSupport.current()));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabOrderResponse>> getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        LabOrder order = labOrderService.getById(id);
        assertOwnerOrStaff(order.getPatientId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(LabOrderResponse.from(order), TraceIdSupport.current()));
    }

    @PatchMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('LAB_TECH')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> updateStatus(@PathVariable Long id,
                                                                       @Valid @RequestBody StatusUpdateRequest request) {
        LabOrder order = labOrderService.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.ok(LabOrderResponse.from(order), TraceIdSupport.current()));
    }

    @PostMapping("/orders/{id}/results")
    @PreAuthorize("hasRole('LAB_TECH')")
    public ResponseEntity<ApiResponse<LabResultView>> uploadResult(@PathVariable Long id,
                                                                    @Valid @RequestBody ResultUploadRequest request,
                                                                    HttpServletRequest httpRequest) {
        Long reportedBy = callerUserId(httpRequest);
        LabResult result = labOrderService.uploadResult(id, request, reportedBy);
        LabResultView view = new LabResultView(result.getId(), result.getOrderItemId(), result.getValue(),
                result.getUnit(), result.getReferenceRange(), result.isAbnormalFlag());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(view, "Result uploaded", TraceIdSupport.current()));
    }

    public record LabResultView(Long id, Long orderItemId, String value, String unit, String referenceRange, boolean abnormalFlag) {
    }

    private void assertOwnerOrStaff(Long patientId, HttpServletRequest request) {
        Long callerId = callerUserId(request);
        boolean owner = callerId != null && callerId.equals(patientId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean staff = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> !a.equals("ROLE_PATIENT"));
        if (!owner && !staff) {
            throw new AccessDeniedException("Not authorised to view this lab order");
        }
    }

    private Long callerUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return attr instanceof Long l ? l : null;
    }
}
