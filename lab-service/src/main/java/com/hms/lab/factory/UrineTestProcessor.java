package com.hms.lab.factory;

import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabResult;
import org.springframework.stereotype.Component;

@Component
public class UrineTestProcessor implements TestProcessor {

    @Override
    public String getSampleType() {
        return "URINE";
    }

    @Override
    public LabResult processResult(LabOrderItem item, ResultUploadRequest request, Long reportedBy) {
        Boolean computed = NumericRangeSupport.computeAbnormal(request.value(), request.referenceRange());
        boolean abnormal = computed != null ? computed : Boolean.TRUE.equals(request.abnormalFlag());
        return new LabResult(item.getId(), request.value(), request.unit(), request.referenceRange(), abnormal, reportedBy);
    }
}
