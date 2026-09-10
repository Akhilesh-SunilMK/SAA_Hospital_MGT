package com.hms.notification.dto;

import com.hms.notification.model.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** Body for POST /api/v1/notifications/send. */
public record SendNotificationRequest(
        @NotNull Long userId,
        @NotNull ChannelType channel,
        @NotBlank String templateCode,
        Map<String, String> variables
) {
    public Map<String, String> variablesOrEmpty() {
        return variables != null ? variables : Map.of();
    }
}
