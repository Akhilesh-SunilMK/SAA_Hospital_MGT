package com.hms.emr.factory;

/**
 * Abstract Factory pattern (SRS 4.2, FR-EM-09 / FR-BL-09): a report family consists of a
 * formatter, an exporter and a styler. The PDF, Excel and CSV families must never be mixed —
 * {@link FactoryProvider} selects the right family at runtime with no conditional branching in
 * the calling {@code ReportService}.
 */
public interface ReportComponentFactory {
    ReportFormatter createFormatter();

    ReportExporter createExporter();

    ReportStyler createStyler();
}
