package com.hms.billing.factory.report;

import com.hms.billing.factory.report.excel.ExcelExporter;
import com.hms.billing.factory.report.excel.ExcelFormatter;
import com.hms.billing.factory.report.excel.ExcelStyler;

public class ExcelReportFactory implements ReportComponentFactory {
    @Override
    public ReportFormatter createFormatter() {
        return new ExcelFormatter();
    }

    @Override
    public ReportExporter createExporter() {
        return new ExcelExporter();
    }

    @Override
    public ReportStyler createStyler() {
        return new ExcelStyler();
    }
}
