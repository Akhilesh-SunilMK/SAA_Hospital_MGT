package com.hms.webui.service;

import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.LabDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabService {

    private final ApiClient api;

    public LabService(ApiClient api) {
        this.api = api;
    }

    public LabOrderResponse createOrder(LabOrderRequest request, SessionUser user) {
        return api.post("/api/v1/lab/orders", request, LabOrderResponse.class, user);
    }

    public LabOrderResponse getOrder(Long id, SessionUser user) {
        return api.get("/api/v1/lab/orders/" + id, LabOrderResponse.class, user);
    }

    public LabOrderResponse updateStatus(Long id, StatusUpdateRequest request, SessionUser user) {
        return api.patch("/api/v1/lab/orders/" + id + "/status", request, LabOrderResponse.class, user);
    }

    public LabResultView uploadResult(Long orderId, ResultUploadRequest request, SessionUser user) {
        return api.post("/api/v1/lab/orders/" + orderId + "/results", request, LabResultView.class, user);
    }

    public List<TestCatalogueResponse> catalogue(SessionUser user) {
        TestCatalogueResponse[] arr = api.get("/api/v1/lab/catalogue", TestCatalogueResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }
}
