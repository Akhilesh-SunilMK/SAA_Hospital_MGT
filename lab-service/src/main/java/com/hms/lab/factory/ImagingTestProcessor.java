package com.hms.lab.factory;

import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabResult;
import org.springframework.stereotype.Component;

/** Imaging reports are free text — no numeric range check, abnormalFlag is whatever the radiologist reports. */
@Component
public class ImagingTestProcessor implements TestProcessor {

    @Override
    public String getSampleType() {
        return "IMAGING";
    }

    @Override
    public LabResult processResult(LabOrderItem item, ResultUploadRequest request, Long reportedBy) {
        boolean abnormal = Boolean.TRUE.equals(request.abnormalFlag());
        return new LabResult(item.getId(), request.value(), request.unit(), request.referenceRange(), abnormal, reportedBy);
    }
}
