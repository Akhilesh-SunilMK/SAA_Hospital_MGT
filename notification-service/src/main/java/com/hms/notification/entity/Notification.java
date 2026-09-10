package com.hms.notification.entity;

import com.hms.notification.model.ChannelType;
import com.hms.notification.model.NotificationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SRS 8.1 hms_notification.notifications. No public setters (state changes go through the
 * intention-revealing methods below) even though this entity isn't one of the NFR-12
 * Builder-mandated ones (only 4 constructor params) — immutability is kept consistent anyway.
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType channel;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification(Long userId, ChannelType channel, String templateCode, String payload) {
        this.userId = userId;
        this.channel = channel;
        this.templateCode = templateCode;
        this.payload = payload;
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void markAttempt() {
        this.attempts++;
    }

    public void markRetrying() {
        this.status = NotificationStatus.RETRYING;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public void markSkipped() {
        this.status = NotificationStatus.SKIPPED;
    }
}
