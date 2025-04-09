package de.iai.ilcd.rest;

import de.iai.ilcd.delegate.NodeRestServiceBD;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.model.registry.DataSetRegistrationData;
import de.iai.ilcd.model.registry.RegistrationData;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.model.registry.RegistryStatus;
import eu.europa.ec.jrc.lca.commons.domain.NodeCredentials;
import eu.europa.ec.jrc.lca.commons.domain.RegistryCredentials;
import eu.europa.ec.jrc.lca.commons.security.SecurityContext;
import eu.europa.ec.jrc.lca.commons.service.exceptions.*;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import jakarta.mail.internet.AddressException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.spec.RSAPublicKeySpec;
import java.util.Iterator;
import java.util.List;

@Service("nodeRegistrationService")
public class NodeRegistrationServiceImpl implements NodeRegistrationService {

    private static final Logger LOGGER = LogManager.getLogger(NodeRegistrationServiceImpl.class);

    @Autowired
    private RegistryDao registryDao;

    private NetworkNodeDao networkNodeDao = new NetworkNodeDao();

    @Autowired
    private NodeCredentialsDao nodeCredentialsDao;

    @Autowired
    private RegistryCredentialsDao registryCredentialsDao;

    @Autowired
    private RegistrationDataDao registrationDataDao;

    @Autowired
    private SecurityContext securityContext;

    @Autowired
    private DataSetRegistrationDataDao dataSetRegistrationDataDao;

    @Override
    public void registerNode(Registry registry, Node node) throws NodeRegistrationException, RestWSUnexpectedStatusException, RestWSUnknownException,
            AddressException {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("registering node " + node.getNodeId() + " with registry at " + registry.getBaseUrl());
        RSAPublicKeySpec key = getRegistryPublicKey(registry);
        node.getNodeCredentials().encrypt(key);
        node.getNodeCredentials().setNodeId(node.getNodeId());
        RegistryCredentials registryCredentials = NodeRestServiceBD.getInstance(registry).register(node);
        storeData(registry, node, registryCredentials);
    }

    private void storeData(Registry registry, Node node, RegistryCredentials registryCredentials) {
        RegistrationData rData = getRegistrationData(node);
        registry.setStatus(RegistryStatus.PENDING_REGISTRATION);
        registry.setRegistrationData(rData);
        registry.setRegistryCredentials(registryCredentials);
        registry.setNodeCredentials(node.getNodeCredentials());
        registry.getNodeCredentials().encrypt(securityContext.getPublicKey());
        registryDao.saveOrUpdate(registry);
    }

    private RegistrationData getRegistrationData(Node node) {
        RegistrationData rData = new RegistrationData();
        rData.setNodeBaseUrl(node.getBaseUrl());
        rData.setNodeId(node.getNodeId());
        return rData;
    }

    private RSAPublicKeySpec getRegistryPublicKey(Registry registry) throws RestWSUnknownException {
        return NodeRestServiceBD.getInstance(registry).getPublicKey();
    }

    @Override
    public void deregisterNode(Registry registry, NodeCredentials credentials) throws RestWSUnexpectedStatusException, RestWSUnknownException,
            AuthenticationException, NodeDegistrationException {

        validateDatasets(registry);
        removeNotAcceptedDatasetRegistrationData(registry);
        removePendingDatasetRegistrationData(registry);

        RSAPublicKeySpec key = getRegistryPublicKey(registry);
        credentials.encryptWithNonce(key);
        NodeRestServiceBD.getInstance(registry).deregister(credentials);

        registry.setStatus(RegistryStatus.NOT_REGISTERED);
        clearNodesList(registry);
        clearRegistrationData(registry);
        registryDao.saveOrUpdate(registry);
    }

    @Override
    public void deregisterNode(Registry registry) {
        removeNotAcceptedDatasetRegistrationData(registry);
        removePendingDatasetRegistrationData(registry);
        removeAcceptedRegistrationData(registry);

        registry.setStatus(RegistryStatus.NOT_REGISTERED);
        clearNodesList(registry);
        clearRegistrationData(registry);
        registryDao.saveOrUpdate(registry);

        LOGGER.info("================= deregistering node");
    }

    @Override
    public void clearRegistrationData(Registry registry) {
        // workaround for issue #245
        long regDataId = registry.getRegistrationData().getId();
        registry.setRegistrationData(null);
        long nodeCredentialsId = registry.getNodeCredentials().getId();
        registry.setNodeCredentials(null);
        long registryCredentialsId = registry.getRegistryCredentials().getId();
        registry.setNodeCredentials(null);
        registry.setRegistryCredentials(null);
        registryDao.saveOrUpdate(registry);
        registrationDataDao.remove(regDataId);
        nodeCredentialsDao.remove(nodeCredentialsId);
        registryCredentialsDao.remove(registryCredentialsId);
        // end workaround

        /*
         * was:
         * registrationDataDao.remove( registry.getRegistrationData().getId() );
         * registry.setRegistrationData( null );
         * nodeCredentialsDao.remove( registry.getNodeCredentials().getId() );
         * registry.setNodeCredentials( null );
         * registryCredentialsDao.remove( registry.getRegistryCredentials().getId() );
         * registry.setRegistryCredentials( null );
         */
    }

    private void clearNodesList(Registry registry) {
        for (Iterator<NetworkNode> iter = registry.getNodes().iterator(); iter.hasNext(); ) {
            NetworkNode nn = iter.next();
            iter.remove();
            networkNodeDao.remove(nn);
        }
    }

    private void validateDatasets(Registry registry) throws NodeDegistrationException {
        if (dataSetRegistrationDataDao.getRegisteredAccepted(registry.getId()).size() != 0) {
            throw new NodeDegistrationException("there are registered datasets");
        }
    }

    private void removeNotAcceptedDatasetRegistrationData(Registry registry) {
        List<DataSetRegistrationData> notAccepted = dataSetRegistrationDataDao.getRegisteredNotAccepted(registry.getId());
        for (DataSetRegistrationData dsrd : notAccepted) {
            dataSetRegistrationDataDao.remove(dsrd.getId());
        }
    }

    private void removePendingDatasetRegistrationData(Registry registry) {
        List<DataSetRegistrationData> pending = dataSetRegistrationDataDao.getRegisteredPendingDeregistration(registry.getId());
        for (DataSetRegistrationData dsrd : pending) {
            dataSetRegistrationDataDao.remove(dsrd.getId());
        }
    }

    private void removeAcceptedRegistrationData(Registry registry) {
        List<DataSetRegistrationData> pending = dataSetRegistrationDataDao.getRegisteredAccepted(registry.getId());
        for (DataSetRegistrationData dsrd : pending) {
            dataSetRegistrationDataDao.remove(dsrd.getId());
        }
    }
}
