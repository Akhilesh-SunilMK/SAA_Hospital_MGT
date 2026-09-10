package com.hms.billing.factory.report.csv;

import com.hms.billing.factory.report.ReportExporter;

public class CsvExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        return toBytes(formatted);
    }
}
