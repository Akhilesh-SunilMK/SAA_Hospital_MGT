package com.hms.billing.factory.report;

import com.hms.billing.factory.report.csv.CsvExporter;
import com.hms.billing.factory.report.csv.CsvFormatter;
import com.hms.billing.factory.report.csv.CsvStyler;
import com.hms.billing.factory.report.excel.ExcelExporter;
import com.hms.billing.factory.report.excel.ExcelFormatter;
import com.hms.billing.factory.report.excel.ExcelStyler;
import com.hms.billing.factory.report.pdf.PdfExporter;
import com.hms.billing.factory.report.pdf.PdfFormatter;
import com.hms.billing.factory.report.pdf.PdfStyler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportComponentFactoryTest {

    private final ReportData data = new ReportData("Revenue", List.of(Map.of("invoiceNo", "INV-1", "total", 100)));

    @Test
    void tcAf01_pdfFormatOnlyInstantiatesPdfFamily() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.PDF);
        assertThat(factory.createFormatter()).isInstanceOf(PdfFormatter.class);
        assertThat(factory.createExporter()).isInstanceOf(PdfExporter.class);
        assertThat(factory.createStyler()).isInstanceOf(PdfStyler.class);
    }

    @Test
    void tcAf02_excelFormatNeverInstantiatesPdfComponent() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.EXCEL);
        assertThat(factory.createFormatter()).isInstanceOf(ExcelFormatter.class).isNotInstanceOf(PdfFormatter.class);
        assertThat(factory.createExporter()).isInstanceOf(ExcelExporter.class).isNotInstanceOf(PdfExporter.class);
        assertThat(factory.createStyler()).isInstanceOf(ExcelStyler.class).isNotInstanceOf(PdfStyler.class);
    }

    @Test
    void csvFamilyIsSelfConsistent() {
        ReportComponentFactory factory = FactoryProvider.of(ReportFormat.CSV);
        assertThat(factory.createFormatter()).isInstanceOf(CsvFormatter.class);
        assertThat(factory.createExporter()).isInstanceOf(CsvExporter.class);
        assertThat(factory.createStyler()).isInstanceOf(CsvStyler.class);
    }

    @Test
    void generatesNonEmptyOutputForEachFormat() {
        for (ReportFormat format : ReportFormat.values()) {
            ReportComponentFactory factory = FactoryProvider.of(format);
            String formatted = factory.createFormatter().format(data, factory.createStyler());
            byte[] output = factory.createExporter().export(formatted);
            assertThat(output).isNotEmpty();
        }
    }
}
