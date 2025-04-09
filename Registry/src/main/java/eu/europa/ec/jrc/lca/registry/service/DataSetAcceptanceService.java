package eu.europa.ec.jrc.lca.registry.service;

import eu.europa.ec.jrc.lca.commons.service.exceptions.DatasetIllegalStatusException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;

import java.util.List;

public interface DataSetAcceptanceService {
    /**
     * Accepting dataset registration requests
     *
     * @param datasets
     */
    void acceptDataSets(List<DataSet> datasets);

    /**
     * Accepting dataset update requests
     *
     * @param dataset
     * @throws DatasetIllegalStatusException
     */
    void acceptUpdateDataSet(DataSet dataset) throws DatasetIllegalStatusException;

    /**
     * Rejecting dataset registration requests
     *
     * @param datasets
     * @param reason
     */
    void rejectDataSets(List<DataSet> datasets, String reason);
}
