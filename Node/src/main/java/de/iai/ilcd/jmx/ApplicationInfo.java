package de.iai.ilcd.jmx;

import de.iai.ilcd.configuration.ConfigurationService;

public class ApplicationInfo implements ApplicationInfoMBean {

    private ConfigurationService configurationService;

    public ApplicationInfo(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public String getVersion() {
        return this.configurationService.getVersionTag();
    }

    @Override
    public String getSchemaVersion() {
        return this.configurationService.getSchemaVersion();
    }

    @Override
    public String getNodeId() {
        return this.configurationService.getNodeId();
    }
}
