package com.hms.billing.factory.report.csv;

import com.hms.billing.factory.report.ReportData;
import com.hms.billing.factory.report.ReportFormatter;
import com.hms.billing.factory.report.ReportStyler;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CsvFormatter implements ReportFormatter {
    @Override
    public String format(ReportData data, ReportStyler styler) {
        StringBuilder sb = new StringBuilder();
        sb.append(styler.header(data.title())).append('\n');
        if (!data.rows().isEmpty()) {
            Set<String> columns = new LinkedHashSet<>();
            data.rows().forEach(row -> columns.addAll(row.keySet()));
            sb.append(String.join(",", columns)).append('\n');
            for (Map<String, Object> row : data.rows()) {
                String line = columns.stream()
                        .map(c -> String.valueOf(row.getOrDefault(c, "")))
                        .reduce((a, b) -> a + "," + b).orElse("");
                sb.append(line).append('\n');
            }
        }
        sb.append(styler.footer());
        return sb.toString();
    }
}
