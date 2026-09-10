package com.hms.billing.factory.report;

import java.util.List;
import java.util.Map;

public record ReportData(String title, List<Map<String, Object>> rows) {
}
