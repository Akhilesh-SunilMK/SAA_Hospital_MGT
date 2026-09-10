package com.hms.notification.dto;

import com.hms.notification.model.ChannelType;
import jakarta.validation.constraints.NotNull;

/** Body for PUT /api/v1/notifications/preferences. */
public record UpdatePreferenceRequest(
        @NotNull ChannelType channel,
        boolean enabled
) {
}
