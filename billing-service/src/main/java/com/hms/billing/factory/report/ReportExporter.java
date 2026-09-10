package com.hms.billing.factory.report;

import java.nio.charset.StandardCharsets;

public interface ReportExporter {
    byte[] export(String formatted);

    default byte[] toBytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
