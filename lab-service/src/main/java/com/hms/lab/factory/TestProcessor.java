package com.hms.lab.factory;

import com.hms.lab.dto.ResultUploadRequest;
import com.hms.lab.entity.LabOrderItem;
import com.hms.lab.entity.LabResult;

/**
 * Factory Method pattern (traceability matrix: lab-service -> TestProcessor). Different sample
 * types need different result validation before persistence — a numeric range check for a
 * BLOOD/URINE panel vs. free-text acceptance for an imaging report. Adding a new sample type
 * requires only a new {@code @Component} implementing this interface; {@link TestProcessorFactory}
 * resolves it by sample type with no change to calling code.
 */
public interface TestProcessor {

    String getSampleType();

    LabResult processResult(LabOrderItem item, ResultUploadRequest request, Long reportedBy);
}
