package com.hms.emr.factory;

public interface ReportFormatter {
    String format(PatientSummaryData data, ReportStyler styler);
}
