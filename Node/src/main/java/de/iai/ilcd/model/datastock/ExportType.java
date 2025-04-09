package de.iai.ilcd.model.datastock;

public enum ExportType {

    ZIP(".zip"), CSV_EPD(".csv"), CSV_EPD_C(".csv");

    private String value;

    ExportType(String value) {
        this.value = value;
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
