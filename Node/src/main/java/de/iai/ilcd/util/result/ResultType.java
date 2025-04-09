package de.iai.ilcd.util.result;

public enum ResultType {

    REDUNDANT("redundant", 0),
    SUCCESS("success", 1),
    PARTIALLY_SUCCESSFUL("partially successful", 5),
    QUIET_ERROR("failed", 10),
    ERROR("failed", 20);

    String value;

    int severity;

    ResultType(String value, int severity) {
        this.value = value;
        this.severity = severity;
    }

    public String getValue() {
        return value;
    }

    public int getSeverity() {
        return severity;
    }
}
