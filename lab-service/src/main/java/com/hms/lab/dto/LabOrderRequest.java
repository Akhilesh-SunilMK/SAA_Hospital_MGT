package com.hms.lab.dto;

import com.hms.lab.entity.LabPriority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LabOrderRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotEmpty List<Long> testIds,
        LabPriority priority,
        boolean fastingRequired
) {
}
