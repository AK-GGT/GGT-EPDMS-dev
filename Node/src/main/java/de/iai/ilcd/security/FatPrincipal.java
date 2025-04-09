package de.iai.ilcd.security;

import de.iai.ilcd.model.security.IUser;

import java.io.Serializable;
import java.security.Principal;

/**
 * Carry around the current user inside SecurityManager
 * instead of repeatedly nagging the UserDAO
 *
 * @author MK
 * @since soda4LCA 7.0.0
 */
public interface FatPrincipal extends Principal, Serializable {

    String getName();

    IUser getSodaUser();
}
