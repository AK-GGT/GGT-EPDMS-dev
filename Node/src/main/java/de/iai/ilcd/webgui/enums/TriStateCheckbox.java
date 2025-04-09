package de.iai.ilcd.webgui.enums;

public enum TriStateCheckbox {

    EMPTY(0, null),
    CHECKED(1, true),
    CROSSED(2, false);

    private int intValue;

    private Boolean booleanValue;

    TriStateCheckbox(int i, Boolean b) {
        intValue = i;
        booleanValue = b;
    }

    public static TriStateCheckbox from(int i) {
        for (TriStateCheckbox t : TriStateCheckbox.values())
            if (t.intValue() == i)
                return t;

        throw new IllegalArgumentException("Unknown state: " + i);
    }

    public static TriStateCheckbox from(Boolean b) {
        for (TriStateCheckbox t : TriStateCheckbox.values())
            if (t.getBooleanValue().equals(b))
                return t;

        throw new IllegalArgumentException("Unknown state: " + b); // Will, of course, never occur.
    }

    public int intValue() {
        return intValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public String toString() {
        return String.valueOf(intValue);
    }
}
