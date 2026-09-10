package com.hms.billing.factory.report.excel;

import com.hms.billing.factory.report.ReportStyler;

public class ExcelStyler implements ReportStyler {
    @Override
    public String header(String title) {
        return "[Sheet1] " + title;
    }

    @Override
    public String footer() {
        return "[EndOfSheet]";
    }
}
