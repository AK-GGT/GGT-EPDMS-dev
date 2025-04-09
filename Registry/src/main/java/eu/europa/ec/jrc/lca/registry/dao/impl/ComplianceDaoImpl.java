package eu.europa.ec.jrc.lca.registry.dao.impl;

import eu.europa.ec.jrc.lca.registry.dao.ComplianceDao;
import eu.europa.ec.jrc.lca.registry.domain.Compliance;
import org.springframework.stereotype.Repository;

@Repository(value = "complianceDao")
public class ComplianceDaoImpl extends GenericDAOImpl<Compliance, Long>
        implements ComplianceDao {

}
