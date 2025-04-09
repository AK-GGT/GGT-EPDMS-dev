package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.result.ResultType;
import de.iai.ilcd.webgui.controller.admin.export.DBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AssignUnassignBatchMethod {

    protected static final Map<DataSetDaoType, String> tableNameResolver = Arrays.stream(
                    new Object[][]{{DataSetDaoType.CONTACT, "`datastock_contact`"},
                            {DataSetDaoType.ELEMENTARY_FLOW, "`datastock_flow_elementary`"},
                            {DataSetDaoType.FLOW_PROPERTY, "`datastock_flowproperty`"},
                            {DataSetDaoType.LCIA_METHOD, "`datastock_lciamethod`"},
                            {DataSetDaoType.LIFE_CYCLE_MODEL, "`datastock_lifecyclemodel`"},
                            {DataSetDaoType.PRODUCT_FLOW, "`datastock_flow_product`"},
                            {DataSetDaoType.PROCESS, "`datastock_process`"},
                            {DataSetDaoType.SOURCE, "`datastock_source`"},
                            {DataSetDaoType.UNIT_GROUP, "`datastock_unitgroup`"}})
            .collect(
                    Collectors.toMap(
                            tuple -> (DataSetDaoType) tuple[0],
                            tuple -> (String) tuple[1]));
    protected static final Map<DataSetDaoType, String> dataSetIdColumnNameResolver = Arrays.stream(
                    new Object[][]{{DataSetDaoType.CONTACT, "`contacts_ID`"},
                            {DataSetDaoType.ELEMENTARY_FLOW, "`elementaryFlows_ID`"},
                            {DataSetDaoType.FLOW_PROPERTY, "`flowProperties_ID`"},
                            {DataSetDaoType.LCIA_METHOD, "`lciaMethods_ID`"},
                            {DataSetDaoType.LIFE_CYCLE_MODEL, "`lifeCycleModels_ID`"},
                            {DataSetDaoType.PRODUCT_FLOW, "`productFlows_ID`"},
                            {DataSetDaoType.PROCESS, "`processes_ID`"}, {DataSetDaoType.SOURCE, "`sources_ID`"},
                            {DataSetDaoType.UNIT_GROUP, "`unitGroups_ID`"}})
            .collect(
                    Collectors.toMap(
                            tuple -> (DataSetDaoType) tuple[0],
                            tuple -> (String) tuple[1]));
    protected static final Map<DataSetDaoType, String> dataStockIdColumnNameResolver = Arrays.stream(
                    new Object[][]{{DataSetDaoType.CONTACT, "`containingDataStocks_ID`"},
                            {DataSetDaoType.ELEMENTARY_FLOW, "`containingDataStocks_ID`"},
                            {DataSetDaoType.FLOW_PROPERTY, "`containingDataStocks_ID`"},
                            {DataSetDaoType.LCIA_METHOD, "`containingDataStocks_ID`"},
                            {DataSetDaoType.LIFE_CYCLE_MODEL, "`containingDataStocks_ID`"},
                            {DataSetDaoType.PRODUCT_FLOW, "`containingDataStocks_ID`"},
                            {DataSetDaoType.PROCESS, "`containingDataStocks_ID`"},
                            {DataSetDaoType.SOURCE, "`containingDataStocks_ID`"},
                            {DataSetDaoType.UNIT_GROUP, "`containingDataStocks_ID`"}})
            .collect(
                    Collectors.toMap(
                            tuple -> (DataSetDaoType) tuple[0],
                            tuple -> (String) tuple[1]));
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignUnassignBatchMethod.class);
    protected DataStockMetaData targetMeta;
    private final JdbcTemplate jdbcTemplate;

    public AssignUnassignBatchMethod(DataStockMetaData targetMeta) {
        super();
        validateStaticFields();
        this.jdbcTemplate = DBConfig.getJdbcTemplate();
        this.targetMeta = targetMeta;
    }

    public AssignUnassignBatchMethod(JdbcTemplate jdbcTemplate, DataStockMetaData targetMeta) {
        super();
        validateStaticFields();
        this.jdbcTemplate = jdbcTemplate;
        this.targetMeta = targetMeta;
    }

    protected static Map<DataSetDaoType, String> getDataStockIdColumnNameResolver() {
        return dataStockIdColumnNameResolver;
    }

    /////////////////////////
    // Mandatory Overrides //
    /////////////////////////

    protected static Map<DataSetDaoType, String> getTableNameResolver() {
        return tableNameResolver;
    }

    protected static Map<DataSetDaoType, String> getDataSetIdColumnNameResolver() {
        return dataSetIdColumnNameResolver;
    }

    ////////////////////////
    // Basic construction //
    ////////////////////////

    /**
     * Aliases are needed to formulate native queries (we need to bypass JPA!)
     * <p>
     * Let's, however, make sure, in case of a new data set (resp. data set dao type), we notify
     * developers of the above hard-coded mess that needs to be kept up to date.
     * <p>
     * So...
     * <p>
     * If you've found this following your stacktrace:
     * Probably you will just need to go to db, look up the JPA-produced cross-reference tables (they
     * should begin with 'datastock_') and copy the table/column names into the resp. maps.
     * <p>
     * If you hate this (that's ok): DON'T try to use JPA to assign huge amounts of data sets to some
     * data stock, unless you've found a way to ensure hardware categorically has unlimited heap. (And
     * customers categorically have unlimited patience.) -- (Or maybe you've changed our caching
     * strategy for eclipselink and JPA?)
     */
    private void validateStaticFields() {
        for (DataSetDaoType daoType : DataSetDaoType.values()) {

            String tableName = tableNameResolver.get(daoType);
            if (tableName == null) {
                throw new IllegalArgumentException(
                        "Please make sure all names datastock-dataset-crossReference-tables are known to this class.");
            }

            String dataSetIdColumnName = dataSetIdColumnNameResolver.get(daoType);
            if (dataSetIdColumnName == null) {
                throw new IllegalArgumentException(
                        "Please make sure all names datastock-dataset-crossReference-tables are known to this class.");
            }

            String dataStockIdColumnName = dataStockIdColumnNameResolver.get(daoType);
            if (dataStockIdColumnName == null) {
                throw new IllegalArgumentException(
                        "Please make sure all names datastock-dataset-crossReference-tables are known to this class.");
            }
        }
    }

    protected abstract String generateSingleQueryStringForType(DataSetDaoType daoType);

    protected abstract BatchPreparedStatementSetter getBatchPreparedStatementSetter(
            List<Long> dataSetIds);

    public ResultType applyTo(Set<DataSetReference> references) {
        EnumMap<DataSetDaoType, Set<Long>> idsByDaoType;

        // We need to sort the references by their resp. dao types
        // (read: 'dao types' = 'cross reference tables')
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Sorting references by dao type.");
            }
            // Let's sort the refs by tables
            idsByDaoType = new EnumMap<>(DataSetDaoType.class);
            Arrays.stream(DataSetDaoType.values()).forEach(dt -> idsByDaoType.put(dt, new HashSet<>()));

            // We only care about the ids
            for (DataSetReference ref : references) {
                if (ref.getDaoType() != null && ref.getId() != null) // no nonsense
                {
                    idsByDaoType.get(ref.getDaoType()).add(ref.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("An error occured when attempting to sort the references according to their"
                    + " dao types (= cross reference tables).", e);
            return ResultType.ERROR;
        }

        // Now off to the db
        try {
            // Into each table we batch insert the collected ids
            for (Map.Entry<DataSetDaoType, Set<Long>> typedBatch : idsByDaoType.entrySet()) {
                DataSetDaoType type = typedBatch.getKey();
                List<Long> ids = new ArrayList<>(typedBatch.getValue());

                String queryString = generateSingleQueryStringForType(type);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "Running batch query for dao type {} (generated from the following single query: '{}')",
                            type.name(), queryString);
                }
                jdbcTemplate.batchUpdate(queryString, getBatchPreparedStatementSetter(ids));

                final var dsClass = type.getDatasetClass();
                final var JPACache = PersistenceUtil.getEntityManager()
                        .getEntityManagerFactory()
                        .getCache();
                ids.forEach(id -> JPACache.evict(dsClass, id)); // Because we've not done this with JPA's Entity-Managers, we need to evict the cache manually
            }

            final var em = PersistenceUtil.getEntityManager();
            em.getEntityManagerFactory()
                    .getCache()
                    .evict(targetMeta.getDataStockClass(), targetMeta.getId()); // Let's evict the cached stock from global cache as well, for good measure

            final var stock = em.find(targetMeta.getDataStockClass(), targetMeta.getId());
            final var tx = em.getTransaction();
            tx.begin();
            stock.setModified(true);
            tx.commit();

        } catch (Exception e) {
            LOGGER.error("An error occured when attempting to construct or apply query to db.", e);
            return ResultType.ERROR;
        }

        return ResultType.SUCCESS;
    }

    protected DataStockMetaData getTargetMeta() {
        return this.targetMeta;
    }
}
