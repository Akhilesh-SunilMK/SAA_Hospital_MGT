package com.hms.emr.controller;

import com.hms.common.security.JwtAuthenticationFilter;
import com.hms.emr.factory.ReportFormat;
import com.hms.emr.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** FR-EM-09: patient summary export via the Abstract Factory pipeline (SRS 4.2). */
@RestController
@RequestMapping("/api/v1/emr")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/patients/{id}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> summary(@PathVariable Long id,
                                           @RequestParam ReportFormat format,
                                           HttpServletRequest httpRequest) {
        assertOwnerOrDoctor(id, httpRequest);
        byte[] body = reportService.generateSummary(id, format);
        String filename = "patient-" + id + "-summary." + format.name().toLowerCase();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    private void assertOwnerOrDoctor(Long patientId, HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        Long callerId = attr instanceof Long l ? l : null;
        boolean owner = callerId != null && callerId.equals(patientId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean doctorOrAdmin = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DOCTOR") || a.equals("ROLE_ADMIN"));
        if (!owner && !doctorOrAdmin) {
            throw new AccessDeniedException("Not authorised to export this patient's summary");
        }
    }
}
