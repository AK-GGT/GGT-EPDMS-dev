package de.iai.ilcd.model.dao;

import eu.europa.ec.jrc.lca.commons.domain.NodeCredentials;
import org.springframework.stereotype.Repository;

@Repository(value = "nodeCredentialsDao")
public class NodeCredentialsDaoImpl extends GenericDAOImpl<NodeCredentials, Long> implements NodeCredentialsDao {

}
