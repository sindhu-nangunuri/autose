package com.dataquality.models;

public enum DataQualityMetric {
    COMPLETENESS("Completeness", "Measures the percentage of non-null values"),
    UNIQUENESS("Uniqueness", "Measures the percentage of unique values"),
    CONSISTENCY("Consistency", "Measures data consistency across related fields"),
    VALIDITY("Validity", "Measures adherence to defined formats and rules"),
    ACCURACY("Accuracy", "Measures correctness of data values"),
    INTEGRITY("Integrity", "Measures referential and domain integrity"),
    TIMELINESS("Timeliness", "Measures data freshness and currency"),
    CONFORMITY("Conformity", "Measures adherence to data standards"),
    RANGE("Range", "Measures values within expected ranges"),
    BLANKS("Blanks", "Measures presence of blank/empty values"),
    OUTLIERS("Outliers", "Measures presence of statistical outliers");

    private final String displayName;
    private final String description;

    DataQualityMetric(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}