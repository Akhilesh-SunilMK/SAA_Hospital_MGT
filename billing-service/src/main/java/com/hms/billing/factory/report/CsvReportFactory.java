package com.hms.billing.factory.report;

import com.hms.billing.factory.report.csv.CsvExporter;
import com.hms.billing.factory.report.csv.CsvFormatter;
import com.hms.billing.factory.report.csv.CsvStyler;

public class CsvReportFactory implements ReportComponentFactory {
    @Override
    public ReportFormatter createFormatter() {
        return new CsvFormatter();
    }

    @Override
    public ReportExporter createExporter() {
        return new CsvExporter();
    }

    @Override
    public ReportStyler createStyler() {
        return new CsvStyler();
    }
}
