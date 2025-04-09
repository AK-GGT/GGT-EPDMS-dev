package de.iai.ilcd.model.security;

/**
 * @author clemens.duepmeier
 */
public enum Gender {
    f("female"), m("male"), o("other");

    private String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender fromValue(String value) {
        for (Gender enumValue : Gender.values()) {
            if (enumValue.getValue().equalsIgnoreCase(value))
                return enumValue;
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
