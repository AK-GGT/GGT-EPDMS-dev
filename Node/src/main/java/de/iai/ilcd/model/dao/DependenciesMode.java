package de.iai.ilcd.model.dao;

public enum DependenciesMode {

    NONE(0), REFERENCE_FLOWS(1), ALL_FROM_DATASTOCK(2), ALL(3);

    private int value;

    DependenciesMode(int value) {
        this.value = value;
    }

    public static DependenciesMode fromValue(int value) {
        for (DependenciesMode enumValue : DependenciesMode.values()) {
            if (enumValue.getValue() == value) {
                return enumValue;
            }
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        switch (this.value) {
            case 0:
                return "NONE";
            case 1:
                return "REFERENCE_FLOWS";
            case 2:
                return "ALL_FROM_DATASTOCK";
            case 3:
                return "ALL";
            default:
                return Integer.toString(this.value);
        }
    }

}
