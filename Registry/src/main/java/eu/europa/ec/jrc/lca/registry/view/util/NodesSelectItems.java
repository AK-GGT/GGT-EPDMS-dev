package eu.europa.ec.jrc.lca.registry.view.util;

import eu.europa.ec.jrc.lca.commons.view.util.SelectItemsProducer;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import eu.europa.ec.jrc.lca.registry.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;

public class NodesSelectItems extends SelectItemsProducer<Node> {

    @Autowired
    protected NodeService nodeService;

    @Override
    public Object getValue(Node entity) {
        return entity.getId();
    }

    @Override
    public String getLabel(Node entity) {
        return entity.getNodeId();
    }

}
