package com.hms.lab.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.web.TraceIdSupport;
import com.hms.lab.dto.TestCatalogueResponse;
import com.hms.lab.service.TestCatalogueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab")
public class TestCatalogueController {

    private final TestCatalogueService testCatalogueService;

    public TestCatalogueController(TestCatalogueService testCatalogueService) {
        this.testCatalogueService = testCatalogueService;
    }

    @GetMapping("/catalogue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TestCatalogueResponse>>> catalogue() {
        List<TestCatalogueResponse> body = testCatalogueService.listAll().stream()
                .map(TestCatalogueResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(body, TraceIdSupport.current()));
    }
}
