package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.persistence.PersistenceUtil;
import eu.europa.ec.jrc.lca.commons.dao.SearchParameters;
import eu.europa.ec.jrc.lca.commons.service.LazyLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class NetworkNodeDao extends GenericDAOImpl<NetworkNode, Long> implements LazyLoader<NetworkNode> {

    private static final Logger logger = LogManager.getLogger(NetworkNodeDao.class);

    @SuppressWarnings("unchecked")
    /**
     * Get List of all network nodes
     *
     * @return list of all network nodes
     */
    public List<NetworkNode> getNetworkNodes() {
        EntityManager em = PersistenceUtil.getEntityManager();

        List<NetworkNode> networkNodes = (List<NetworkNode>) em.createQuery("select node from NetworkNode node").getResultList();

        return networkNodes;
    }

    /**
     * Get a network node by its node ID (not database id!)
     *
     * @param nodeId {@link NetworkNode#getNodeId() node id} (not database id!)
     */
    public NetworkNode getNetworkNode(String nodeId, Long registryId) {
        EntityManager em = PersistenceUtil.getEntityManager();

        NetworkNode networkNode = null;
        try {
            networkNode = (NetworkNode) em.createQuery("select node from NetworkNode node where node.nodeId=:nodeId and node.registry.id=:registryId")
                    .setParameter("nodeId", nodeId).setParameter("registryId", registryId).getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return networkNode;
    }

    public NetworkNode getNetworkNode(String nodeId, String registryUuid) {
        EntityManager em = PersistenceUtil.getEntityManager();

        NetworkNode networkNode = null;
        try {
            networkNode = (NetworkNode) em.createQuery("select node from NetworkNode node where node.nodeId=:nodeId and node.registry.uuid=:registryUuid")
                    .setParameter("nodeId", nodeId).setParameter("registryUuid", registryUuid).getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return networkNode;
    }

    public NetworkNode getNetworkNode(String nodeId) {
        EntityManager em = PersistenceUtil.getEntityManager();

        NetworkNode networkNode = null;
        try {
            networkNode = (NetworkNode) em.createQuery("select node from NetworkNode node where node.nodeId=:nodeId").setParameter("nodeId", nodeId)
                    .getSingleResult();
        } catch (NoResultException e) {
            // we do nothing here; just return null
        }
        return networkNode;
    }

    public NetworkNode getNetworkNode(long id) {
        EntityManager em = PersistenceUtil.getEntityManager();

        NetworkNode networkNode = (NetworkNode) em.find(NetworkNode.class, Long.valueOf(id));

        return networkNode;
    }

    public NetworkNode getUnitGroupById(String id) {
        return getNetworkNode(Long.parseLong(id));
    }

    /**
     * Get Node by database id
     *
     * @param id id of node
     * @return loaded node
     */
    public NetworkNode getNetworkNodeById(Long id) {
        return getNetworkNode(id);
    }

    public boolean checkAndPersist(NetworkNode networkNode, PersistType pType, PrintWriter out) {
        EntityManager em = PersistenceUtil.getEntityManager();

        NetworkNode existingNode = this.getNetworkNode(networkNode.getNodeId());
        if (existingNode != null) {
            if (pType == PersistType.ONLYNEW) {
                if (out != null)
                    out.println("Warning: Network node with this nodeId already exists; change name or choose option to overwrite");
                return false;
            }
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (existingNode != null && (pType == PersistType.MERGE)) {
                if (out != null) {
                    out.println("Notice: network node with this nodeId already exists; will replace it with this entry");
                }
                // delete first the existing one, we will use the new one
                em.remove(existingNode);
            }

            em.persist(networkNode);
            tx.commit();
            return true;
        } catch (Exception e) {
            logger.error("cannot persist network node {}", networkNode.getNodeId());
            logger.error("stacktrace is: ", e);
            tx.rollback();
        }
        return false;
    }

    public void remove(NetworkNode node) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            NetworkNode managed = em.find(NetworkNode.class, node.getId());
            em.remove(managed);
            tx.commit();
        } catch (Exception e) {
        }
    }

    public NetworkNode merge(NetworkNode node) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(node);
            tx.commit();
            return node;
        } catch (Exception e) {
            logger.error("cannot merge changes to network node {}", node.getNodeId());
            logger.error("stacktrace is: ", e);
            tx.rollback();
        }
        return null;
    }

    @Override
    public List<NetworkNode> loadLazy(SearchParameters sp) {
        return find(sp);
    }

    public List<NetworkNode> getRemoteNetworkNodesFromRegistry(String registryUUID) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em
                .createQuery("select node from NetworkNode node where node.registry.uuid=:registryUUID and node.nodeId!=node.registry.nodeCredentials.nodeId")
                .setParameter("registryUUID", registryUUID).getResultList();
    }

    public List<NetworkNode> getRemoteNetworkNodes() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select node from NetworkNode node where node.registry is null").getResultList();
    }

    public Long getAllCount() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (Long) em.createQuery("select count(node) from NetworkNode node").getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<NetworkNode> get(int startIndex, int pageSize) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select node from NetworkNode node").setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
    }

}
