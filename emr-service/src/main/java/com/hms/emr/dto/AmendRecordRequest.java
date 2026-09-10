package com.hms.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record AmendRecordRequest(@NotBlank String newValue, @NotBlank String reason) {
}
