package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static de.iai.ilcd.model.dao.AbstractDao.cleanParams;

/**
 * @author clemens.duepmeier
 */
public class ClassificationDao {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(ClassificationDao.class);

    /**
     * Get a ClClass by clId
     *
     * @param clId the clId to get for
     * @return loaded cl class
     */
    public ClClass getByClId(String clId) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery("SELECT c FROM ClClass c WHERE c.clId=:clId", ClClass.class);
        q.setParameter("clId", clId);

        @SuppressWarnings("unchecked")
        List<ClClass> results = q.getResultList();
        if (results != null && results.get(0) != null)
            return results.get(0);
        else
            return null;
    }

    /**
     * Get a ClClass by clId
     *
     * @param clId the clId to get for
     * @return loaded cl class
     */
    public String getNameByClId(DataSetType datasetType, String clId) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery("SELECT DISTINCT c.name FROM ClClass c WHERE c.clId=:clId AND c.dataSetType=:dsType", ClClass.class);
        q.setParameter("clId", clId);
        q.setParameter("dsType", datasetType);

        @SuppressWarnings("unchecked")
        List<String> results = q.getResultList();
        if (results != null && results.get(0) != null)
            return results.get(0);
        else
            return null;
    }

    /**
     * Get top classes for provided data stocks
     *
     * @param dataSetType          type of data set
     * @param classificationSystem classification system
     * @param stocks               data stocks
     * @return top classes
     */
    public List<ClClass> getTopClasses(DataSetType dataSetType, String classificationSystem, IDataStockMetaData... stocks) {
        return getTopClasses(dataSetType, classificationSystem, null, stocks);
    }

    /**
     * Get top classes for provided data stocks with additional parameters for process datasets
     *
     * @param dataSetType          type of data set
     * @param classificationSystem classification system
     * @param stocks               data stocks
     * @return top classes
     */
    public List<ClClass> getTopClasses(DataSetType dataSetType, String classificationSystem, ValueParser params, IDataStockMetaData... stocks) {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> joins = new ArrayList<>();
        List<String> wheres = new ArrayList<>();

        joins.add("LEFT JOIN a.classifications cl");
        joins.add("LEFT JOIN cl.classes clz");

        wheres.add("clz.level=0");
        if (StringUtils.isNotBlank(classificationSystem)) {
            wheres.add("cl.name=:claName");
            paramMap.put("claName", classificationSystem);
        }

        // only for processes
        boolean hasDataSourceParam = (params != null) && dataSetType.equals(DataSetType.PROCESS) && !StringUtils.isBlank(params.getString("dataSource"));

        if (hasDataSourceParam) {
            joins.add("LEFT JOIN a.dataSources datasource ");
            ProcessDao.addDatasourcesWhereClauses("a", params, wheres, paramMap);
        }

        boolean hasComplianceParams = params != null && dataSetType.equals(DataSetType.PROCESS) && cleanParams(params.getStrings("compliance")).length > 0;
        if (hasComplianceParams) {
            joins.add("LEFT JOIN a.complianceSystems compliancesystems ");
            joins.add("LEFT JOIN compliancesystems.sourceReference compliancesourceref ");
            ProcessDao.addComplianceWhereClauses("a", params, wheres, paramMap);
        }

        this.addClassesStockClauses(joins, wheres, paramMap, stocks);

        return selectDistinctClClassesAsc("clz", dataSetType, joins, paramMap, wheres);
    }

    /**
     * Get top classes for provided data stocks
     *
     * @param dataSetType type of data set
     * @param stocks      data stocks
     * @return top classes
     */
    public List<ClClass> getTopClasses(DataSetType dataSetType, IDataStockMetaData... stocks) {
        return this.getTopClasses(dataSetType, null, stocks);
    }

    /**
     * Get the classification systems for provided top level class
     *
     * @param dataSetType       type of data set
     * @param topLevelClassName name of top level class
     * @return names of the classification systems
     */
    @SuppressWarnings("unchecked")
    public List<String> getCategorySystemsOfTopLevelClass(DataSetType dataSetType, String topLevelClassName) {
        EntityManager em = PersistenceUtil.getEntityManager();

        Query q = em.createQuery(
                "SELECT DISTINCT cla.name FROM Classification cla LEFT JOIN cla.classes clz WHERE clz.name=:clzName AND clz.dataSetType=:type ORDER BY cla.name ASC",
                String.class);

        q.setParameter("type", dataSetType);
        q.setParameter("clzName", topLevelClassName);

        return q.getResultList();
    }

    /**
     * Get sub classes
     *
     * @param dataSetType          type of data set
     * @param classificationSystem name of classification system
     * @param classes              path of classes
     * @param stocks               data stocks
     * @return sub classes
     */
    public List<ClClass> getSubClasses(DataSetType dataSetType, String classificationSystem, List<String> classes, boolean mostRecentVersionOnly,
                                       IDataStockMetaData... stocks) {
        return getSubClasses(dataSetType, classificationSystem, classes, null, mostRecentVersionOnly, stocks);
    }

    /**
     * Get sub classes
     *
     * @param dataSetType          type of data set
     * @param classificationSystem name of classification system
     * @param classes              path of classes
     * @param params               additional parameters like dataSource
     * @param stocks               data stocks
     * @return sub classes
     */
    public List<ClClass> getSubClasses(DataSetType dataSetType, String classificationSystem, List<String> classes, ValueParser params, boolean mostRecentVersionOnly,
                                       IDataStockMetaData... stocks) {

        if (CollectionUtils.isEmpty(classes)) {
            throw new IllegalArgumentException("Empty classes path not allowed!");
        }

        List<String> wheres = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        final int clSize = classes.size();

        List<String> joins = new ArrayList<>();

        joins.add("LEFT JOIN a.classifications cl");

        // only for processes
        boolean hasDataSourceParam = (params != null) && dataSetType.equals(DataSetType.PROCESS) && !StringUtils.isBlank(params.getString("dataSource"));

        if (hasDataSourceParam) {
            joins.add("LEFT JOIN a.dataSources datasource ");
            ProcessDao.addDatasourcesWhereClauses("a", params, wheres, paramMap);
        }

        this.addClassesStockClauses(joins, wheres, paramMap, stocks);

        for (int level = 0; level <= clSize; level++) {
            final String alias = "clz" + level;
            final String nameParam = "name" + level;

            joins.add(" JOIN cl.classes " + alias);
            wheres.add(alias + ".level=" + level);
            wheres.add(alias + ".dataSetType=:dsType");
            if (level < clSize) {
                wheres.add(alias + ".name=:" + nameParam);
                paramMap.put(nameParam, classes.get(level));
            }
        }

        if (mostRecentVersionOnly) {
            wheres.add(DataSetDao.buildMostRecentVersionsOnlySubQuery("a", this.getEntityName(dataSetType), null, StringUtils.join(joins, " "),
                    wheres));
        }

        paramMap.put("dsType", dataSetType);

        return selectDistinctClClassesAsc("clz" + clSize, dataSetType, joins, paramMap, wheres);
    }

    /**
     * Add stock classes for classes queries. Prerequisites:
     * <ul>
     * <li>alias for the data set object must be <code>a</code></li>
     * <li>alias <code>ads</code> must be unused</li>
     * <li>and parameter name <code>rootDsId</code> must be unused</li>
     * </ul>
     *
     * @param joins    list of joins
     * @param wheres   list of where clauses
     * @param paramMap parameter map
     * @param stocks   data stock meta data
     * @see #getSubClasses(DataSetType, String, List, boolean, IDataStockMetaData...)
     * @see #getTopClasses(DataSetType, String, IDataStockMetaData...)
     */
    private void addClassesStockClauses(List<String> joins, List<String> wheres, Map<String, Object> paramMap, IDataStockMetaData... stocks) {
        if (stocks != null) {
            List<String> stockClauses = new ArrayList<>();
            List<String> rootStockClauses = new ArrayList<>();
            List<Long> dsIds = new ArrayList<>();
            boolean dsJoinDone = false;
            for (IDataStockMetaData m : stocks) {
                // root data stock
                if (m.isRoot()) {
                    String paramName = "rootDsId" + m.getId();
                    rootStockClauses.add("a.rootDataStock.id=:" + paramName);
                    paramMap.put(paramName, m.getId());
                }
                // non root data stock
                else {
                    if (!dsJoinDone) {
                        joins.add("LEFT JOIN a.containingDataStocks ads");
                        dsJoinDone = true;
                    }
                    dsIds.add(m.getId());
                }
            }
            if (!rootStockClauses.isEmpty()) {
                stockClauses.add("(" + StringUtils.join(rootStockClauses, " OR ") + ")");
            }
            if (dsJoinDone && !dsIds.isEmpty()) {
                stockClauses.add("ads.id IN(" + StringUtils.join(dsIds, ',') + ")");
            }
            if (!stockClauses.isEmpty()) {
                wheres.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
            }
        }
    }

    /**
     * Get sub classes
     *
     * @param dataSetType          type of data set
     * @param classificationSystem name of classification system
     * @param classname            name of class
     * @param level                level of class
     * @param stocks               data stocks
     * @return sub classes
     */
    public List<ClClass> getSubClasses(DataSetType dataSetType, String classificationSystem, String classname, int level, boolean mostRecentVersionOnly,
                                       IDataStockMetaData... stocks) {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> joins = new ArrayList<>();
        List<String> wheres = new ArrayList<>();

        joins.add("LEFT JOIN a.classifications cl");
        joins.add("JOIN cl.classes clz0");
        joins.add("JOIN cl.classes clz1");

        this.addClassesStockClauses(joins, wheres, paramMap, stocks);

        wheres.add("cl.name=:cName");
        wheres.add("clz0.level=:lvl");
        wheres.add("clz1.name=:clzName");

        if (mostRecentVersionOnly) {
            wheres.add(DataSetDao.buildMostRecentVersionsOnlySubQuery("a", this.getEntityName(dataSetType), null, StringUtils.join(joins, " "),
                    wheres));
        }

        paramMap.put("lvl", level);
        paramMap.put("cName", classificationSystem);
        paramMap.put("clzName", classname);

        return selectDistinctClClassesAsc("clz0", dataSetType, joins, paramMap, wheres);
    }

    /**
     * Get the names of all category systems (classifications)
     *
     * @return names of all category systems (classifications)
     */
    @SuppressWarnings("unchecked")
    public List<String> getCategorySystemNames() {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery("SELECT DISTINCT c.name FROM Classification c", String.class);
        return q.getResultList();
    }

    /**
     * Get entity name from data set type
     *
     * @param type input data set type
     * @return name of entity for queries
     */
    private String getEntityName(DataSetType type) {
        if (DataSetType.PROCESS.equals(type)) {
            return "Process";
        }
        if (DataSetType.FLOW.equals(type)) {
            return "Flow";
        }
        if (DataSetType.FLOWPROPERTY.equals(type)) {
            return "FlowProperty";
        }
        if (DataSetType.CONTACT.equals(type)) {
            return "Contact";
        }
        if (DataSetType.LCIAMETHOD.equals(type)) {
            return "LCIAMethod";
        }
        if (DataSetType.SOURCE.equals(type)) {
            return "Source";
        }
        if (DataSetType.UNITGROUP.equals(type)) {
            return "UnitGroup";
        }
        return null;
    }

    /**
     * Uses a reliable query logic to build the query and perform the select (mitigating e.g. duplicate issues).
     *
     * @param selectEntityAlias This alias is used in the 'SELECT', 'GROUP BY' and 'ORDER BY' statements.
     *                          There's no default value, as it typically depends on joins.
     * @param dataSetType       Type of data set for which the ClClasses should be fethced (determines the join table)
     * @param joinClauses       List of join clauses, e.g. '{"LEFT JOIN a.classifications cl", "LEFT JOIN cl.classes clz0"}'
     * @param paramMap          Map that contains maps the referenced parameter keys (e.g. in where clauses) to associated objects.
     * @param whereClauses      List of where clauses, e.g. '{"name=:name", "type=:type"}'
     * @return Distinct classification-classes matching the criteria, ordered ascendingly.
     * Both distinction and order are based on the name field of the *selected* entity.
     */
    @SuppressWarnings("unchecked")
    private List<ClClass> selectDistinctClClassesAsc(@Nonnull String selectEntityAlias,
                                                     @Nonnull final DataSetType dataSetType, @Nonnull List<String> joinClauses,
                                                     @Nonnull Map<String, Object> paramMap, @Nonnull List<String> whereClauses) {
        if (selectEntityAlias.isBlank())
            throw new IllegalArgumentException("Method param 'SelectEntityAlias' can't be blank. (There's no default as it most likely depends on predetermined joins.)");
        final List<String> wheres = whereClauses.isEmpty() ? null : new ArrayList<>(whereClauses);
        final List<String> joins = joinClauses.isEmpty() ? null : new ArrayList<>(joinClauses);
        final Map<String, Object> params = paramMap.isEmpty() ? null : new HashMap<>(paramMap);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT max(").append(selectEntityAlias).append(".id)")
                .append(" FROM ").append(this.getEntityName(dataSetType)).append(" a ");

        if (joins != null)
            sb.append(String.join(" ", joins));
        if (wheres != null)
            sb.append(" WHERE ").append(String.join(" AND ", wheres));

        sb.append(" GROUP BY ").append(selectEntityAlias).append(".name")
                .append(" ORDER BY ").append(selectEntityAlias).append(".name ASC");

        if (LOGGER.isDebugEnabled()) // ')) ORDER BY t0.clName ASC'
            LOGGER.debug(sb.toString());

        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery(sb.toString(), Long.class);
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                q.setParameter(param.getKey(), param.getValue());
            }
        }

        Instant start = null;
        if (LOGGER.isTraceEnabled())
            start = Instant.now();

        List<Long> idsOfDistinctResults = q.getResultList();
        if (idsOfDistinctResults == null || idsOfDistinctResults.isEmpty())
            return Collections.EMPTY_LIST; // Nothing found!

        sb = new StringBuilder();
        String idSelectionKey = "idSelection";
        sb.append("SELECT clz from ").append(this.getEntityName(dataSetType)).append(" a")
                .append(" LEFT JOIN a.classifications cl LEFT JOIN cl.classes clz")
                .append(" WHERE clz.id IN :").append(idSelectionKey)
                .append(" ORDER BY clz.name ASC");
        paramMap.put(idSelectionKey, idsOfDistinctResults);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(sb.toString());

        q = em.createQuery(sb.toString(), ClClass.class);
        q.setParameter(idSelectionKey, idsOfDistinctResults);

        Instant startSecondQuery = null;
        if (LOGGER.isTraceEnabled())
            startSecondQuery = Instant.now();

        List<ClClass> result = q.getResultList();

        Instant queriesCompletedAt;
        if (LOGGER.isTraceEnabled() && start != null && startSecondQuery != null) {
            queriesCompletedAt = Instant.now();
            LOGGER.trace(String.format("Fetching classification classes took %1s millis (of which the second query took %2s millis)",
                    Duration.between(start, queriesCompletedAt).toMillis(),
                    Duration.between(startSecondQuery, queriesCompletedAt).toMillis()));
        }

        return result;
    }
}
