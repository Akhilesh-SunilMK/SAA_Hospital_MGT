package com.hms.lab.dto;

import jakarta.validation.constraints.NotNull;

public record ResultUploadRequest(
        @NotNull Long orderItemId,
        @NotNull String value,
        String unit,
        String referenceRange,
        Boolean abnormalFlag
) {
}
