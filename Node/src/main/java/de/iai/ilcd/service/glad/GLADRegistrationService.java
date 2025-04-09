package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.rest.util.InvalidGLADUrlException;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.LazyLoader;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;

import java.util.List;

/**
 * The service object for Data registration to GLAD.
 * Manages data set registrations to GLAD.
 *
 * @author sarai
 */
public interface GLADRegistrationService extends LazyLoader<GLADRegistrationData> {

    /**
     * Registers datasets to GLAD.
     *
     * @param processes The proccesses that shall be registered to GLAD
     * @return A list of results containing the information whether eahch
     * proccess was successfully registered or not (and why)
     * @throws NodeIllegalStatusException
     * @throws RestWSUnknownException
     * @throws AuthenticationException
     */
    public List<DataSetRegistrationResult> register(List<Process> processes)
            throws NodeIllegalStatusException, RestWSUnknownException, AuthenticationException, InvalidGLADUrlException;

    /**
     * Gets all registered data sets.
     *
     * @return A list of all GLAD registrations
     */
    public List<GLADRegistrationData> getRegistered();

    /**
     * Gets list of all registered data sets according to given process.
     *
     * @param process
     * @return
     */
    public List<GLADRegistrationData> getListOfRegistrations(Process process);

    /**
     * Deregisters given processes from GLAD.
     *
     * @param processes The list of processes that shall be deregistered from GLAD
     */
    public void deregister(List<Process> processes);

}
