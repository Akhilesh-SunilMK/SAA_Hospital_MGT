package com.hms.billing.factory.report.excel;

import com.hms.billing.factory.report.ReportData;
import com.hms.billing.factory.report.ReportFormatter;
import com.hms.billing.factory.report.ReportStyler;

public class ExcelFormatter implements ReportFormatter {
    @Override
    public String format(ReportData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append(styler.header(data.title())).append('\n');
        data.rows().forEach(row -> {
            String tsvRow = row.values().stream().map(String::valueOf).reduce((a, b) -> a + "\t" + b).orElse("");
            sb.append(tsvRow).append('\n');
        });
        sb.append(styler.footer());
        return sb.toString();
    }
}
