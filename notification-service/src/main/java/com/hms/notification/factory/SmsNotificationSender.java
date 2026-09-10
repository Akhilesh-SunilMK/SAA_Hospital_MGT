package com.hms.notification.factory;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.model.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Concrete product — SRS 4.1.1. No SMS gateway credentials are provisioned in this environment. */
@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("[SMS] to userId={} body='{}'", request.userId(), request.body());
        return NotificationResult.success("sms-" + UUID.randomUUID());
    }

    @Override
    public ChannelType getChannel() {
        return ChannelType.SMS;
    }
}
