package eu.europa.ec.jrc.lca.registry.dao;

import eu.europa.ec.jrc.lca.registry.domain.DataSet;

import java.util.List;

public interface DataSetDao extends GenericDAO<DataSet, Long> {

    DataSet findAcceptedByUUID(String uuid);

    DataSet findAcceptedByUUIDAndNode(String uuid, String nodeId);

    List<DataSet> findByUUID(String uuid);

    List<DataSet> findByUUIDAndNode(String uuid, String nodeId);

    DataSet findByUUIDAndVersionAndNode(String uuid, String version, String nodeId);

    int updateAssociatedDatasets(DataSet ds);

    int removeByNode(String nodeId);

    List<DataSet> findByNode(String nodeId);

    List<DataSet> findAcceptedByNode(String nodeId);
}
