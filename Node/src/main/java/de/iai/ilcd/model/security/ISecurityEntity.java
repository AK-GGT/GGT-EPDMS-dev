package de.iai.ilcd.model.security;

import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

/**
 * Interface for common ID access of security entities
 */
public interface ISecurityEntity extends ILongIdObject {

    /**
     * Get the display name
     *
     * @return display name
     */
    public String getDisplayName();

}
