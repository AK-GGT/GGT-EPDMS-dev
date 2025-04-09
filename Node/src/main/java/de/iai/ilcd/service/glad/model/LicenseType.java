package de.iai.ilcd.service.glad.model;

import de.fzk.iai.ilcd.service.model.enums.LicenseTypeValue;

public enum LicenseType {
    FREE, MIXED, CHARGED, UNKNOWN;

    public static LicenseType fromValue(LicenseTypeValue value) {
        switch (value) {
            case FREE_OF_CHARGE_FOR_ALL_USERS_AND_USES:
                return FREE;
            case FREE_OF_CHARGE_FOR_MEMBERS_ONLY:
                return MIXED;
            case FREE_OF_CHARGE_FOR_SOME_USER_TYPES_OR_USE_TYPES:
                return MIXED;
            case LICENSE_FEE:
                return CHARGED;
            case OTHER:
                return UNKNOWN;
            default:
                return UNKNOWN;
        }
    }
}
