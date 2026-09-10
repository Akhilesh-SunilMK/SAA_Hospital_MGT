package com.hms.lab.factory;

/** Shared helper for processors that attempt a "min-max" reference-range check. */
final class NumericRangeSupport {

    private NumericRangeSupport() {
    }

    /**
     * Best-effort: tries to parse {@code value} and a "min-max" {@code referenceRange}. Returns
     * {@code null} (meaning "could not determine") rather than throwing, since real-world
     * reference ranges include units and non-numeric qualifiers this simplified parser won't
     * handle — callers fall back to the explicitly-provided abnormalFlag in that case.
     */
    static Boolean computeAbnormal(String value, String referenceRange) {
        if (referenceRange == null || !referenceRange.contains("-")) {
            return null;
        }
        try {
            double numericValue = Double.parseDouble(value.trim());
            String[] parts = referenceRange.split("-", 2);
            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());
            return numericValue < min || numericValue > max;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
