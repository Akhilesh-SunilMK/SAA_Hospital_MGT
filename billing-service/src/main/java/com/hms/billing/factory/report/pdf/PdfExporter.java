package com.hms.billing.factory.report.pdf;

import com.hms.billing.factory.report.ReportExporter;

public class PdfExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        return toBytes(formatted);
    }
}
