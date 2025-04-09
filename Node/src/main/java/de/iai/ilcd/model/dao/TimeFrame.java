package de.iai.ilcd.model.dao;

/**
 * Indicates whether a given date is to be interpreted as an exact time or
 * period or rather a time period from or until the given date
 */
public enum TimeFrame {

    EXACT("exact"), FROM("from"), UNTIL("until");

    private final String value;

    TimeFrame(String v) {
        value = v;
    }

    public static TimeFrame fromValue(String v) {
        for (TimeFrame c : TimeFrame.values()) {
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
