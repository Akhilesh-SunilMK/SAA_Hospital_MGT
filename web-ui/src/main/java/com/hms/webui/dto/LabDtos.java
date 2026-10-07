package com.hms.webui.dto;

import java.math.BigDecimal;
import java.util.List;

public final class LabDtos {
    private LabDtos() {}

    public record LabOrderRequest(Long patientId, Long doctorId, List<Long> testIds, String priority,
                                   boolean fastingRequired) {}

    public record LabOrderItemView(Long id, Long testId, String status) {}

    public record LabOrderResponse(Long id, Long patientId, Long doctorId, String orderedAt, String priority,
                                    String status, boolean fastingRequired, List<LabOrderItemView> items) {}

    public record StatusUpdateRequest(String status) {}

    public record ResultUploadRequest(Long orderItemId, String value, String unit, String referenceRange,
                                       Boolean abnormalFlag) {}

    public record LabResultView(Long id, Long orderItemId, String value, String unit, String referenceRange,
                                 Boolean abnormalFlag) {}

    public record TestCatalogueResponse(Long id, String code, String name, String sampleType,
                                         BigDecimal price, Integer turnaroundHours) {}
}
