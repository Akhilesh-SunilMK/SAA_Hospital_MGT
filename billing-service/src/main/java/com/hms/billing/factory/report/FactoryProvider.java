package com.hms.billing.factory.report;

public final class FactoryProvider {

    private FactoryProvider() {
    }

    public static ReportComponentFactory of(ReportFormat format) {
        return switch (format) {
            case PDF -> new PdfReportFactory();
            case EXCEL -> new ExcelReportFactory();
            case CSV -> new CsvReportFactory();
        };
    }
}
