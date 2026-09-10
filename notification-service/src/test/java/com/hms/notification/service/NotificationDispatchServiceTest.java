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
import com.hms.notification.model.NotificationStatus;
import com.hms.notification.repository.NotificationRepository;
import com.hms.notification.repository.NotificationTemplateRepository;
import com.hms.notification.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationSenderFactory senderFactory;
    @Mock
    private NotificationTemplateRepository templateRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserPreferenceRepository preferenceRepository;

    private NotificationDispatchService service;

    @BeforeEach
    void setUp() {
        // Zero backoff delay so retry tests run instantly.
        service = new NotificationDispatchService(senderFactory, templateRepository, notificationRepository,
                preferenceRepository, new TemplateRenderer(), new ObjectMapper(), 0L);
        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void retriesThenSucceeds_marksSentWithCorrectAttemptCount() {
        Notification notification = newNotification();
        AtomicInteger calls = new AtomicInteger(0);
        NotificationSender flaky = new NotificationSender() {
            @Override
            public NotificationResult send(NotificationRequest request) {
                int attempt = calls.incrementAndGet();
                if (attempt < 3) {
                    return NotificationResult.failure("transient failure #" + attempt);
                }
                return NotificationResult.success("ok");
            }

            @Override
            public ChannelType getChannel() {
                return ChannelType.EMAIL;
            }
        };

        Notification result = service.sendWithRetry(notification, flaky,
                new NotificationRequest(1L, ChannelType.EMAIL, "s", "b"));

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getAttempts()).isEqualTo(3);
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void exhaustsRetries_marksFailedAfterThreeAttempts() {
        Notification notification = newNotification();
        NotificationSender alwaysFails = new NotificationSender() {
            @Override
            public NotificationResult send(NotificationRequest request) {
                return NotificationResult.failure("permanent failure");
            }

            @Override
            public ChannelType getChannel() {
                return ChannelType.EMAIL;
            }
        };

        Notification result = service.sendWithRetry(notification, alwaysFails,
                new NotificationRequest(1L, ChannelType.EMAIL, "s", "b"));

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.getAttempts()).isEqualTo(3);
    }

    @Test
    void dispatch_channelDisabledByPreference_skipsWithoutCallingSender() {
        NotificationTemplate template = template();
        when(templateRepository.findByCodeAndChannelAndActiveTrue("APPOINTMENT_CONFIRMED", ChannelType.EMAIL))
                .thenReturn(Optional.of(template));
        when(preferenceRepository.findByUserIdAndChannel(1L, ChannelType.EMAIL))
                .thenReturn(Optional.of(new UserPreference(1L, ChannelType.EMAIL, false)));

        Notification result = service.dispatch(1L, ChannelType.EMAIL, "APPOINTMENT_CONFIRMED", Map.of());

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SKIPPED);
        verifyNoInteractions(senderFactory);
    }

    @Test
    void dispatch_missingTemplate_throwsResourceNotFoundException() {
        when(templateRepository.findByCodeAndChannelAndActiveTrue("UNKNOWN", ChannelType.EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.dispatch(1L, ChannelType.EMAIL, "UNKNOWN", Map.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void dispatch_preferenceEnabled_sendsAndMarksSent() {
        NotificationTemplate template = template();
        when(templateRepository.findByCodeAndChannelAndActiveTrue("APPOINTMENT_CONFIRMED", ChannelType.EMAIL))
                .thenReturn(Optional.of(template));
        when(preferenceRepository.findByUserIdAndChannel(1L, ChannelType.EMAIL)).thenReturn(Optional.empty());
        NotificationSender sender = new NotificationSender() {
            @Override
            public NotificationResult send(NotificationRequest request) {
                return NotificationResult.success("ok");
            }

            @Override
            public ChannelType getChannel() {
                return ChannelType.EMAIL;
            }
        };
        when(senderFactory.getSender(ChannelType.EMAIL)).thenReturn(sender);

        Notification result = service.dispatch(1L, ChannelType.EMAIL, "APPOINTMENT_CONFIRMED", Map.of("tokenNumber", "T1"));

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    private Notification newNotification() {
        return new Notification(1L, ChannelType.EMAIL, "APPOINTMENT_CONFIRMED", "{}");
    }

    private NotificationTemplate template() {
        NotificationTemplate template = newInstance(NotificationTemplate.class);
        setField(template, "code", "APPOINTMENT_CONFIRMED");
        setField(template, "channel", ChannelType.EMAIL);
        setField(template, "subject", "Subject");
        setField(template, "body", "Token {{tokenNumber}}");
        setField(template, "active", true);
        return template;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T newInstance(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
