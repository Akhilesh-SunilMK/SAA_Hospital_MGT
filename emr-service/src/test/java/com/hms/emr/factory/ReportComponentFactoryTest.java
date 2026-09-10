package com.hms.emr.factory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors the intent of SRS 10.1 TC-AF-01/02: family consistency, no cross-family leakage. */
class ReportComponentFactoryTest {

    private final PatientSummaryData data = new PatientSummaryData(
            42L,
            List.of(new PatientSummaryData.RecordLine(1L, "2026-01-01", "Fever", List.of("R50.9 - Fever"))),
            List.of(new PatientSummaryData.PrescriptionLine(1L, "2026-01-01", List.of("Paracetamol 500mg TDS")))
    );

    @Test
    void pdfFamilyProducesOnlyPdfComponents() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.PDF);
        assertThat(factory).isInstanceOf(PdfReportFactory.class);
        String formatted = factory.createFormatter().format(data, factory.createStyler());
        byte[] bytes = factory.createExporter().export(formatted);
        assertThat(new String(bytes)).startsWith("%PDF-1.4-LITE");
        assertThat(new String(bytes)).doesNotContain("HMS-XLSX-LITE");
    }

    @Test
    void excelFormatProducesNoPdfComponent() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.EXCEL);
        assertThat(factory).isInstanceOf(ExcelReportFactory.class);
        String formatted = factory.createFormatter().format(data, factory.createStyler());
        byte[] bytes = factory.createExporter().export(formatted);
        assertThat(new String(bytes)).doesNotContain("%PDF");
        assertThat(new String(bytes)).startsWith("HMS-XLSX-LITE");
    }

    @Test
    void csvFormatIsPlainCommaSeparated() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.CSV);
        String formatted = factory.createFormatter().format(data, factory.createStyler());
        assertThat(formatted).contains("recordId,date,chiefComplaint,diagnoses");
    }
}
