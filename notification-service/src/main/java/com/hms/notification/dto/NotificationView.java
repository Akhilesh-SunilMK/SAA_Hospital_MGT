package com.hms.notification.dto;

import com.hms.notification.entity.Notification;
import com.hms.notification.model.ChannelType;
import com.hms.notification.model.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationView(
        Long id,
        Long userId,
        ChannelType channel,
        String templateCode,
        NotificationStatus status,
        int attempts,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
    public static NotificationView from(Notification n) {
        return new NotificationView(n.getId(), n.getUserId(), n.getChannel(), n.getTemplateCode(),
                n.getStatus(), n.getAttempts(), n.getSentAt(), n.getCreatedAt());
    }
}
