package eu.europa.ec.jrc.lca.registry.view.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope("request")
public class NodesForAcceptedDSSelectItems extends NodesSelectItems {
    @PostConstruct
    public void init() {
        setEntities(nodeService.getListOfNodesForAcceptedDatasets());
        addNotSelectedItem();
    }
}
