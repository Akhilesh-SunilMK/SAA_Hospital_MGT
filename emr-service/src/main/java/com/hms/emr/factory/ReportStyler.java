package com.hms.emr.factory;

public interface ReportStyler {
    String header(Long patientId);

    String footer();
}
