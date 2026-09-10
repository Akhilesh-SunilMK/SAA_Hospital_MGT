package com.hms.notification.factory;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.model.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Concrete product — SRS 4.1.1. No Firebase Cloud Messaging credentials provisioned here. */
@Component
public class PushNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationSender.class);

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("[PUSH] to userId={} body='{}'", request.userId(), request.body());
        return NotificationResult.success("push-" + UUID.randomUUID());
    }

    @Override
    public ChannelType getChannel() {
        return ChannelType.PUSH;
    }
}
