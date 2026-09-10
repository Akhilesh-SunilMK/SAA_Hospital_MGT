package com.hms.notification.factory;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.model.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete product — SRS 4.1.1. No SMTP relay is available in this environment (SRS A2: the
 * hospital provisions SMTP credentials at deployment), so delivery is simulated by logging the
 * rendered message; the dispatch/retry/status-tracking pipeline around this is fully real.
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Override
    public NotificationResult send(NotificationRequest request) {
        log.info("[EMAIL] to userId={} subject='{}' body='{}'", request.userId(), request.subject(), request.body());
        return NotificationResult.success("email-" + UUID.randomUUID());
    }

    @Override
    public ChannelType getChannel() {
        return ChannelType.EMAIL;
    }
}
