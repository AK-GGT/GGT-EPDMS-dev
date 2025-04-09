package eu.europa.ec.jrc.lca.registry.view.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope("request")
public class NodesForNotAcceptedDSSelectItems extends NodesSelectItems {
    @PostConstruct
    public void init() {
        setEntities(nodeService.getListOfNodesForNotAcceptedDatasets());
        addNotSelectedItem();
    }
}
