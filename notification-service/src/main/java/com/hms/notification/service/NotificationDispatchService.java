package com.hms.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.entity.Notification;
import com.hms.notification.entity.NotificationTemplate;
import com.hms.notification.entity.UserPreference;
import com.hms.notification.factory.NotificationSender;
import com.hms.notification.factory.NotificationSenderFactory;
import com.hms.notification.model.ChannelType;
import com.hms.notification.repository.NotificationRepository;
import com.hms.notification.repository.NotificationTemplateRepository;
import com.hms.notification.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Core dispatch pipeline: preference check (FR-NT-07) -> template render (FR-NT-03) ->
 * NotificationSenderFactory (FR-NT-02) -> retry with exponential backoff (FR-NT-06) ->
 * persisted delivery status (FR-NT-04).
 */
@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final NotificationSenderFactory senderFactory;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;
    private final long retryBaseDelayMs;

    public NotificationDispatchService(NotificationSenderFactory senderFactory,
                                        NotificationTemplateRepository templateRepository,
                                        NotificationRepository notificationRepository,
                                        UserPreferenceRepository preferenceRepository,
                                        TemplateRenderer templateRenderer,
                                        ObjectMapper objectMapper,
                                        @Value("${notification.retry.base-delay-ms:500}") long retryBaseDelayMs) {
        this.senderFactory = senderFactory;
        this.templateRepository = templateRepository;
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;
        this.retryBaseDelayMs = retryBaseDelayMs;
    }

    @Transactional
    public Notification dispatch(Long userId, ChannelType channel, String templateCode, Map<String, String> variables) {
        NotificationTemplate template = templateRepository.findByCodeAndChannelAndActiveTrue(templateCode, channel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active template '" + templateCode + "' for channel " + channel));

        Notification notification = new Notification(userId, channel, templateCode, toJson(variables));
        notification = notificationRepository.save(notification);

        boolean channelEnabled = preferenceRepository.findByUserIdAndChannel(userId, channel)
                .map(UserPreference::isEnabled)
                .orElse(true);

        if (!channelEnabled) {
            notification.markSkipped();
            log.info("Notification {} skipped: userId={} has disabled channel {}", notification.getId(), userId, channel);
            return notificationRepository.save(notification);
        }

        String renderedBody = templateRenderer.render(template.getBody(), variables);
        NotificationSender sender = senderFactory.getSender(channel);
        NotificationRequest request = new NotificationRequest(userId, channel, template.getSubject(), renderedBody);

        return sendWithRetry(notification, sender, request);
    }

    /**
     * Package-visible so unit tests can exercise the retry/backoff logic directly against a
     * hand-built {@link Notification} and a mock {@link NotificationSender}, without needing a
     * database or a real template.
     */
    Notification sendWithRetry(Notification notification, NotificationSender sender, NotificationRequest request) {
        String lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            notification.markAttempt();
            if (attempt > 1) {
                notification.markRetrying();
            }
            try {
                NotificationResult result = sender.send(request);
                if (result.success()) {
                    notification.markSent();
                    return notificationRepository.save(notification);
                }
                lastError = result.errorMessage();
            } catch (RuntimeException ex) {
                lastError = ex.getMessage();
            }
            if (attempt < MAX_ATTEMPTS) {
                backoffSleep(attempt);
            }
        }
        notification.markFailed();
        log.warn("Notification {} failed after {} attempts: {}", notification.getId(), MAX_ATTEMPTS, lastError);
        return notificationRepository.save(notification);
    }

    private void backoffSleep(int attempt) {
        if (retryBaseDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(retryBaseDelayMs * (1L << (attempt - 1)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String toJson(Map<String, String> variables) {
        try {
            return objectMapper.writeValueAsString(variables != null ? variables : Map.of());
        } catch (Exception e) {
            return "{}";
        }
    }
}
