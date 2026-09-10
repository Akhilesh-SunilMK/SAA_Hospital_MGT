package com.hms.billing.factory.report.excel;

import com.hms.billing.factory.report.ReportExporter;

public class ExcelExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        return toBytes(formatted);
    }
}
