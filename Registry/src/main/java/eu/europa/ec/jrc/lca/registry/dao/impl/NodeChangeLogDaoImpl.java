package eu.europa.ec.jrc.lca.registry.dao.impl;

import eu.europa.ec.jrc.lca.registry.dao.NodeChangeLogDao;
import eu.europa.ec.jrc.lca.registry.domain.NodeChangeLog;
import org.springframework.stereotype.Repository;

@Repository(value = "nodeChangeLogDao")
public class NodeChangeLogDaoImpl extends GenericDAOImpl<NodeChangeLog, Long> implements NodeChangeLogDao {


}
