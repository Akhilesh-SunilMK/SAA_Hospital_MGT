package com.hms.billing.service;

import com.hms.billing.factory.report.FactoryProvider;
import com.hms.billing.factory.report.ReportComponentFactory;
import com.hms.billing.factory.report.ReportData;
import com.hms.billing.factory.report.ReportFormat;
import org.springframework.stereotype.Service;

/**
 * No conditional branching on format here (FR-BL-09) — {@link FactoryProvider#of(ReportFormat)}
 * is the only place the format is switched on; this method is format-agnostic.
 */
@Service
public class ReportService {

    public byte[] generate(ReportFormat format, ReportData data) {
        ReportComponentFactory factory = FactoryProvider.of(format);
        String formatted = factory.createFormatter().format(data, factory.createStyler());
        return factory.createExporter().export(formatted);
    }
}
