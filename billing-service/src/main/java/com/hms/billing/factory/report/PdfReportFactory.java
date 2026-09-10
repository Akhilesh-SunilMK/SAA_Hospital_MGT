package com.hms.billing.factory.report;

import com.hms.billing.factory.report.pdf.PdfExporter;
import com.hms.billing.factory.report.pdf.PdfFormatter;
import com.hms.billing.factory.report.pdf.PdfStyler;

public class PdfReportFactory implements ReportComponentFactory {
    @Override
    public ReportFormatter createFormatter() {
        return new PdfFormatter();
    }

    @Override
    public ReportExporter createExporter() {
        return new PdfExporter();
    }

    @Override
    public ReportStyler createStyler() {
        return new PdfStyler();
    }
}
