package com.hms.notification.dto;

import com.hms.notification.model.ChannelType;

/** What a {@code NotificationSender} needs to actually dispatch a rendered message. */
public record NotificationRequest(
        Long userId,
        ChannelType channel,
        String subject,
        String body
) {
}
