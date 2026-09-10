package com.hms.lab.dto;

import com.hms.lab.entity.LabOrder;
import com.hms.lab.entity.LabOrderStatus;
import com.hms.lab.entity.LabPriority;

import java.time.LocalDateTime;
import java.util.List;

public record LabOrderResponse(
        Long id, Long patientId, Long doctorId, LocalDateTime orderedAt,
        LabPriority priority, LabOrderStatus status, boolean fastingRequired, List<ItemResponse> items
) {
    public record ItemResponse(Long id, Long testId, String status) {
    }

    public static LabOrderResponse from(LabOrder order) {
        return new LabOrderResponse(
                order.getId(), order.getPatientId(), order.getDoctorId(), order.getOrderedAt(),
                order.getPriority(), order.getStatus(), order.isFastingRequired(),
                order.getItems().stream().map(i -> new ItemResponse(i.getId(), i.getTestId(), i.getStatus().name())).toList()
        );
    }
}
