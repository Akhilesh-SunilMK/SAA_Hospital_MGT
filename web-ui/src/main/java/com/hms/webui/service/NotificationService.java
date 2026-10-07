package com.hms.webui.service;

import com.hms.common.dto.PageResponse;
import com.hms.webui.client.ApiClient;
import com.hms.webui.dto.NotificationDtos.*;
import com.hms.webui.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class NotificationService {

    private final ApiClient api;

    public NotificationService(ApiClient api) {
        this.api = api;
    }

    public NotificationView send(SendNotificationRequest request, SessionUser user) {
        return api.post("/api/v1/notifications/send", request, NotificationView.class, user);
    }

    public PageResponse<NotificationView> search(Long userId, String status, int page, int size, SessionUser user) {
        String uri = UriComponentsBuilder.fromPath("/api/v1/notifications")
                .queryParamIfPresent("userId", Optional.ofNullable(userId))
                .queryParamIfPresent("status", Optional.ofNullable(status).filter(s -> !s.isBlank()))
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        return api.get(uri, api.pageType(NotificationView.class), user);
    }

    public void updatePreference(UpdatePreferenceRequest request, SessionUser user) {
        api.put("/api/v1/notifications/preferences", request, (Class<Void>) null, user);
    }
}
