package com.hms.billing.factory.report.csv;

import com.hms.billing.factory.report.ReportStyler;

public class CsvStyler implements ReportStyler {
    @Override
    public String header(String title) {
        return "# " + title;
    }

    @Override
    public String footer() {
        return "# end";
    }
}
