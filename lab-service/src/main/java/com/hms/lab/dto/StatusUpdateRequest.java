package com.hms.lab.dto;

import com.hms.lab.entity.LabOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull LabOrderStatus status) {
}
