package com.hms.notification.model;

/**
 * SRS Appendix B: EMAIL, SMS, PUSH. WHATSAPP is added deliberately with no registered
 * {@code NotificationSender} bean so TC-F-02 (unregistered channel -> UnsupportedChannelException)
 * has a real, realistic channel to exercise rather than a synthetic test-only value.
 */
public enum ChannelType {
    EMAIL,
    SMS,
    PUSH,
    WHATSAPP
}
