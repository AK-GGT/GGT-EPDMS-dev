package eu.europa.ec.jrc.lca.registry.dao.impl;

import eu.europa.ec.jrc.lca.registry.dao.AuditLogDao;
import eu.europa.ec.jrc.lca.registry.domain.NodeAuditLog;
import org.springframework.stereotype.Repository;

@Repository(value = "auditLogDao")
public class AuditLogDaoImpl extends GenericDAOImpl<NodeAuditLog, Long> implements
        AuditLogDao {

}
