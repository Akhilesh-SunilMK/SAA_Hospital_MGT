package com.hms.notification.factory;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResult;
import com.hms.notification.exception.UnsupportedChannelException;
import com.hms.notification.model.ChannelType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** SRS 10.1 TC-F-01, TC-F-02, TC-F-03. */
class NotificationSenderFactoryTest {

    @Test
    void tcF01_getSenderForEmail_returnsEmailNotificationSender() {
        NotificationSenderFactory factory = new NotificationSenderFactory(
                List.of(new EmailNotificationSender(), new SmsNotificationSender(), new PushNotificationSender()));

        NotificationSender sender = factory.getSender(ChannelType.EMAIL);

        assertThat(sender).isInstanceOf(EmailNotificationSender.class);
    }

    @Test
    void tcF02_getSenderForUnregisteredChannel_throwsUnsupportedChannelException() {
        // WHATSAPP is declared in ChannelType but has no registered sender.
        NotificationSenderFactory factory = new NotificationSenderFactory(
                List.of(new EmailNotificationSender(), new SmsNotificationSender(), new PushNotificationSender()));

        assertThatThrownBy(() -> factory.getSender(ChannelType.WHATSAPP))
                .isInstanceOf(UnsupportedChannelException.class)
                .hasMessageContaining("WHATSAPP");
    }

    @Test
    void tcF03_registeringNewSenderBean_isResolvedWithoutFactoryCodeChange() {
        // A brand new NotificationSender for the previously-unregistered WHATSAPP channel is
        // added purely by constructing the list differently — NotificationSenderFactory itself
        // is untouched, proving the Open/Closed property (FR-NT-05).
        NotificationSender whatsAppSender = new NotificationSender() {
            @Override
            public NotificationResult send(NotificationRequest request) {
                return NotificationResult.success("whatsapp-test");
            }

            @Override
            public ChannelType getChannel() {
                return ChannelType.WHATSAPP;
            }
        };

        NotificationSenderFactory factory = new NotificationSenderFactory(
                List.of(new EmailNotificationSender(), whatsAppSender));

        NotificationSender resolved = factory.getSender(ChannelType.WHATSAPP);

        assertThat(resolved).isSameAs(whatsAppSender);
    }
}
