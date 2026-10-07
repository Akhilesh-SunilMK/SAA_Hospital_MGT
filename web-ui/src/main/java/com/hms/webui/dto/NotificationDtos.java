package com.hms.webui.dto;

import java.util.Map;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record SendNotificationRequest(Long userId, String channel, String templateCode,
                                           Map<String, String> variables) {}

    public record NotificationView(Long id, Long userId, String channel, String templateCode, String status,
                                    Integer attempts, String sentAt, String createdAt) {}

    public record UpdatePreferenceRequest(String channel, boolean enabled) {}
}
