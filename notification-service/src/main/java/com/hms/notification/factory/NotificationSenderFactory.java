package com.hms.notification.factory;

import com.hms.notification.exception.UnsupportedChannelException;
import com.hms.notification.model.ChannelType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory Method pattern (SRS 4.1.1) — reproduced essentially verbatim from the SRS.
 * FR-NT-02/FR-NT-05: adding a new channel requires only a new NotificationSender
 * {@code @Component}; this class and every caller are unmodified (Open/Closed Principle).
 * WHATSAPP is declared in {@link ChannelType} but deliberately has no registered sender,
 * so {@link #getSender(ChannelType)} for it exercises the real failure path (TC-F-02).
 */
@Component
public class NotificationSenderFactory {

    private final Map<ChannelType, NotificationSender> registry;

    public NotificationSenderFactory(List<NotificationSender> senders) {
        this.registry = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, s -> s));
    }

    public NotificationSender getSender(ChannelType channel) {
        NotificationSender sender = registry.get(channel);
        if (sender == null) {
            throw new UnsupportedChannelException("No sender registered for channel: " + channel);
        }
        return sender;
    }
}
