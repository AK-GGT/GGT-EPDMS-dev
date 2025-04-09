package eu.europa.ec.jrc.lca.registry.domain;

import javax.persistence.metamodel.SingularAttribute;
import java.util.Date;

@javax.persistence.metamodel.StaticMetamodel(eu.europa.ec.jrc.lca.registry.domain.NodeChangeLog.class)
public class NodeChangeLog_ {

    public static volatile SingularAttribute<NodeChangeLog, Long> id;
    public static volatile SingularAttribute<NodeChangeLog, String> nodeId;
    public static volatile SingularAttribute<NodeChangeLog, Date> operationDate;
    public static volatile SingularAttribute<NodeChangeLog, OperationType> operationType;
}
