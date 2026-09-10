package com.hms.notification.model;

/** SRS 8.1 hms_notification.notifications.status: PENDING, SENT, FAILED, RETRYING (FR-NT-04).
 *  SKIPPED is added for the FR-NT-07 opt-out path — the notification was never attempted
 *  because the recipient disabled that channel. */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    RETRYING,
    SKIPPED
}
