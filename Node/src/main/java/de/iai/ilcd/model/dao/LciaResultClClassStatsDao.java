package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.process.LciaResultClClassStats;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

/**
 * DAO for {@link LciaResultClClassStats}
 */
public class LciaResultClClassStatsDao extends AbstractLongIdObjectDao<LciaResultClClassStats> {

    private static Logger logger = LogManager.getLogger(LciaResultClClassStatsDao.class);

    /**
     * Create DAO
     */
    public LciaResultClClassStatsDao() {
        super(LciaResultClClassStats.class.getSimpleName(), LciaResultClClassStats.class);
    }

    /**
     * Get statistics objects for UUID of unit group that are currently not in dirty state (dirty means
     * {@link LciaResultClClassStats#getTsCalculated() tsCalculated} &lt;
     * {@link LciaResultClClassStats#getTsLastChange() tsChanged})
     *
     * @param methodUuid UUID of unit group
     * @return list of statistic objects
     */
    @SuppressWarnings("unchecked")
    public List<LciaResultClClassStats> getNotDirty(String methodUuid, String referenceFlowpropertyUuid) {
        EntityManager em = PersistenceUtil.getEntityManager();

        Query q = em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.tsCalculated >= a.tsLastChange AND a.methodUuid=:methodUuid AND a.referenceFlowpropertyUuid=:referenceFlowpropertyUuid");
        q.setParameter("methodUuid", methodUuid);
        q.setParameter("referenceFlowpropertyUuid", referenceFlowpropertyUuid);

        return q.getResultList();
    }

    /**
     * Get statistics objects for stock ID that are updated (since last change).
     *
     * @param stockId The stock all statistics should include
     * @return A list of statistics that include given stock
     */
    @SuppressWarnings("unchecked")
    public List<LciaResultClClassStats> getByStock(String stockId) {
        EntityManager em = PersistenceUtil.getEntityManager();

        Query q = em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.tsCalculated >= a.tsLastChange  AND a.stockIds LIKE :stockId");
        q.setParameter("stockId", "%" + stockId + "%");

        return q.getResultList();
    }

    /**
     * Get the statistics object
     *
     * @param clid       class id
     * @param methodUuid unit group uuid
     * @param module     module
     * @return statistics object
     */
    public LciaResultClClassStats get(String clid, String methodUuid, String module, List<Long> stockIds, String referenceFlowpropertyUuid) {
        EntityManager em = PersistenceUtil.getEntityManager();
        if (logger.isDebugEnabled()) {
            logger.debug("stock IDs: " + stockIds);
        }

        LciaResultClClassStats queriedStats = null;
        String strStockIds = StringUtils.join(stockIds, ",");
        try {
            Query q = em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.clid=:clid AND a.stockIds=:stockIds AND a.methodUuid=:methodUuid AND a.module=:module AND a.referenceFlowpropertyUuid=:referenceFlowpropertyUuid");
            q.setParameter("clid", clid);
            q.setParameter("stockIds", strStockIds);
            q.setParameter("methodUuid", methodUuid);
            q.setParameter("module", module);
            q.setParameter("referenceFlowpropertyUuid", referenceFlowpropertyUuid);
            if (logger.isTraceEnabled()) {
                logger.trace("get query: " + q.toString());
            }

            queriedStats = (LciaResultClClassStats) q.getSingleResult();
            if (logger.isTraceEnabled()) {
                logger.trace("Category ID: " + queriedStats.getClid());
                logger.trace("Maximum of Stats: " + queriedStats.getMax());
                logger.trace("Mean of stats: " + queriedStats.getMean());
                logger.trace("Minimum of stats: " + queriedStats.getMin());
                logger.trace("Method UUID: " + queriedStats.getMethodUuid());
                logger.trace("Module: " + queriedStats.getModule());
                logger.trace("UUID of reference flow property: " + queriedStats.getReferenceFlowpropertyUuid());
                logger.trace("Reference Unit: " + queriedStats.getRefUnit());
                logger.trace("Timestamp of calulation: " + queriedStats.getTsCalculated());
                logger.trace("Timestamp of last change: " + queriedStats.getTsLastChange());
            }

            if (queriedStats.getTsCalculated() < queriedStats.getTsLastChange()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("old statistics.\n Recalculating results...");
                }
                return this.recalculateLciaResClClassStats(queriedStats, stockIds, referenceFlowpropertyUuid);
            } else {
                logger.debug("Statistics are still up to date");
                return queriedStats;
            }
        } catch (NoResultException nre) {
            logger.debug("No statistics found on query. Creating new statistics...");
            try {
                return this.createNotExisting(clid, methodUuid, module, stockIds, referenceFlowpropertyUuid);
            } catch (PersistException pex) {
                logger.error("An error occured during persisting: " + pex);
                return null;
            }
        } catch (MergeException mex) {
            logger.error("Error while merging: " + mex);
            return null;
        }
    }

    /**
     * Create statistics object for combination of <code>clid</code>, <code>ugUuid</code> and <code>module</code> that
     * does not yet exist
     *
     * @param clid       class id
     * @param methodUuid unit group uuid
     * @param module     module
     * @return statistics object
     * @throws PersistException on persist errors
     */
    private LciaResultClClassStats createNotExisting(String clid, String methodUuid, String module, List<Long> stockIds, String referenceFlowpropertyUuid) throws PersistException {
        LciaResultClClassStats createdStats = this.calculateLciaResClClassStats(null, clid, methodUuid, module, stockIds, referenceFlowpropertyUuid);
        this.persist(createdStats);
        return createdStats;
    }

    /**
     * Re-calculate statistics for (already managed) object
     *
     * @param stats statistics object to re-calculate
     * @return updated statistics object
     * @throws MergeException on merge errors
     */
    private LciaResultClClassStats recalculateLciaResClClassStats(LciaResultClClassStats stats, List<Long> stockIds, String referenceFlowpropertyUuid) throws MergeException {
        stats = this.calculateLciaResClClassStats(stats, stats.getClid(), stats.getMethodUuid(), stats.getModule(), stockIds, referenceFlowpropertyUuid);
        return this.merge(stats);
    }

    /**
     * Calculate statistics
     *
     * @param objToFill  existing object to set values for, might be <code>null</code> to create fresh object
     * @param clid       class id
     * @param methodUuid unit group uuid
     * @param module     module
     * @return statistics object
     */
    private LciaResultClClassStats calculateLciaResClClassStats(LciaResultClClassStats objToFill, String clid, String methodUuid, String module, List<Long> stockIds, String referenceFlowPropertyUuid) {

        //For testing and modulation currently changed

        String strStockIds = StringUtils.join(stockIds, ",");
        if (logger.isDebugEnabled()) {
            logger.debug("stock IDs: " + strStockIds);
            logger.debug("query parameters: ");
            logger.debug("category: " + clid);
            logger.debug("UUID of reference flow property: " + referenceFlowPropertyUuid);
            logger.debug("UUID of LCIA result: " + methodUuid);
            logger.debug("Module: " + module);
        }

        String sql = "SELECT gm.`UUID` AS `method`, rfp.`UUID` AS `ref_flowproperty`, f.`referencePropertyUnit_cache` AS `ref_unit`, la.`module`, MIN(la.`scaled_value`) AS `min`, MAX(la.`scaled_value`) AS `max`, AVG(la.`scaled_value`) AS `avg`, COUNT(la.`scaled_value`) AS `reference_count` " +
                " FROM `lciaresult` l" +
                " LEFT JOIN `process_lciaresult` pl ON (l.ID=pl.lciaResults_ID)" +
                " LEFT JOIN `process_exchange` pe ON pe.Process_ID = pl.Process_ID" +
                " LEFT JOIN `exchange` e ON e.ID = pe.exchanges_ID" +
                " LEFT JOIN `flow_common` f ON f.ID = e.FLOW_ID" +
                " LEFT JOIN `flow_propertydescriptions` fpd ON fpd.Flow_ID = f.ID" +
                " LEFT JOIN `flowpropertydescription` fpd2 ON fpd2.ID = fpd.propertyDescriptions_ID" +
                " LEFT JOIN `flowproperty` rfp ON rfp.ID = fpd2.FLOWPROPERTY_ID" +
                " LEFT JOIN `lciaresult_amounts` la ON (l.ID=la.lciaresult_id)" +
                " LEFT JOIN `globalreference` gm ON (gm.ID=l.METHODREFERENCE_ID)" +
                " WHERE la.`scaled_value` IS NOT NULL" +
                " AND pl.Process_ID IN(" +
                " SELECT DISTINCT(p.ID)" +
                " FROM `process` p" +
                " LEFT JOIN `process_classifications` pc ON (p.ID=pc.Process_ID)" +
                " LEFT JOIN `datastock_process` ds ON (p.ID=ds.processes_ID)" +
                " LEFT JOIN `classification_clclass` ccc ON (pc.classifications_ID=ccc.Classification_ID)" +
                " LEFT JOIN `clclass` cc ON (cc.ID=ccc.classes_ID)" +
                " WHERE " +
                " cc.CLID= ?" +
                " AND ds.containingDataStocks_ID IN ( " + strStockIds + " ) )" +
                " AND rfp.UUID = ?" +
                " AND gm.`UUID`= ?" +
                " AND la.`module`= ?" +
                " GROUP BY gm.`UUID`, la.`module`";

        EntityManager em = PersistenceUtil.getEntityManager();

        Query q = em.createNativeQuery(sql);
        q.setParameter(1, clid);
        q.setParameter(2, referenceFlowPropertyUuid);
        q.setParameter(3, methodUuid);
        q.setParameter(4, module);

        logger.debug("calculated query: " + q.toString());

        if (logger.isTraceEnabled()) {
            logger.trace("ObjToFill is null: " + (objToFill == null));
        }

        LciaResultClClassStats stats = objToFill != null ? objToFill : new LciaResultClClassStats();

        if (objToFill == null) {
            stats.setClid(clid);
            stats.setMethodUuid(methodUuid);
            stats.setReferenceFlowpropertyUuid(referenceFlowPropertyUuid);
            stats.setModule(module);
            stats.setStockIds(strStockIds);
        }

        try {
            Object[] res = (Object[]) q.getSingleResult();

            // set values and calc time always

            try {
                stats.setMin(((Number) res[4]).doubleValue());
                stats.setMax(((Number) res[5]).doubleValue());
                stats.setMean(((Number) res[6]).doubleValue());
                stats.setReferenceCount(((Number) res[7]).intValue());

            } catch (NullPointerException e) {
                logger.debug("result is null!!");
                stats.setMin(null);
                stats.setMax(null);
                stats.setMean(null);
                stats.setReferenceCount(null);
            }

            final long now = System.currentTimeMillis();
            stats.setTsCalculated(now);

            // rest of meta data only for new objects
            if (objToFill == null) {
                if (res != null) {
                    stats.setRefUnit((String) res[2]);
                } else {
                    stats.setRefUnit(null);
                }
                stats.setTsLastChange(now);
            }
            return stats;

        } catch (NoResultException nre) {
            logger.debug("No result found since no min/max found. Setting stats manually:");
            stats.setMin(null);
            stats.setMax(null);
            stats.setMean(null);
            stats.setReferenceCount(null);
            if (objToFill == null) {
                final long now = System.currentTimeMillis();
                stats.setTsCalculated(now);
                stats.setTsLastChange(now);
                stats.setRefUnit(null);
            }
            return stats;
        }

    }

}
