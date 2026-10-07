package com.hms.webui.service;

import com.hms.common.dto.PageResponse;
import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.PharmacyDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
public class PharmacyService {

    private final ApiClient api;

    public PharmacyService(ApiClient api) {
        this.api = api;
    }

    public PageResponse<DrugResponse> drugs(String search, int page, int size, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/pharmacy/drugs")
                .queryParamIfPresent("search", Optional.ofNullable(search).filter(s -> !s.isBlank()))
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        return api.get(uri, api.pageType(DrugResponse.class), user);
    }

    public DrugResponse createDrug(DrugRequest request, SessionUser user) {
        return api.post("/api/v1/pharmacy/drugs", request, DrugResponse.class, user);
    }

    public void adjustStock(Long drugId, StockAdjustRequest request, SessionUser user) {
        api.patch("/api/v1/pharmacy/stock/" + drugId, request, (Class<Void>) null, user);
    }

    public DispenseResponse dispense(DispenseRequest request, SessionUser user) {
        return api.post("/api/v1/pharmacy/dispense", request, DispenseResponse.class, user);
    }

    public List<LowStockResponse> lowStock(SessionUser user) {
        LowStockResponse[] arr = api.get("/api/v1/pharmacy/stock/low", LowStockResponse[].class, user);
        return arr == null ? List.of() : List.of(arr);
    }
}
