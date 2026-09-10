package com.hms.emr.factory;

import java.nio.charset.StandardCharsets;

public interface ReportExporter {
    byte[] export(String formatted);

    default byte[] asBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
