package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.TypeOfReviewValue;

public enum ReviewType {
    PANEL, EXTERNAL, INTERNAL, UNKNOWN, NONE;

    public static ReviewType fromValue(TypeOfReviewValue value) {
        switch (value) {
            case ACCREDITED_THIRD_PARTY_REVIEW:
                return EXTERNAL;
            case INDEPENDENT_EXTERNAL_REVIEW:
                return EXTERNAL;
            case INDEPENDENT_INTERNAL_REVIEW:
                return INTERNAL;
            case DEPENDENT_INTERNAL_REVIEW:
                return INTERNAL;
            case INDEPENDENT_REVIEW_PANEL:
                return PANEL;
            case NOT_REVIEWED:
                return NONE;
            default:
                return UNKNOWN;
        }
    }
}
