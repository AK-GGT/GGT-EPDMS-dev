package de.iai.ilcd.model.common;

import de.iai.ilcd.webgui.controller.admin.PushTargetHandler;

/**
 * Target stock. This implementation is used for the {@link PushTargetHandler} to manage available stocks in target node.
 * Therefore it only contains the basic properties of a stock used by the PushTargeHandler.
 *
 * @author sarai
 */
public class TargetStock {

    /*
     * Name of target stock
     */
    String dsName;

    /*
     * description of target stock
     */
    String dsDescription;

    /*
     * The uuid of represented target stock as String
     */
    private String dsUuid;

    /**
     * Gets name of target stock entry.
     *
     * @return Name of target stock entry
     */
    public String getDsName() {
        return dsName;
    }

    /**
     * Sets the name of target stock entry.
     *
     * @param dsName Name of target stock entry
     */
    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    /**
     * Gets description of target stock entry.
     *
     * @return Description of target stock entry
     */
    public String getDsDescription() {
        return dsDescription;
    }

    /**
     * Sets the description of target stock entry.
     *
     * @param dsDescription The description of target stock entry
     */
    public void setDsDescription(String dsDescription) {
        this.dsDescription = dsDescription;
    }

    /**
     * Gets the UUID of target stock.
     *
     * @return The UUID of target stock
     */
    public String getDsUuid() {
        return dsUuid;
    }

    /**
     * Sets the UUID of target stock.
     *
     * @param dsUuid The UUID of target stock
     */
    public void setDsUuid(String dsUuid) {
        this.dsUuid = dsUuid;
    }
}
