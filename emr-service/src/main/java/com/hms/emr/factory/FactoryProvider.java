package com.hms.emr.factory;

/** Selects the report family at runtime — no conditional branching in ReportService (FR-EM-09). */
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
