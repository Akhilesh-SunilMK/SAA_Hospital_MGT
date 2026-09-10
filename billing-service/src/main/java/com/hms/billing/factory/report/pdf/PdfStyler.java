package com.hms.billing.factory.report.pdf;

import com.hms.billing.factory.report.ReportStyler;

public class PdfStyler implements ReportStyler {
    @Override
    public String header(String title) {
        return "%PDF-1.4\n% " + title + "\n----------------------------------------";
    }

    @Override
    public String footer() {
        return "----------------------------------------\n%%EOF";
    }
}
