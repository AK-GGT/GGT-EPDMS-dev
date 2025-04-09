package de.iai.ilcd.webgui.util;

public enum ValidationContext {

    IMPORT("import"), STOCK("stock"), DATASETS("datasets");

    private final String value;

    ValidationContext(String v) {
        value = v;
    }

    public static ValidationContext fromValue(String v) {
        for (ValidationContext c : ValidationContext.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

    public String getValue() {
        return value;
    }

}
