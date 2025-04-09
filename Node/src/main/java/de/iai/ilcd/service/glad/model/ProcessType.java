package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;

public enum ProcessType {
    UNIT, PARTIALLY_AGGREGATED, FULLY_AGGREGATED, BRIDGE, UNKNOWN;

    public static ProcessType fromValue(TypeOfProcessValue type) {
        switch (type) {
            case AVOIDED_PRODUCT_SYSTEM:
                return UNKNOWN;
            case EPD:
                return UNKNOWN;
            case LCI_RESULT:
                return FULLY_AGGREGATED;
            case PARTLY_TERMINATED_SYSTEM:
                return PARTIALLY_AGGREGATED;
            case UNIT_PROCESS_BLACK_BOX:
                return UNIT;
            case UNIT_PROCESS_SINGLE_OPERATION:
                return UNIT;
            default:
                return UNKNOWN;
        }
    }

}
