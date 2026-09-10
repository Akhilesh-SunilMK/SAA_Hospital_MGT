package com.hms.emr.factory;

import java.nio.charset.StandardCharsets;

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

class CsvFormatter implements ReportFormatter {
    @Override
    public String format(PatientSummaryData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(styler.header(data.patientId())).append('\n');
        sb.append("recordId,date,chiefComplaint,diagnoses\n");
        for (var r : data.records()) {
            sb.append(r.recordId()).append(',').append(r.createdAt()).append(',')
                    .append('"').append(r.chiefComplaint()).append('"').append(',')
                    .append('"').append(String.join("; ", r.diagnoses())).append('"').append('\n');
        }
        sb.append("prescriptionId,date,drugLines\n");
        for (var p : data.prescriptions()) {
            sb.append(p.prescriptionId()).append(',').append(p.issuedAt()).append(',')
                    .append('"').append(String.join("; ", p.drugLines())).append('"').append('\n');
        }
        sb.append("# ").append(styler.footer());
        return sb.toString();
    }
}

class CsvExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        return formatted.getBytes(StandardCharsets.UTF_8);
    }
}

class CsvStyler implements ReportStyler {
    @Override
    public String header(Long patientId) {
        return "HMS Patient Clinical Summary (CSV) - Patient #" + patientId;
    }

    @Override
    public String footer() {
        return "end of csv";
    }
}
