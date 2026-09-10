package com.hms.billing.factory.report.pdf;

import com.hms.billing.factory.report.ReportData;
import com.hms.billing.factory.report.ReportFormatter;
import com.hms.billing.factory.report.ReportStyler;

public class PdfFormatter implements ReportFormatter {
    @Override
    public String format(ReportData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append(styler.header(data.title())).append('\n');
        data.rows().forEach(row -> sb.append(row).append('\n'));
        sb.append(styler.footer());
        return sb.toString();
    }
}
