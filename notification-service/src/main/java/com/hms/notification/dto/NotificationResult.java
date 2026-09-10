package com.hms.notification.dto;

public record NotificationResult(boolean success, String providerMessageId, String errorMessage) {

    public static NotificationResult success(String providerMessageId) {
        return new NotificationResult(true, providerMessageId, null);
    }

    public static NotificationResult failure(String errorMessage) {
        return new NotificationResult(false, null, errorMessage);
    }
}
