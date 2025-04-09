package de.iai.ilcd.model.common;

/**
 * @author clemens.duepmeier
 */
public enum DataSetState {
    UNRELEASED("new"), LOCKED("locked"), RELEASED("released");

    private String value;

    DataSetState(String value) {
        this.value = value;
    }

    public static DataSetState fromValue(String value) {
        for (DataSetState enumValue : DataSetState.values()) {
            if (enumValue.getValue().equals(value))
                return enumValue;
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
