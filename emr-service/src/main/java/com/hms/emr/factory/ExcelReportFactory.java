package com.hms.emr.factory;

import java.nio.charset.StandardCharsets;

/** Excel family, simplified to a tab-separated "Excel-lite" text export (no POI dependency). */
public class ExcelReportFactory implements ReportComponentFactory {

    @Override
    public ReportFormatter createFormatter() {
        return new ExcelFormatter();
    }

    @Override
    public ReportExporter createExporter() {
        return new ExcelExporter();
    }

    @Override
    public ReportStyler createStyler() {
        return new ExcelStyler();
    }
}

class ExcelFormatter implements ReportFormatter {
    @Override
    public String format(PatientSummaryData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append(styler.header(data.patientId())).append('\n');
        sb.append("RecordId\tDate\tChiefComplaint\tDiagnoses\n");
        for (var r : data.records()) {
            sb.append(r.recordId()).append('\t').append(r.createdAt()).append('\t')
                    .append(r.chiefComplaint()).append('\t')
                    .append(String.join("|", r.diagnoses())).append('\n');
        }
        sb.append("PrescriptionId\tDate\tDrugLines\n");
        for (var p : data.prescriptions()) {
            sb.append(p.prescriptionId()).append('\t').append(p.issuedAt()).append('\t')
                    .append(String.join("|", p.drugLines())).append('\n');
        }
        sb.append(styler.footer());
        return sb.toString();
    }
}

class ExcelExporter implements ReportExporter {
    @Override
    public byte[] export(String formatted) {
        return ("HMS-XLSX-LITE\n" + formatted).getBytes(StandardCharsets.UTF_8);
    }
}

class ExcelStyler implements ReportStyler {
    @Override
    public String header(Long patientId) {
        return "HMS Patient Clinical Summary (Excel) - Patient #" + patientId;
    }

    @Override
    public String footer() {
        return "-- end of sheet --";
    }
}
