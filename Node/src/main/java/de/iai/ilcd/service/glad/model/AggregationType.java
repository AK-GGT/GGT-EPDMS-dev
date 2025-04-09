package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;

public enum AggregationType {
    HORIZONTAL, NOT_APPLICABLE, VERTICAL, COMBINED, UNKNOWN;

    public static AggregationType fromValue(TypeOfProcessValue value) {
        switch (value) {
            case UNIT_PROCESS_BLACK_BOX:
                return HORIZONTAL;
            case UNIT_PROCESS_SINGLE_OPERATION:
                return NOT_APPLICABLE;
            case LCI_RESULT:
                return VERTICAL;
            default:
                return UNKNOWN;
        }
    }
}
