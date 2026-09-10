package com.hms.notification.factory;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.model.ChannelType;

/** Product interface — SRS 4.1.1 Factory Method pattern. */
public interface NotificationSender {
    NotificationResult send(NotificationRequest request);

    ChannelType getChannel();
}
