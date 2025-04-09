package de.iai.ilcd.rest;

import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.registry.DataSetRegistrationData;
import de.iai.ilcd.model.registry.Registry;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;

import java.util.List;

public interface DataSetDeregistrationService {

    /**
     * Applying deregistration request received from registry
     *
     * @param registryId
     * @param changedDS  Dataset to deregister
     * @throws RestWSUnknownException
     */
    public void applyDeregistration(String registryId, DataSet changedDS) throws RestWSUnknownException;

    /**
     * Deregistrating datasets
     *
     * @param registrationData
     * @param reason
     * @param registry
     * @throws AuthenticationException
     * @throws RestWSUnknownException
     */
    public void deregisterDatasets(List<DataSetRegistrationData> registrationData, String reason, Registry registry) throws AuthenticationException,
            RestWSUnknownException;

    /**
     * Deregistrating datasets
     *
     * @param processes
     * @param reason
     * @param registryId
     * @throws AuthenticationException
     * @throws RestWSUnknownException
     */
    public void deregisterDatasets(List<Process> processes, String reason, Long registryId) throws AuthenticationException, RestWSUnknownException;

    /**
     * Automatic dataset deregistration on dataset removal
     *
     * @param dataSet
     * @throws AuthenticationException
     */
    public void deregisterDatasetOnRemoval(Process dataSet) throws AuthenticationException;

    /**
     * Sending stored dataset deregistration request
     *
     * @param registrationData
     * @param reason
     * @param registry
     * @throws AuthenticationException
     * @throws RestWSUnknownException
     */
    public void resubmitDeregisterDatasets(List<DataSetRegistrationData> registrationData, String reason, Registry registry) throws AuthenticationException,
            RestWSUnknownException;
}
