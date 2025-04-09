package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.LCIMethodPrincipleValue;

public enum ModelingType {
    ATTRIBUTIONAL, CONSEQUENTIAL, UNKNOWN;

    public static ModelingType fromValue(LCIMethodPrincipleValue value) {
        switch (value) {
            case ATTRIBUTIONAL:
                return ATTRIBUTIONAL;
            case CONSEQUENTIAL:
                return CONSEQUENTIAL;
            case CONSEQUENTIAL_WITH_ATTRIBUTIONAL_COMPONENTS:
                return CONSEQUENTIAL;
            case NOT_APPLICABLE:
                return UNKNOWN;
            case OTHER:
                return UNKNOWN;
            default:
                return UNKNOWN;
        }
    }
}
