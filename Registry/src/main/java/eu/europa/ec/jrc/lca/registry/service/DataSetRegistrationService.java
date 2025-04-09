package eu.europa.ec.jrc.lca.registry.service;

import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;

import java.util.List;

public interface DataSetRegistrationService {
    /**
     * Registering datasets
     *
     * @param datasets
     * @param nodeId
     * @return
     * @throws NodeIllegalStatusException - when node is not approved yet
     */
    List<DataSetRegistrationResult> registerDataSets(List<DataSet> datasets,
                                                     String nodeId) throws NodeIllegalStatusException;
}
