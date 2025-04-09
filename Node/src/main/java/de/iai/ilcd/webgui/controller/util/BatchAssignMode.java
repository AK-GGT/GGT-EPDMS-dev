package de.iai.ilcd.webgui.controller.util;

import org.apache.commons.lang.StringUtils;

public enum BatchAssignMode {
    EXCEL_IMPORT("byExcelImport"),
    DATA_SET_TYPE("byType");

    private String stringValue;

    BatchAssignMode(String stringValue) {
        this.stringValue = stringValue;
    }

    public static BatchAssignMode fromValue(String value) {
        if (value.isEmpty())
            return null;
        for (BatchAssignMode enumValue : BatchAssignMode.values()) {
            if (StringUtils.equalsIgnoreCase(enumValue.getValue(), value)) {
                return enumValue;
            }
        }
        return null;
    }

    public String getValue() {
        return this.stringValue;
    }
}
