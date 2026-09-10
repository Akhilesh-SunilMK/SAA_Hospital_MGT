package com.hms.billing.factory.report;

public interface ReportFormatter {
    String format(ReportData data, ReportStyler styler);
}
