package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

import javax.annotation.Nonnull;

public enum DecimalSeparator {

    COMMA(",", "comma"),
    DOT(".", "dot");

    private final String value;
    private final String parameterValue;

    DecimalSeparator(@Nonnull final String value,
                     @Nonnull final String parameterValue) {
        this.value = value;
        this.parameterValue = parameterValue;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Nonnull
    public String getParameterValue() {
        return parameterValue;
    }
}
