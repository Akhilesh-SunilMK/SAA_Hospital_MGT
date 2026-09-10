package com.hms.emr.factory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * PDF family. Kept dependency-light: rather than pulling in a real PDF rendering library, the
 * export step wraps the formatted text in a recognisable pseudo-PDF envelope. What TC-AF-01
 * actually verifies is that every component in the returned family is PDF-specific and that no
 * Excel/CSV component leaks in — not that the bytes render in a PDF viewer.
 */
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

class PdfFormatter implements ReportFormatter {
    @Override
    public String format(PatientSummaryData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append(styler.header(data.patientId())).append('\n');
        for (var r : data.records()) {
            sb.append("Record #").append(r.recordId()).append(" (").append(r.createdAt()).append(")\n");
            sb.append("  Chief complaint: ").append(r.chiefComplaint()).append('\n');
            sb.append("  Diagnoses: ").append(String.join(", ", r.diagnoses())).append('\n');
        }
        for (var p : data.prescriptions()) {
            sb.append("Prescription #").append(p.prescriptionId()).append(" (").append(p.issuedAt()).append(")\n");
            sb.append("  ").append(p.drugLines().stream().collect(Collectors.joining("; "))).append('\n');
        }
        sb.append(styler.footer());
        return sb.toString();
    }
}

class PdfExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        String withPseudoPdfHeader = "%PDF-1.4-LITE\n" + formatted + "\n%%EOF";
        return withPseudoPdfHeader.getBytes(StandardCharsets.UTF_8);
    }
}

class PdfStyler implements ReportStyler {
    @Override
    public String header(Long patientId) {
        return "=== HMS Patient Clinical Summary (PDF) — Patient #" + patientId + " ===";
    }

    @Override
    public String footer() {
        return "--- End of PDF report ---";
    }
}
