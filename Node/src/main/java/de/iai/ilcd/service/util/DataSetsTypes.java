package de.iai.ilcd.service.util;

public enum DataSetsTypes {
    PROCESSES("common.processes"), ELEMENTARY_FLOWS("common.elementaryFlows"), LCIA_METHODS("common.lciaMethods"),
    PRODUCT_FLOWS("common.productFlows"), FLOW_PROPERTIES("common.flowProperties"), UNIT_GROUPS("common.unitGroups"),
    SOURCES("common.sources"), CONTACTS("common.contacts"), LIFECYCLEMODELS("common.lifecyclemodels");

    private String displayName;

    private DataSetsTypes(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}