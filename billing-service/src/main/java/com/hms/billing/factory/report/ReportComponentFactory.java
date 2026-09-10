package com.hms.billing.factory.report;

/**
 * Abstract Factory (SRS 4.2, FR-BL-09). A report family consists of a formatter, an exporter,
 * and a header/footer styler; the PDF family, Excel family, and CSV family must never be mixed.
 */
public interface ReportComponentFactory {
    ReportFormatter createFormatter();

    ReportExporter createExporter();

    ReportStyler createStyler();
}
