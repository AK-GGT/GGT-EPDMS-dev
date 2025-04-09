package eu.europa.ec.jrc.lca.registry.dao;

import eu.europa.ec.jrc.lca.registry.domain.DataSetStatus;
import eu.europa.ec.jrc.lca.registry.domain.Node;

import java.util.List;

public interface NodeDao extends GenericDAO<Node, Long> {

    Long getCountByNodeIdAndUrl(String nodeId, String url);

    Node getByNodeId(String nodeID);

    List<Node> getListOfNotApprovedNodes();

    List<Node> getListOfApprovedNodes();

    List<Node> getListOfNodesForDatasetsWithStatus(DataSetStatus[] status);
}
