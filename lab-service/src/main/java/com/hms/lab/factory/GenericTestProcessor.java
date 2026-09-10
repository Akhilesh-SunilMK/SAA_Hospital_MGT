package com.hms.lab.factory;

import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabResult;
import org.springframework.stereotype.Component;

/** Fallback for any sample type not covered by a dedicated processor. */
@Component
public class GenericTestProcessor implements TestProcessor {

    public static final String SAMPLE_TYPE = "GENERIC";

    @Override
    public String getSampleType() {
        return SAMPLE_TYPE;
    }

    @Override
    public LabResult processResult(LabOrderItem item, ResultUploadRequest request, Long reportedBy) {
        boolean abnormal = Boolean.TRUE.equals(request.abnormalFlag());
        return new LabResult(item.getId(), request.value(), request.unit(), request.referenceRange(), abnormal, reportedBy);
    }
}
