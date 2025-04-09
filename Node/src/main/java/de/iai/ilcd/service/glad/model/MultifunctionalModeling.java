package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.LCIMethodApproachesValue;

public enum MultifunctionalModeling {
    PHYSICAL, ECONOMIC, CAUSAL, SYSTEM_EXPANSION, NOT_APPLICABLE, NONE, UNKNOWN;

    public static MultifunctionalModeling fromValue(LCIMethodApproachesValue value) {
        if (value == null) {
            return NONE;
        }
        switch (value) {
            case ALLOCATION_GROSS_CALORIFIC_VALUE:
            case ALLOCATION_NET_CALORIFIC_VALUE:
            case ALLOCATION_EXERGETIC_CONTENT:
            case ALLOCATION_ELEMENT_CONTENT:
            case ALLOCATION_MASS:
            case ALLOCATION_VOLUME:
            case ALLOCATION_RECYCLED_CONTENT:
                return PHYSICAL;
            case ALLOCATION_MARKET_VALUE:
                return ECONOMIC;
            case ALLOCATION_PHYSICAL_CAUSALITY:
            case ALLOCATION_ABILITY_TO_BEAR:
            case ALLOCATION_MARGINAL_CAUSALITY:
            case ALLOCATION_100_TO_MAIN_FUNCTION:
            case ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT:
            case ALLOCATION_EQUAL_DISTRIBUTION:
            case CONSEQUENTIAL_EFFECTS_OTHER:
                return CAUSAL;
            case SUBSTITUTION_BAT:
            case SUBSTITUTION_AVERAGE_MARKET_PRICE_CORRECTION:
            case SUBSTITUTION_AVERAGE_TECHNICAL_PROPERTIES_CORRECTION:
            case SUBSTITUTION_RECYCLING_POTENTIAL:
            case SUBSTITUTION_AVERAGE_NO_CORRECTION:
            case SUBSTITUTION_SPECIFIC:
                return SYSTEM_EXPANSION;
            case NOT_APPLICABLE:
                return NOT_APPLICABLE;
            default:
                return UNKNOWN;
        }
    }
}
