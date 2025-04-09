package de.iai.ilcd.webgui.controller.util.csv;

import de.iai.ilcd.model.datastock.ExportType;

public enum DecimalSeparator {

    DOT("dot", '.'), COMMA("comma", ',');

    public final char separator;
    private final String value;

    DecimalSeparator(String value, char separator) {
        this.value = value;
        this.separator = separator;
    }

    public static ExportType fromValue(String value) {
        for (ExportType enumValue : ExportType.values()) {
            if (enumValue.getValue().equals(value))
                return enumValue;
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
