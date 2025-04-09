package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

public enum ExportCacheFlag {

    MODIFIED(1),
    NOT_MODIFIED(0);

    private final int value;

    ExportCacheFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
