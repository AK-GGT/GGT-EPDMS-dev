package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.dao.GenericDAO;
import de.iai.ilcd.model.process.Process;

import java.util.List;

/**
 * The Data Access Object (DAO) of GLAD registrations.
 * Manages all data sets which are registered to GLAD.
 *
 * @author sarai
 */
public interface GLADRegistrationDataDao extends GenericDAO<GLADRegistrationData, Long> {

    /**
     * Gets all to GLAD registered data sets.
     *
     * @return A list of all data sets registered to GLAD:
     */
    List<GLADRegistrationData> getRegistered();

    /**
     * Gets the list of registrations to GLAD from process.
     *
     * @param process The process of which registrations are wanted
     * @return A List of registrations to glad from process
     */
    List<GLADRegistrationData> getListOfRegistrations(Process process);

    /**
     * Finds a GLAD registration by given UUID and version.
     *
     * @param UUID    The UUID of wanted registration
     * @param version The version of wanted registration
     * @return A GLAD registration containing given UUID and version
     */
    GLADRegistrationData findByUUIDAndVersion(String UUID, String version);

    /**
     * Finds a GLAD registration by given UUID.
     *
     * @param UUID The UUID of wanted registration
     * @return A GLAD registration containing given UUID
     */
    GLADRegistrationData findByUUID(String UUID);

}
