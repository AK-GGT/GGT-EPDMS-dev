package de.iai.ilcd.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.DependenciesMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class DependenciesOptions {

    private static Logger logger = LogManager.getLogger(DependenciesOptions.class);

    /**
     * flag that indicates whether to include dependencies (linked datasets from
     * the same root data stock). This is being reset to false after each
     * operation.
     */
    private DependenciesMode dependenciesOption = ConfigurationService.INSTANCE.getDisplayConfig().getDependenciesOptionDefault();
    private DependenciesMode[] configuredValues = null;

    public DependenciesOptions() {
    }

    public void resetDependenciesOption() {
        if (logger.isTraceEnabled())
            logger.trace("resetting dependencies option to default");
        this.setDependenciesOption(ConfigurationService.INSTANCE.getDisplayConfig().getDependenciesOptionDefault());
    }

    public DependenciesMode[] getDependenciesOptionsValues() {
        if (this.configuredValues == null) {
            EnumSet<DependenciesMode> modes = EnumSet.allOf(DependenciesMode.class);
            if (!ConfigurationService.INSTANCE.getDisplayConfig().isShowDependenciesOption0())
                modes.remove(DependenciesMode.NONE);
            if (!ConfigurationService.INSTANCE.getDisplayConfig().isShowDependenciesOption1())
                modes.remove(DependenciesMode.REFERENCE_FLOWS);
            if (!ConfigurationService.INSTANCE.getDisplayConfig().isShowDependenciesOption2())
                modes.remove(DependenciesMode.ALL_FROM_DATASTOCK);
            if (!ConfigurationService.INSTANCE.getDisplayConfig().isShowDependenciesOption3())
                modes.remove(DependenciesMode.ALL);
            this.configuredValues = new DependenciesMode[modes.size()];
            int index = 0;
            for (DependenciesMode m : modes)
                this.configuredValues[index++] = m;
        }

        return this.configuredValues;
    }

    public DependenciesMode getDependenciesOption() {
        return dependenciesOption;
    }

    public void setDependenciesOption(int option) {
        if (logger.isTraceEnabled())
            logger.trace("setting dependencies option to " + option);
        this.setDependenciesOption(DependenciesMode.fromValue(option));
    }

    public void setDependenciesOption(DependenciesMode dependenciesOption) {
        if (logger.isTraceEnabled())
            logger.trace("setting dependencies option to " + dependenciesOption);
        this.dependenciesOption = dependenciesOption;
    }

    public DependenciesMode[] getConfiguredValues() {
        return configuredValues;
    }

    public void setConfiguredValues(DependenciesMode[] configuredValues) {
        this.configuredValues = configuredValues;
    }
}