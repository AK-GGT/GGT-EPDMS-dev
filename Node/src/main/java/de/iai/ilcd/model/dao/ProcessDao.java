package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.process.ProcessDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.process.ReviewType;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.fzk.iai.ilcd.service.model.IProcessListVO;
import de.fzk.iai.ilcd.service.model.IProcessVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IReview;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.process.Exchange;
import de.iai.ilcd.model.process.LciaResult;
import de.iai.ilcd.model.process.LciaResultClClassStats;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.UnmarshalHelper;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data access object for {@link Process processes}
 */
public class ProcessDao extends DataSetDao<Process, IProcessListVO, IProcessVO> {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(ProcessDao.class);

    /**
     * Create the data access object for {@link Process processes}
     */
    public ProcessDao() {
        super("Process", Process.class, IProcessListVO.class, IProcessVO.class, DataSetType.PROCESS);
    }

    public static void addComplianceWhereClauses(String typeAlias, ValueParser params, List<String> whereClauses, Map<String, Object> whereParamValues) {
        String[] complianceParams = cleanParams(params.getStrings("compliance")); // Should contain UUIDs of the source data sets

        if (complianceParams.length > 0) {
            String complianceMode = params.getString("complianceMode");
            String complianceClause = generateComplianceWhereClauses(typeAlias, complianceParams, complianceMode, whereParamValues);
            if (complianceClause == null || complianceClause.trim().isEmpty())
                whereClauses.add("false");
            else
                whereClauses.add("(" + complianceClause + ")");
        }
    }

    /**
     * Generates a where clause for ComplianceSystems. As complianceParams it
     * expects an array of UUIDs (which typically point to source data sets).
     *
     * @param typeAlias
     * @param complianceParams
     * @param complianceMode
     * @param whereParamValues
     * @return
     */
    protected static String generateComplianceWhereClauses(String typeAlias, String[] complianceParams,
                                                           String complianceMode, Map<String, Object> whereParamValues) {
        if (complianceParams == null || complianceParams.length == 0 || whereParamValues == null)
            return "";

        List<String> params = Stream.of(complianceParams).collect(Collectors.toList());

        // Fetch all known compliance systems (Source data sets), keep the ones, that were referenced by
        // params.
        List<Source> referencedComplianceSystems = new ArrayList<>();
        try {
            SourceDao sourceDao = new SourceDao();
            List<Source> allComplianceSystems = sourceDao.getComplianceSystems();
            referencedComplianceSystems = allComplianceSystems.stream()
                    .filter(cs -> params.contains(cs.getUuidAsString())).collect(Collectors.toList());

            if (referencedComplianceSystems.size() == 0)
                return null;

            // If we couldn't produce all referenced ComplianceSystems, we want the
            // 'AND' clause to fail.. hence we add null once.
            if (params.size() > referencedComplianceSystems.size())
                referencedComplianceSystems.add(null); // An 'AND' query will end up returning 0 data sets.

        } catch (Exception e) {
            referencedComplianceSystems.add(null); // An 'AND' query will end up returning 0 data sets.
            e.printStackTrace();
        }

        // Adjust to query mode. Invalid query modes will lead to 'contains any'
        // behaviour. 'AND' is not supported so far.
        String conjunction = StringUtils.equalsIgnoreCase(complianceMode, "OR") ? " OR " : " OR ";
        String sqlOperator = "=";
        String sqlNegator = StringUtils.equalsIgnoreCase(complianceMode, "NOT") ? "NOT " : "";

        // Generate the clauses
        List<String> clauses = new ArrayList<String>();
        int n = referencedComplianceSystems.size();
        for (Source s : referencedComplianceSystems) {
            // generate suitably unique parameter for prepared Statement and store value in
            // whereParamValues
            String genParam = "complianceSourceParam" + n;
            whereParamValues.put(genParam, s.getUuid());

            // e.g. "(:complianceSourceParam0 = compliancesourceref.uuid)"
            String clause = "(" + sqlNegator + ":" + genParam + " " + sqlOperator + " compliancesourceref.uuid)";
            clauses.add(clause);
            n--;
        }
        return clauses.size() > 0 ? String.join(conjunction, clauses) : null;
    }

    protected static void addDatasourcesWhereClauses(String typeAlias, ValueParser params, List<String> whereClauses,
                                                     Map<String, Object> whereParamValues) {
        String[] datasources = params.getStrings("dataSource");
        if (datasources != null && datasources.length > 0) {
            String datasourceMode = params.getString("dataSourceMode");
            if (!"NOT".equalsIgnoreCase(datasourceMode))
                datasourceMode = "OR";

            if (datasourceMode.equals("OR")) {
                whereClauses.add(" datasource.uuid.uuid IN :datasource ");
                whereParamValues.put("datasource", Arrays.asList(datasources));
            } else if (datasourceMode.equals("NOT")) {
                // NOT: WHERE clause shall look like this
                // "(SELECT ds FROM a.dataSources ds WHERE ds.uuid.uuid='b497a91f-e14b-4b69-8f28-f50eb1576766' OR ds.uuid.uuid='28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a') NOT MEMBER OF a.dataSources "
                StringBuilder clause = new StringBuilder(" (SELECT dsource FROM " + typeAlias + ".dataSources dsource WHERE ");
                boolean first = true;
                int paramNo = 0;
                for (String ds : datasources) {
                    if (first) {
                        first = false;
                    } else {
                        clause.append(" OR ");
                    }
                    String dsParam = "dsParam" + Integer.toString(paramNo++);
                    clause.append("dsource.uuid.uuid=:" + dsParam);
                    whereParamValues.put(dsParam, ds);
                }
                clause.append(") NOT MEMBER OF " + typeAlias + ".dataSources");
                whereClauses.add(clause.toString());
            }
        }
    }

    protected static void addDataGeneratorWhereClauses(String typeAlias, ValueParser params, List<String> whereClauses,
                                                       Map<String, Object> whereParamValues) {
        String[] datasetgenerator = params.getStrings("datasetGenerator");
        if (datasetgenerator != null && datasetgenerator.length > 0) {
            String datasetGeneratorMode = params.getString("datasetGeneratorMode");
            if (!"NOT".equalsIgnoreCase(datasetGeneratorMode))
                datasetGeneratorMode = "OR";

            if (datasetGeneratorMode.equals("OR")) {
                whereClauses.add(" datasetGenerator.uuid.uuid IN :datasetgenerator ");
                whereParamValues.put("datasetgenerator", Arrays.asList(datasetgenerator));
            } else if (datasetGeneratorMode.equals("NOT")) {
                // NOT: WHERE clause shall look like this
                // "(SELECT ds FROM a.dataSources ds WHERE ds.uuid.uuid='b497a91f-e14b-4b69-8f28-f50eb1576766' OR ds.uuid.uuid='28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a') NOT MEMBER OF a.dataSources "
                StringBuilder clause = new StringBuilder(" (SELECT dgenerator FROM " + typeAlias + ".datasetGenerator dgenerator WHERE ");
                boolean first = true;
                int paramNo = 0;
                for (String ds : datasetgenerator) {
                    if (first) {
                        first = false;
                    } else {
                        clause.append(" OR ");
                    }
                    String dsParam = "dsParam" + Integer.toString(paramNo++);
                    clause.append("dgenerator.uuid.uuid=:" + dsParam);
                    whereParamValues.put(dsParam, ds);
                }
                clause.append(") NOT MEMBER OF " + typeAlias + ".datasetGenerator");
                whereClauses.add(clause.toString());
            }
        }
    }

    @Override
    protected String getDataStockField() {
        return DatasetTypes.PROCESSES.getValue();
    }

    public Process getFullProcess(String uuid) {
        Process process = this.getByUuid(uuid);
        if (process != null) {
            this.addFlowsToExchanges(process);
        }
        return process;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(Process dataSet) {
        this.addFlowsToExchanges(dataSet);
        if (ConfigurationService.INSTANCE.isQqaEnabled())
            this.updateTimestampForLciaResultCacheObjects(dataSet);
    }

    /**
     * Get the list of processes which have the provided direction and flow as
     * in- or output exchange flow
     *
     * @param flowUuid    uuid of flow
     * @param direction   direction of flow
     * @param firstResult start index
     * @param maxResults  maximum result items
     * @return list of processes which have the provided direction and flow as
     * in- or output exchange flow
     */
    @SuppressWarnings("unchecked")
    public List<Process> getProcessesForExchangeFlow(String flowUuid, ExchangeDirectionValues direction,
                                                     int firstResult, int maxResults) {
        Query q = this.getProcessesForExchangeFlowQuery(flowUuid, direction, false);
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);
        return q.getResultList();
    }

    /**
     * Get count of processes for provided exchange flow and direction
     *
     * @param flowUuid  uuid of flow
     * @param direction direction of flow
     * @return count of processes for provided exchange flow and direction
     */
    public long getProcessesForExchangeFlowCount(String flowUuid, ExchangeDirectionValues direction) {
        Query q = this.getProcessesForExchangeFlowQuery(flowUuid, direction, true);
        return (Long) q.getSingleResult();
    }

    /**
     * Get query for list or count of processes which have provided flow as in-
     * or output exchange flow
     *
     * @param flowUuid  uuid of flow to get processes for
     * @param direction direction of process (may be null)
     * @param count     flag to indicate if count query shall be created
     * @return query for list or count of processes which have provided flow as
     * in- or output exchange flow
     */
    private Query getProcessesForExchangeFlowQuery(String flowUuid, ExchangeDirectionValues direction, boolean count) {
        if (flowUuid == null) {
            throw new IllegalArgumentException("Flow must not be null!");
        }
        EntityManager em = PersistenceUtil.getEntityManager();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (count) {
            sb.append("COUNT(DISTINCT p)");
        } else {
            sb.append("DISTINCT p");
        }

        sb.append(" FROM Process p LEFT JOIN p.exchanges e WHERE e.flow.uuid.uuid=:uuid");

        if (direction != null)
            sb.append(" AND e.exchangeDirection=:dir");

        // we only want the latest versions
        sb.append(" AND ");

        List<String> wheres = new ArrayList<>();
        wheres.add("d.flow.uuid.uuid=:uuid");
        if (direction != null)
            wheres.add("d.exchangeDirection=:dir");
        sb.append(buildMostRecentVersionsOnlySubQuery("p", "Process", "", "LEFT JOIN p.exchanges d", wheres));

        Query q = em.createQuery(sb.toString());

        q.setParameter("uuid", flowUuid);
        if (direction != null)
            q.setParameter("dir", direction);

        return q;

    }

    @SuppressWarnings("unchecked")
    public List<GeographicalArea> getUsedLocations() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery(
                        "SELECT DISTINCT area FROM Process p, GeographicalArea area WHERE p.geography.location=area.areaCode ORDER BY area.name")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<GeographicalArea> getAllLocations() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("SELECT DISTINCT area FROM GeographicalArea area ORDER BY area.name").getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getReferenceYears() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery(
                        "SELECT DISTINCT p.timeInformation.referenceYear FROM Process p WHERE p.timeInformation.referenceYear IS NOT NULL ORDER BY p.timeInformation.referenceYear asc")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getValidUntilYears() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery(
                        "SELECT DISTINCT p.timeInformation.validUntil FROM Process p WHERE p.timeInformation.validUntil IS NOT NULL ORDER BY p.timeInformation.validUntil asc")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Tag> getTags() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery(
                        "SELECT tag FROM Process p, Tag t WHERE p.tag.process_ID=p.id ORDER BY p.id")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Deprecated // Makes no sense and never used.
    public List<GlobalReference> getPrecedingDataSetVersions() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery(
                        "SELECT globalreference FROM Process p, GlobalReference g WHERE p.precedingDataSetVersions.process_ID=p.id ORDER BY p.id")
                .getResultList();
    }

    public Process getSupersedingDataSetVersion(String uuid, List<IDataStockMetaData> stocks) {
        try {
            // let's extract the stock UUIDs to a simple set for easier handling
            HashSet<String> stockUuids = new HashSet<>();
            if (stocks != null)
                stockUuids.addAll(stocks.stream().map(IDataStockMetaData::getUuidAsString).collect(Collectors.toSet()));

            EntityManager em = PersistenceUtil.getEntityManager();
            Query q = em.createQuery("SELECT p FROM Process p JOIN p.precedingDataSetVersions pv WHERE pv.uuid.uuid=:uuid ORDER BY p.version DESC");
            q.setParameter("uuid", uuid);

            List<Process> result = q.getResultList();
            if (LOGGER.isTraceEnabled())
                LOGGER.trace(result.size() + " results found for superseding dataset");

            // if we don't care about datastock permissions, return the topmost hit
            if (!ConfigurationService.INSTANCE.isHonorPermissionsForSupersedingDatasets() && !result.isEmpty())
                return result.get(0);

            // otherwise, go through all results and return the first result where datastock matches
            for (Process p : result) {
                if (p.getUuidAsString().equals(uuid)) {
                    LOGGER.warn("The superseding dataset for " + uuid + " has the same UUID, which doesn't make sense, so we'll ignore it.");
                    return null;
                }
                if (stockUuids.contains(p.getRootDataStock().getUuidAsString()))
                    return p;
                for (DataStock ds : p.getContainingDataStocks()) {
                    if (stockUuids.contains(ds.getUuidAsString()))
                        return p;
                }
            }
            return null;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public List<Process> getSupersedingDataSetVersions(String uuid) {
        try {
            EntityManager em = PersistenceUtil.getEntityManager();
            Query q = em.createQuery("SELECT p FROM Process p JOIN p.precedingDataSetVersions pv WHERE pv.uuid.uuid=:uuid ORDER BY p.version DESC");
            q.setParameter("uuid", uuid);
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getLanguages() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createNativeQuery("SELECT DISTINCT n.lang FROM processname_base n, process p WHERE p.id=n.process_id ORDER BY n.lang ASC").getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> getLanguages(IDataStockMetaData... stocks) {
        if (stocks == null || stocks.length == 0) {
            return new ArrayList<String>();
        }

        List<String> lstRdsIds = new ArrayList<>();
        List<String> lstDsIds = new ArrayList<>();
        for (IDataStockMetaData m : stocks) {
            if (m.isRoot()) {
                lstRdsIds.add(Long.toString(m.getId()));
            } else {
                lstDsIds.add(Long.toString(m.getId()));
            }
        }

        StringBuilder sb = new StringBuilder("SELECT DISTINCT n.lang FROM processname_base n, process p ");

        final List<String> whereStmnts = new ArrayList<>();

        if (!lstRdsIds.isEmpty()) {
            whereStmnts.add("p.root_stock_id IN (" + this.join(lstRdsIds, ",") + ")");
        }
        if (!lstDsIds.isEmpty()) {
            sb.append(", datastock_process dp ");
            whereStmnts.add(" dp.processes_ID=p.ID AND dp.containingdatastocks_id in (" + this.join(lstDsIds, ",") + ")");
        }

        sb.append("WHERE p.id=n.process_id ");

        if (!whereStmnts.isEmpty()) {
            sb.append(" AND ");
            sb.append(StringUtils.join(whereStmnts, " AND "));
        }

        sb.append(" ORDER BY n.lang ASC");

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(sb.toString());

        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createNativeQuery(sb.toString()).getResultList();
    }

    /**
     * Get the reference years
     *
     * @param stocks stocks to get reference years for
     * @return loaded years. <b>Please note:</b> no stocks (<code>null</code> or
     * empty array) will return an empty list!
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getReferenceYears(IDataStockMetaData... stocks) {
        if (stocks == null || stocks.length == 0) {
            return new ArrayList<Integer>();
        }

        Map<String, String> xjw = allXBuildJoins(stocks);
        String join = xjw.get("JOIN");
        String wheres = xjw.get("WHERE");

        EntityManager em = PersistenceUtil.getEntityManager();
        return em
                .createQuery("select distinct p.timeInformation.referenceYear from Process p" + join + " WHERE "
                        + wheres + " ORDER BY p.timeInformation.referenceYear asc")
                .getResultList();
    }

    /**
     * Get the validUntil years
     *
     * @param stocks stocks to get validUntil years for
     * @return loaded years. <b>Please note:</b> no stocks (<code>null</code> or
     * empty array) will return an empty list!
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getValidUntilYears(IDataStockMetaData... stocks) {
        if (stocks == null || stocks.length == 0) {
            return new ArrayList<Integer>();
        }

        Map<String, String> xjw = allXBuildJoins(stocks);
        String join = xjw.get("JOIN");
        String wheres = xjw.get("WHERE");

        EntityManager em = PersistenceUtil.getEntityManager();
        return em
                .createQuery("select distinct p.timeInformation.validUntil from Process p" + join + " WHERE "
                        + wheres + " order by p.timeInformation.validUntil asc")
                .getResultList();
    }

    /**
     * Get the dataset generator UUIDs
     *
     * @param stocks stocks to get dataset generator contacts for
     * @return dataset generator contacts. <b>Please note:</b> no stocks (<code>null</code> or
     * empty array) will return an empty list!
     */
    @SuppressWarnings("unchecked")
    public List<IGlobalReference> getDatasetGenerators(IDataStockMetaData... stocks) {
        if (stocks == null || stocks.length == 0) {
            return new ArrayList<IGlobalReference>();
        }

        Map<String, String> xjw = allXBuildJoins(stocks);
        String join = xjw.get("JOIN");
        String wheres = xjw.get("WHERE");

        EntityManager em = PersistenceUtil.getEntityManager();
        List<GlobalReference> refs = em
                .createQuery("select distinct dsgenerator from Process p JOIN p.datasetGenerator dsgenerator " + join + " WHERE " + wheres)
                .getResultList();

        List<IGlobalReference> result = new ArrayList<IGlobalReference>();

        // TODO this is a workaround to dedupe the list until the issue with GlobalReference identity (and the
        //      resulting huge number of GlobalReferences in the DB table) has been solved
        for (GlobalReference ref : refs) {
            ref.setId(null);
            result.add(ref);
        }
        Set<IGlobalReference> depdupedResults = new LinkedHashSet<>(result);
        result.clear();
        result.addAll(depdupedResults);
        return result;
    }

    private Map<String, String> allXBuildJoins(IDataStockMetaData... stocks) {
        List<String> lstRdsIds = new ArrayList<String>();
        List<String> lstDsIds = new ArrayList<String>();

        for (IDataStockMetaData m : stocks) {
            if (m.isRoot()) {
                lstRdsIds.add(Long.toString(m.getId()));
            } else {
                lstDsIds.add(Long.toString(m.getId()));
            }
        }

        String join = "";
        String wheres = "";

        final List<String> whereStmnts = new ArrayList<String>();

        if (!lstRdsIds.isEmpty()) {
            whereStmnts.add("p.rootDataStock.id in (" + this.join(lstRdsIds, ",") + ")");
        }
        if (!lstDsIds.isEmpty()) {
            join = " LEFT JOIN p.containingDataStocks ds";
            whereStmnts.add("ds.id in (" + this.join(lstDsIds, ",") + ")");
        }

        wheres = StringUtils.join(whereStmnts, " OR ");

        Map<String, String> result = new HashMap<String, String>();

        result.put("JOIN", join);
        result.put("WHERE", wheres);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("type.value".equals(sortString) || "type".equals(sortString)) {
            return buildOrderBy(typeAlias, "type", typeAlias, "nameCache", sortOrder);
        } else if ("location".equals(sortString)) {
            return buildOrderBy(typeAlias, "geography.location", typeAlias, "nameCache", sortOrder);
        } else if ("referenceYear".equals(sortString) || "timeInformation.referenceYear".equals(sortString)) {
            return buildOrderBy(typeAlias, "timeInformation.referenceYear", typeAlias, "nameCache", sortOrder);
        } else if ("validUntil".equals(sortString) || "timeInformation.validUntil".equals(sortString)) {
            return buildOrderBy(typeAlias, "timeInformation.validUntil", typeAlias, "nameCache", sortOrder);
        } else if ("LCIMethodInformation.methodPrinciple.value".equals(sortString)) {
            return buildOrderBy(typeAlias, "lCIMethodInformation.methodPrinciple", typeAlias, "nameCache", sortOrder);
        } else if ("complianceSystems".equals(sortString) || "compliance".equals(sortString)) {
            return buildOrderBy(typeAlias, "complianceSystemCache", typeAlias, "nameCache", sortOrder);
        } else if ("subType".equals(sortString)) {
            return buildOrderBy(typeAlias, "subType", typeAlias, "nameCache", sortOrder);
        } else if ("owner".equals(sortString) || "ownerReference".equals(sortString)) {
            return buildOrderBy(null, "VALUE(ownerref)", typeAlias, "nameCache", sortOrder);
        } else if ("registrationNumber".equals(sortString)) {
            return buildOrderBy(typeAlias, "registrationNumber", typeAlias, "nameCache", sortOrder);
        } else if ("registrationAuthority".equals(sortString)) {
            return buildOrderBy(null, "VALUE(regAuthority)", typeAlias, "nameCache", sortOrder);
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringJoinPart(ValueParser params, String typeAlias, String sortCriterium) {
        boolean exchangeFlowQuery = !StringUtils.isBlank(params.getString("exchangeFlow"));
        if (exchangeFlowQuery) {
            return "LEFT JOIN " + typeAlias + ".exchanges " + typeAlias + "Ex";
        }

        boolean hasNameParam = !StringUtils.isBlank(params.getString("name"));
        boolean hasDescParam = !StringUtils.isBlank(params.getString("description"));
        boolean hasOwnerParam = !StringUtils.isBlank(params.getString("owner"))
                || "owner".equals(sortCriterium) || "ownerReference".equals(sortCriterium);
        boolean hasComplianceParam = !StringUtils.isBlank(params.getString("compliance"));
        boolean hasDataSourceParam = !StringUtils.isBlank(params.getString("dataSource"));
        boolean hasTagParam = !StringUtils.isBlank(params.getString("tag"));
        boolean hasDatasetGeneratorParam = !StringUtils.isBlank(params.getString("datasetGenerator"));
        boolean hasReferenceToRegistrationAuthorityParam = !StringUtils.isBlank(params.getString("registrationAuthority")) || "registrationAuthority".equals(sortCriterium);

        if (hasNameParam || hasDescParam || hasOwnerParam || hasComplianceParam || hasDataSourceParam || hasTagParam || hasDatasetGeneratorParam || hasReferenceToRegistrationAuthorityParam) {
            StringBuilder buf = new StringBuilder();
            if (hasNameParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".baseName bn LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".nameRoute nr LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".nameLocation nl LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".nameUnit nu LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".synonyms syn ");
            }
            if (hasDescParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".description des LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".useAdvice ua ");
            }
            if (hasOwnerParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".ownerReference.shortDescription ownerref ");
            }
            if (hasComplianceParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".complianceSystems compliancesystems ");
                buf.append("LEFT JOIN ");
                buf.append("compliancesystems.sourceReference compliancesourceref ");
            }
            if (hasDataSourceParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".dataSources datasource ");
            }
            if (hasTagParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".tags tag ");
            }
            if (hasDatasetGeneratorParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".datasetGenerator datasetgenerator ");
            }
            if (hasReferenceToRegistrationAuthorityParam) {
                buf.append("LEFT JOIN ");
                buf.append(typeAlias);
                buf.append(".referenceToRegistrationAuthority regAuthority ");
            }
            return buf.toString();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params,
                                                                    List<String> whereClauses, Map<String, Object> whereParamValues) {
        // time information stuff
        Integer referenceYear = this.parseInteger(params.getString("referenceYear"));
        String validUntilParam = params.getString("validUntil");
        Integer validUntil = null;
        boolean validUntilMatchModeExact = false;
        if (StringUtils.startsWith(validUntilParam, "=")) {
            validUntil = this.parseInteger(StringUtils.substring(validUntilParam, 1));
            validUntilMatchModeExact = true;
        } else
            validUntil = this.parseInteger(validUntilParam);

        if (referenceYear != null || validUntil != null) {
            if (referenceYear != null) {
                whereClauses.add(typeAlias + ".timeInformation.referenceYear = :referenceYear ");
                whereParamValues.put("referenceYear", referenceYear);
            }

            if (validUntil != null) {
                if (validUntilMatchModeExact)
                    whereClauses.add(typeAlias + ".timeInformation.validUntil = :validUntil ");
                else
                    whereClauses.add(typeAlias + ".timeInformation.validUntil >= :validUntil ");
                whereParamValues.put("validUntil", validUntil);
            }
        }

        // parameterized
        if (params.getString("parameterized") != null) {
            whereClauses.add(typeAlias + ".parameterized=:parameterized");
            whereParamValues.put("parameterized", Boolean.TRUE);
        }

        // exchange flows
        final boolean exchangeFlowQuery = params.getString("exchangeFlow") != null;
        final String exAlias = typeAlias + "Ex";

        if (exchangeFlowQuery) {
            whereClauses.add(exAlias + ".flow.uuid.uuid=:exFlowUuid");
            whereClauses.add(exAlias + ".exchangeDirection=:exFlowDir");

            whereParamValues.put("exFlowUuid", params.getString("exchangeFlow"));
            whereParamValues.put("exFlowDir", params.get("exchangeFlowDirection"));
        }

        // classification
        String[] categories = params.getStrings("classes");
        if (categories != null && categories.length > 0) {
            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            int paramNo = 0;
            for (String cat : categories) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" OR ");
                }
                String catParam = "classParam" + Integer.toString(paramNo++);
                sb.append(typeAlias + ".classificationCache LIKE :" + catParam);
                whereParamValues.put(catParam, "%" + cat + "%");
            }
            sb.append(")");
            whereClauses.add(sb.toString());
        }

        // type
        String[] typeParams = params.getStrings("type");
        String typeWhereClause = createTypeWhereClause(typeAlias, typeParams, whereParamValues);

        if (typeWhereClause != null && !typeWhereClause.trim().isEmpty())
            whereClauses.add("(" + typeWhereClause + ")");

        // subtypes
        String[] subTypeParams = params.getStrings("subType");
        String subTypeWhereClause = createSubTypeWhereClause(typeAlias, subTypeParams, whereParamValues);

        if (subTypeWhereClause != null && !subTypeWhereClause.trim().isEmpty())
            whereClauses.add("(" + subTypeWhereClause + ")");

        // Locations
        String[] locations = params.getStrings("location");
        if (locations != null && locations.length > 0) {
            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            int paramNo = 0;
            for (String loc : locations) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" OR ");
                }
                String locParam = "locParam" + Integer.toString(paramNo++);
                sb.append(typeAlias + ".geography.location=:" + locParam);
                whereParamValues.put(locParam, loc);
            }
            sb.append(")");
            whereClauses.add(sb.toString());
        }

        // owner
        String owner = params.getString("owner");
        if (StringUtils.isNotBlank(owner)) {
            whereClauses.add(" VALUE(ownerref) LIKE :owner");
            whereParamValues.put("owner", "%" + owner + "%");
        }

        // registration authority
        String registrationAuthorityKeyPhrase = params.exists("registrationAuthority") ? params.getString("registrationAuthority").trim() : "";
        if (!registrationAuthorityKeyPhrase.isEmpty()) {
            // feature is enabled.
            if (ConfigurationService.INSTANCE.isAugmentRegistrationAuthority()) {
                final var configMap = ConfigurationService.INSTANCE.getAugmentRegistrationAuthorityMap();
                final var stockClauses = new ArrayList<String>();
                final var explicitlyAugmentedStocks = configMap.keySet().stream().filter(Objects::nonNull).collect(Collectors.toSet());

                // handle explicit augmented RA.
                for (final var rootStockUuid : explicitlyAugmentedStocks) {
                    final var augmentationRule = configMap.get(rootStockUuid);
                    final var stockUuidValueKey = "param" + System.nanoTime();
                    if (isRegistrationAuthorityMatch(registrationAuthorityKeyPhrase, augmentationRule)) {
                        if (isNotOverride(augmentationRule)) {
                            final var regAuthorityParamKey = "raParam" + System.nanoTime();
                            stockClauses.add("(" + typeAlias + ".rootDataStock.uuid.uuid=:" + stockUuidValueKey + " AND " + "(regAuthority IS NULL OR regAuthority.uuid.uuid = :" + regAuthorityParamKey + "))");
                            whereParamValues.put(regAuthorityParamKey, registrationAuthorityKeyPhrase);
                        } else {
                            stockClauses.add("(" + typeAlias + ".rootDataStock.uuid.uuid=:" + stockUuidValueKey + ")");
                        }
                    } else { // mismatch
                        final var regAuthorityParamKey = "raParam" + System.nanoTime();
                        if (isNotOverride(augmentationRule)) {
                            stockClauses.add("(" + typeAlias + ".rootDataStock.uuid.uuid=:" + stockUuidValueKey + " AND " + "regAuthority.uuid.uuid = :" + regAuthorityParamKey + ")");
                        } else {
                            stockClauses.add("(" + typeAlias + ".rootDataStock.uuid.uuid=:" + stockUuidValueKey + " AND " + "(regAuthority.uuid.uuid = :" + regAuthorityParamKey + " AND " + "regAuthority.uuid.uuid != :" + regAuthorityParamKey + "))");
                        }
                        whereParamValues.put(regAuthorityParamKey, registrationAuthorityKeyPhrase);
                    }
                    whereParamValues.put(stockUuidValueKey, rootStockUuid);
                }

                // handle global augmented RA.
                if (configMap.containsKey(null)) {
                    final var globalConfigTriple = configMap.get(null);
                    final var globalClause = new ArrayList<String>();

                    if (!explicitlyAugmentedStocks.isEmpty()) {
                        globalClause.add(typeAlias + ".rootDataStock.uuid.uuid NOT IN :explicitlyAugmentedStocks");
                        whereParamValues.put("explicitlyAugmentedStocks", explicitlyAugmentedStocks);
                    } // else rule is applied to all stocks and we can drop the clause.

                    if (isRegistrationAuthorityMatch(registrationAuthorityKeyPhrase, globalConfigTriple)) { // match.
                        if (isNotOverride(globalConfigTriple)) {
                            final var regAuthorityParamKey = "raParam" + System.nanoTime();
                            globalClause.add("(regAuthority IS NULL OR regAuthority.uuid.uuid = :" + regAuthorityParamKey + ")");
                            whereParamValues.put(regAuthorityParamKey, registrationAuthorityKeyPhrase);
                        }
                    } else { // mismatch.
                        final var regAuthorityParamKey = "raParam" + System.nanoTime();
                        if (isNotOverride(globalConfigTriple)) {
                            globalClause.add("(regAuthority.uuid.uuid = :" + regAuthorityParamKey + ")");
                        } else {
                            globalClause.add("(regAuthority.uuid.uuid = :" + regAuthorityParamKey + " AND " + "regAuthority.uuid.uuid != :" + regAuthorityParamKey + ")");
                        }
                        whereParamValues.put(regAuthorityParamKey, registrationAuthorityKeyPhrase);
                    }

                    if (!globalClause.isEmpty()) {
                        stockClauses.add("(" + StringUtils.join(globalClause, " AND ") + ")");
                    }

                } else { // handle complement set of augmented stocks.
                    final var paramValueKey = "raParam" + System.nanoTime();
                    final var raInComplementsClause = "(regAuthority.uuid.uuid = :" + paramValueKey + " AND " + typeAlias + ".rootDataStock.uuid.uuid NOT IN :explicitlyAugmentedStocksKey)";
                    whereParamValues.put(paramValueKey, registrationAuthorityKeyPhrase);
                    whereParamValues.put("explicitlyAugmentedStocksKey", explicitlyAugmentedStocks);
                    stockClauses.add(raInComplementsClause);
                }

                // concatenate each statement with 'or' and add it to where clause.
                if (!stockClauses.isEmpty()) {
                    whereClauses.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
                }
            } else {
                // feature is disabled.
                StringBuilder sb = new StringBuilder();
                // if it's a UUID, match it against the corresponding property
                sb.append(typeAlias + ".referenceToRegistrationAuthority.uuid.uuid = :regAuthUuid");
                whereParamValues.put("regAuthUuid", registrationAuthorityKeyPhrase);
                whereClauses.add(sb.toString());
            }
        }


        //registration number
        String registrationNumberUserInput = params.getString("registrationNumber");
        if (StringUtils.isNotBlank(registrationNumberUserInput)) {
            String multiContainClause = splitMultiContainClause(typeAlias, "registrationNumber", registrationNumberUserInput, "AND", whereParamValues);
            whereClauses.add(multiContainClause);
        }

        // datasource
        addDatasourcesWhereClauses(typeAlias, params, whereClauses, whereParamValues);

        // datagenerator
        addDataGeneratorWhereClauses(typeAlias, params, whereClauses, whereParamValues);

        // compliance
        addComplianceWhereClauses(typeAlias, params, whereClauses, whereParamValues);

        // metadata only
        if (params.getString("metaDataOnly") != null) {
            whereClauses.add(typeAlias + ".metaDataOnly=:metaDataOnly");
            whereParamValues.put("metaDataOnly", params.getBoolean("metaDataOnly", false));
        }

        // registries
        String registeredIn = params.getString("registeredIn");
        if (registeredIn != null && !registeredIn.equals("-1")) {
            whereClauses.add(
                    "EXISTS (SELECT dsrd FROM DataSetRegistrationData dsrd WHERE dsrd.registry.uuid = :registeredIn "
                            + "AND dsrd.uuid = " + typeAlias + ".uuid.uuid " + "AND dsrd.version = " + typeAlias
                            + ".version "
                            + "AND dsrd.status =  de.iai.ilcd.model.registry.DataSetRegistrationDataStatus.ACCEPTED"
                            + ")");
            whereParamValues.put("registeredIn", registeredIn);
        } else if (registeredIn != null) {
            whereClauses
                    .add("EXISTS (SELECT dsrd FROM GLADRegistrationData dsrd WHERE dsrd.uuid = " + typeAlias + ".uuid.uuid "
                            + "AND dsrd.version = " + typeAlias + ".version" + ")");
        }

        // Tags
        String[] tagParams = cleanParams(params.getStrings("tag"));

        if (tagParams.length > 0) {
            String tagMode = params.getString("tagmode");

            String tagClause = generateTagClause(typeAlias, tagParams, tagMode, whereParamValues);
            if (tagClause != null && !tagClause.trim().isEmpty())
                whereClauses.add("(" + tagClause + ")");
        }
    }

    private boolean isNotOverride(String[] triple) {
        return !(List.of(triple).contains("override") || List.of(triple).contains("overwrite"));
    }

    private boolean isRegistrationAuthorityMatch(String registrationAuthorityKeyPhrase, String[] configTriple) {
        return configTriple[0].equalsIgnoreCase(registrationAuthorityKeyPhrase);
    }

    private String createSubTypeWhereClause(String typeAlias, String[] subTypeParams,
                                            Map<String, Object> whereParamValues) {
        if (subTypeParams == null || subTypeParams.length == 0 || whereParamValues == null)
            return null;

        // Parse types from params
        List<ProcessSubType> selectedSubTypesList = Stream.of(subTypeParams).map(s -> {
            try {
                return ProcessSubType.valueOf(s);
            } catch (IllegalArgumentException iae) {
                return null; // Will be filtered out
            }
        }).filter(st -> st != null).collect(Collectors.toList());

        // Return null iff none of the params where successfully parsed
        if (selectedSubTypesList == null || selectedSubTypesList.isEmpty())
            return null;

        // Create list of subclauses
        String conjunction = " OR ";
        String sqlOperator = "=";

        List<String> clauses = new ArrayList<String>();
        int n = 0;
        for (ProcessSubType st : selectedSubTypesList) {
            // Generate suitably unique parameter key
            String genParamKey = "subTypeParam" + n;
            whereParamValues.put(genParamKey, st);
            n++;

            // e.g. "(a.subType =: subTypeParam0)"
            String clause = "(" + typeAlias + ".subType " + sqlOperator + ":" + genParamKey + ")";
            clauses.add(clause);
        }

        return String.join(conjunction, clauses);
    }

    private String createTypeWhereClause(String typeAlias, String[] typeParams, Map<String, Object> whereParamValues) {
        if (typeParams == null || typeParams.length == 0 || whereParamValues == null)
            return null;

        // Parse types from params
        List<TypeOfProcessValue> selectedTypesList = Stream.of(typeParams).map(s -> {
            try {
                return TypeOfProcessValue.valueOf(s);
            } catch (IllegalArgumentException iae) {
                return null; // Will be filtered out.
            }
        }).filter(t -> t != null).collect(Collectors.toList());

        // Return null iff none of the params where successfully parsed
        if (selectedTypesList == null || selectedTypesList.isEmpty())
            return null;

        // Create list of subclauses
        String conjunction = " OR ";
        String sqlOperator = "=";

        List<String> clauses = new ArrayList<String>();
        int n = 0;
        for (TypeOfProcessValue t : selectedTypesList) {
            // Generate suitably unique parameter key
            String genParamKey = "typeParam" + n;
            whereParamValues.put(genParamKey, t);
            n++;

            // e.g. "(a.type =: typeParam0)"
            String clause = "(" + typeAlias + ".type " + sqlOperator + ":" + genParamKey + ")";
            clauses.add(clause);
        }

        return String.join(conjunction, clauses);
    }

    /**
     * Generates 'where clause' for a given set of tags (given by <code>String[]</code> of names).
     *
     * @param typeAlias
     * @param tagParams        tag names
     * @param tagMode          default: 'containsAll', other options: 'containsAny', 'containsNone'
     * @param whereParamValues must not be null.
     * @return
     */
    private String generateTagClause(String typeAlias, String[] tagParams, String tagMode,
                                     Map<String, Object> whereParamValues) {
        int n = tagParams != null ? tagParams.length : 0;
        if (n == 0 || whereParamValues == null)
            return "";

        // Fetch tag objects for the given name parameters
        List<Tag> tagList = new ArrayList<Tag>();
        TagDao tagDao = new TagDao();
        for (int i = 0; i < n; i++) {
            Tag tag = tagDao.getTagByName(tagParams[i]);
            tagList.add(tagDao.getTagByName(tagParams[i]));
        }

        // Adjust to query mode. Invalid queries will lead to containsAll behaviour
        String conjunction = StringUtils.equalsIgnoreCase(tagMode, "containsAny") ? " OR " : " AND ";
        String sqlOperator = StringUtils.equalsIgnoreCase(tagMode, "containsNone") ? "NOT MEMBER OF" : "MEMBER OF";

        List<String> clauses = new ArrayList<String>();
        for (Tag tag : tagList) {
            // generate suitably unique parameter for prepared Statement and store value in
            // whereParamValues
            String genParam = "tagParam" + n;
            whereParamValues.put(genParam, tag);

            // e.g. "(:tagParam0 MEMBER OF p.tags)"
            String clause = "( :" + genParam + " " + sqlOperator + " " + typeAlias + ".tags)";
            clauses.add(clause);
            n--;
        }
        return clauses.size() > 0 ? String.join(conjunction, clauses) : "";
    }

    /**
     * Override default behavior for name and description filter in order to use
     * { Process#getProcessName()} instead of {@link Process#getName()}.
     */
    @Override
    protected void addNameDescWhereClauseAndNamedParamForQueryStringJpql(String typeAlias, ValueParser params,
                                                                         List<String> whereClauses, Map<String, Object> whereParamValues) {

        // name and description
        final String namePhrase = params.getString("name");
        final String descriptionPhrase = params.getString("description");

        boolean hasNameParam = !StringUtils.isBlank(namePhrase);
        boolean hasDescParam = !StringUtils.isBlank(descriptionPhrase) && descriptionPhrase.length() > 2;
        if (hasNameParam || hasDescParam) {
            final String uuidPhrase = StringUtils.substringAfter(StringUtils.lowerCase(namePhrase), "uuid:").strip();
            boolean hasUUIDParam = !StringUtils.isBlank(uuidPhrase);
            StringBuilder sb = new StringBuilder("(");
            if (hasUUIDParam) {
                sb.append(typeAlias + ".uuid.uuid like :uuidPhrase ");
                whereParamValues.put("uuidPhrase", uuidPhrase + "%");
            } else if (hasNameParam) {
                String[] fields = new String[]{"bn", "nr", "nl", "nu", "syn"};
                sb.append(splitMultiContainClause(fields, "OR", namePhrase, "AND", whereParamValues));
            }
            if (hasDescParam) {
                if (hasNameParam) {
                    sb.append(" OR ");
                }
                sb.append("des LIKE :descriptionPhrase OR ");
                sb.append("ua LIKE :descriptionPhrase");
                whereParamValues.put("descriptionPhrase", "%" + descriptionPhrase + "%");
            }
            sb.append(")");
            whereClauses.add(sb.toString());
        }

    }

    /**
     * Parse integer and return <code>null</code> on arbitrary error
     *
     * @param s string to parse
     * @return parsed integer or <code>null</code>
     */
    private Integer parseInteger(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get a foreign process
     *
     * @param nodeShortName short name of the node
     * @param uuid          UUID of process
     * @return loaded process
     */
    public IProcessVO getForeignProcess(String nodeShortName, String uuid, Long registryId) {
        ForeignDataSetsHelper foreignHelper = new ForeignDataSetsHelper(this);
        return foreignHelper.getForeignDataSet(IProcessVO.class, nodeShortName, uuid, registryId);
    }

    /**
     * Add flows references to the exchanges. This means that references to
     * {@link Exchange#getFlow() data base flows} shall be established where
     * only {@link Exchange#getFlowReference() global references} are set.
     *
     * @param process process to find flows for
     */
    public void addFlowsToExchanges(Process process) {
        ElementaryFlowDao eFlowDao = new ElementaryFlowDao();
        ProductFlowDao pFlowDao = new ProductFlowDao();

        for (Exchange exchange : process.getExchanges()) {

            if (exchange.getFlow() != null) {
                continue; // flow already associated
            }
            if (exchange.getFlowReference() == null) {
                continue;
            }
            GlobalReference flowReference = exchange.getFlowReference();

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("processing reference " + flowReference.getUuid().getUuid() + " version " + flowReference.getVersionAsString());

            String flowUuid = null;
            if (flowReference.getUuid() == null) {
                // crap, no uuid attribute in flow reference
                // but we can try to get it from the uri attribute if there
                String uri = flowReference.getUri();
                if (uri == null) {
                    continue; // sorry we can't get uuid
                }
                String[] splittedUri = uri.split("_");
                if (splittedUri.length <= 2) {
                    continue; // uri does not contain enough parts: so no uuid
                }
                flowUuid = splittedUri[splittedUri.length - 2];
            } else {
                flowUuid = exchange.getFlowReference().getUuid().getUuid();
            }
            Flow flow = null;
            try {
                if (StringUtils.isNotBlank(flowReference.getVersionAsString()))
                    flow = pFlowDao.getByUuidAndVersion(flowUuid, flowReference.getVersion());
                else
                    flow = pFlowDao.getByUuid(flowUuid);

                if (LOGGER.isDebugEnabled()) {
                    try {
                        LOGGER.debug(flow.getUuidAsString() + " " + flow.getDataSetVersion());
                    } catch (NullPointerException e) {
                    }
                }

                if (flow != null) {
                    exchange.setFlow(flow);
                } else {
                    exchange.setFlow(eFlowDao.getByUuid(flowUuid));
                }
            } catch (NoResultException ex) {
                // OK, we have no associated flow, so do nothing
            }
        }
    }

    /**
     * Update the time stamps for the {@link LciaResultClClassStats} cache
     * objects
     *
     * @param p the process to update {@link LciaResultClClassStats} for
     */
    private void updateTimestampForLciaResultCacheObjects(Process p) {
        if (p != null && CollectionUtils.isNotEmpty(p.getLciaResults())) {
            List<String> methodUuids;
            List<Exchange> referenceExchanges = p.getReferenceExchanges();
            methodUuids = new ArrayList<String>();
            for (LciaResult r : p.getLciaResults()) {
                String methodUuid = null;
                try {
                    methodUuid = r.getMethodReference().getUuid().getUuid();
                } catch (NullPointerException e1) {
                }
                if (methodUuid != null && !methodUuids.contains(methodUuid)) {
                    methodUuids.add(methodUuid);
                }
            }

            if (CollectionUtils.isNotEmpty(methodUuids)) {
                LciaResultClClassStatsDao statsDao = new LciaResultClClassStatsDao();
                long now = System.currentTimeMillis();
                try {
                    for (String methodUuid : methodUuids) {
                        for (Exchange exchange : referenceExchanges) {
                            String referenceFlowpropertyUuid = exchange.getFlowWithSoftReference().getReferenceFlowProperty().getFlowPropertyRef().getUuid().getUuid();
                            List<LciaResultClClassStats> statsList = statsDao.getNotDirty(methodUuid, referenceFlowpropertyUuid);
                            for (LciaResultClClassStats statsObj : statsList) {
                                statsObj.setTsLastChange(now);
                            }
                            statsDao.merge(statsList);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while updating time stamps of statistics cache objects", e);
                }
            }

        }
    }

    /**
     * {@inheritDoc} <br />
     * Plus: update time stamps for LCIA result cache objects
     */
    @Override
    public Collection<Process> remove(Collection<Process> objs) throws Exception {
        Collection<Process> tmp = super.remove(objs);
        if (ConfigurationService.INSTANCE.isQqaEnabled()) {
            for (Process p : tmp) {
                this.updateTimestampForLciaResultCacheObjects(p);
            }
        }
        return tmp;
    }

    /**
     * {@inheritDoc} <br />
     * Plus: update time stamps for LCIA result cache objects
     */
    @Override
    public Process remove(Process dataSet) throws DeleteDataSetException {
        Process p = super.remove(dataSet);
        if (ConfigurationService.INSTANCE.isQqaEnabled())
            this.updateTimestampForLciaResultCacheObjects(dataSet);
        return p;
    }

    /**
     * {@inheritDoc} <br />
     * Plus: update time stamps for LCIA result cache objects
     */
    @Override
    public Process merge(Process obj) throws MergeException {
        Process p = super.merge(obj);
        if (ConfigurationService.INSTANCE.isQqaEnabled())
            this.updateTimestampForLciaResultCacheObjects(obj);
        return p;
    }

    /**
     * {@inheritDoc} <br />
     * Plus: update time stamps for LCIA result cache objects
     */
    @Override
    public Collection<Process> merge(Collection<Process> objs) throws MergeException {
        Collection<Process> tmp = super.merge(objs);
        if (ConfigurationService.INSTANCE.isQqaEnabled()) {
            for (Process p : tmp) {
                this.updateTimestampForLciaResultCacheObjects(p);
            }
        }
        return tmp;
    }


    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode) {
        return getDependencies(dataSet, depMode, null);
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        Set<DataSet> dependencies = new HashSet<>();
        if (DependenciesMode.NONE.equals(depMode))
            return dependencies; // empty set

        Process process = (Process) dataSet;
        RootDataStock stock = null;
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        // in these two modes, we're only looking within the same data stock as
        // the originating dataset
        if (DependenciesMode.ALL_FROM_DATASTOCK.equals(depMode) || DependenciesMode.REFERENCE_FLOWS.equals(depMode))
            stock = process.getRootDataStock();

        // reference flow(s)
        List<Exchange> flows = DependenciesMode.REFERENCE_FLOWS.equals(depMode) ? process.getReferenceExchanges()
                : process.getExchanges();
        for (Exchange e : flows) {
            Flow flowWithSoftReference = e.getFlowWithSoftReference();
            GlobalReference flowReference = e.getFlowReference();

            boolean ignore = false;
            if (flowWithSoftReference != null)
                ignore = ignoreListFinal.contains(flowWithSoftReference.getUuidAsString());
            else if (flowReference != null)
                ignore = ignoreListFinal.contains(flowReference.getRefObjectId());
            else
                ignore = true;

            if (!ignore)
                addDependency(flowWithSoftReference, flowReference, stock, dependencies);
        }

        // if we only want the ref flows, that's it
        if (DependenciesMode.REFERENCE_FLOWS.equals(depMode))
            return dependencies;

        // reviewers
        for (IReview review : process.getReviews())
            for (IGlobalReference ref : review.getReferencesToReviewers())
                if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    addDependency(null, ref, stock, dependencies);

        // compliance systems
        for (IComplianceSystem compliance : process.getComplianceSystems()) {
            IGlobalReference complianceRef = compliance.getReference();
            if (complianceRef != null && !ignoreListFinal.contains(complianceRef.getRefObjectId()))
                addDependency(null, complianceRef, stock, dependencies);
        }

        // approvedBy
        try {
            GlobalReference ref = process.getApprovedBy();
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(null, ref, stock, dependencies);
        } catch (NullPointerException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getApprovedBy is null");
        }

        // owner
        try {
            GlobalReference ref = process.getOwnerReference();
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(null, ref, stock, dependencies);
        } catch (NullPointerException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getOwnerReference is null");
        }

        // datasources
        try {
            List<IGlobalReference> refs = process.getDataSources().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(null, refs, stock, dependencies);
        } catch (NullPointerException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getReferenceToDataSource is null");
        }

        // datagenerator
        try {
            List<IGlobalReference> refs = process.getDatasetGeneratorAsList().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(null, refs, stock, dependencies);
        } catch (NullPointerException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getReferenceToDataSource is null");
        }

        // original epd
        try {
            List<IGlobalReference> refs = process.getReferenceToOriginalEPD().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(null, refs, stock, dependencies);
        } catch (NullPointerException npe) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getReferenceToOriginalEPD is null");
        }

        // reference to publisher
        try {
            List<IGlobalReference> refs = process.getReferenceToPublisher().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(null, refs, stock, dependencies);
        } catch (NullPointerException npe) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getReferenceToPublisher is null");
        }

        // registration authority
        try {
            IGlobalReference ref = process.getRegistrationAuthority();
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(null, process.getRegistrationAuthority(), stock, dependencies);
        } catch (NullPointerException npe) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getRegistrationAuthority is null");
        }


        // all others are a lot more expensive to retrieve
        de.fzk.iai.ilcd.api.app.process.ProcessDataSet xmlDataset;

        GlobalReferenceType tmp;
        List<GlobalReferenceType> tmpList;
        try {
            xmlDataset = (ProcessDataSet) new UnmarshalHelper()
                    .unmarshal(process);

        } catch (NullPointerException npe) {
            LOGGER.error("Couldn't unmarshal/find underlying xml data set for process "
                    + process.getUuidAsString(), npe);
            xmlDataset = null;
        }

        if (xmlDataset != null) {
            // complementing processes
            try {
                tmpList = xmlDataset.getProcessInformation().getDataSetInformation().getComplementingProcesses()
                        .getReferenceToComplementingProcess()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToComplementingProcess is null");
            }

            try {
                tmpList = xmlDataset.getProcessInformation().getDataSetInformation().getReferenceToExternalDocumentation()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToExternalDocumentation is null");
            }

            try {
                tmpList = xmlDataset.getProcessInformation().getTechnology().getReferenceToIncludedProcesses()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToIncludedProcesses is null");
            }

            try {
                tmp = xmlDataset.getProcessInformation().getTechnology().getReferenceToTechnologyPictogramme();
                if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                    addDependency(tmp, stock, dependencies);

            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToTechnologyPictogramme is null");
            }

            try {
                tmpList = xmlDataset.getProcessInformation().getTechnology().getReferenceToTechnologyFlowDiagrammOrPicture()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToTechnologyFlowDiagrammOrPicture is null");
            }

            try {
                tmpList = xmlDataset.getModellingAndValidation().getLCIMethodAndAllocation().getReferenceToLCAMethodDetails()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToLCAMethodDetails is null");
            }

            try {
                tmpList = xmlDataset.getModellingAndValidation().getDataSourcesTreatmentAndRepresentativeness()
                        .getReferenceToDataHandlingPrinciples()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToDataHandlingPrinciples is null");
            }

            try {
                for (ReviewType review : xmlDataset.getModellingAndValidation().getValidation().getReview()) {
                    try {
                        tmp = review.getReferenceToCompleteReviewReport();
                        if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                            addDependency(tmp, stock, dependencies);
                    } catch (Exception e) {
                    }
                }
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("review.getReferenceToCompleteReviewReport is null");
            }

            try {
                tmpList = xmlDataset.getAdministrativeInformation().getCommissionerAndGoal().getReferenceToCommissioner()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToCommissioner is null");
            }

            try {
                tmpList = xmlDataset.getAdministrativeInformation().getDataEntryBy().getReferenceToDataSetFormat()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToDataSetFormat is null");
            }

            try {
                tmp = xmlDataset.getAdministrativeInformation().getDataEntryBy()
                        .getReferenceToConvertedOriginalDataSetFrom();
                if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                    addDependency(tmp, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToConvertedOriginalDataSetFrom is null");
            }

            try {
                tmp = xmlDataset.getAdministrativeInformation().getDataEntryBy()
                        .getReferenceToPersonOrEntityEnteringTheData();
                if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                    addDependency(tmp, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToPersonOrEntityEnteringTheData is null");
            }

            try {
                tmp = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                        .getReferenceToUnchangedRepublication();
                if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                    addDependency(tmp, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToUnchangedRepublication is null");
            }

            try {
                tmpList = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                        .getReferenceToEntitiesWithExclusiveAccess()
                        .stream()
                        .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                        .collect(Collectors.toList());

                addDependencies(tmpList, stock, dependencies);
            } catch (NullPointerException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("getReferenceToEntitiesWithExclusiveAccess is null");
            }
        }

        for (Exchange e : process.getExchanges()) {
            try {
                IGlobalReference ref = e.getRefToDataSource();
                if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    addDependency(null, ref, stock, dependencies);
            } catch (NullPointerException n) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("exchange.getRefToDataSource is null");
            }
        }

        return dependencies;
    }

    public long getCountImportedAfter(IDataStockMetaData[] stocks, IDataStockMetaData excludeStock, Instant instant) {

        EntityManager em = PersistenceUtil.getEntityManager();

        final String typeAlias = "a";
        final String typeName = this.getJpaName();

        final String excludeStockName = excludeStock != null ? excludeStock.getName() : null;

        List<IDataStockMetaData> searchStocks = new ArrayList<IDataStockMetaData>();
        for (IDataStockMetaData dsm : stocks) {
            if (!dsm.getName().equals(excludeStockName)) {
                searchStocks.add(dsm);
            }
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT COUNT(DISTINCT " + typeAlias + ") FROM " + typeName + " " + typeAlias + " ");

        String defaultJoins = super.getDefaultQueryStringJoinPart(new ValueParser(), typeAlias);

        String joins = this.getQueryStringJoinPart(new ValueParser(), typeAlias, null);
        if (joins == null) {
            joins = "";
        }

        List<String> whereClauses = new ArrayList<String>();
        Map<String, Object> whereParamValues = new LinkedHashMap<String, Object>();

        boolean dsJoinDone = false;
        if (searchStocks != null) {
            List<String> stockClauses = new ArrayList<String>();
            List<String> rootStockClauses = new ArrayList<String>();
            List<Long> dsIds = new ArrayList<Long>();

            for (IDataStockMetaData m : searchStocks) {
                // root data stock
                if (m.isRoot()) {
                    String paramName = "rootDsId" + Long.toString(m.getId());
                    rootStockClauses.add(typeAlias + ".rootDataStock.id=:" + paramName);
                    whereParamValues.put(paramName, m.getId());
                }
                // non root data stock
                else {
                    if (!dsJoinDone) {
                        joins += " LEFT JOIN " + typeAlias + ".containingDataStocks " + typeAlias + "ds";
                        dsJoinDone = true;
                    }
                    dsIds.add(m.getId());
                }
            }
            if (!rootStockClauses.isEmpty()) {
                stockClauses.add("(" + this.join(rootStockClauses, " OR ") + ")");
            }
            if (dsJoinDone && !dsIds.isEmpty()) {
                stockClauses.add(typeAlias + "ds.id IN(" + StringUtils.join(dsIds, ',') + ")");
            }
            if (!stockClauses.isEmpty()) {
                whereClauses.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
            }
        }
        if (excludeStock != null) {
            String paramName = "exclDsId" + Long.toString(excludeStock.getId());

            if (excludeStock.isRoot()) {
                whereClauses.add(typeAlias + ".rootDataStock.id!=:" + paramName);
                whereParamValues.put(paramName, paramName);
            } else {
                if (!dsJoinDone) {
                    joins += " LEFT JOIN " + typeAlias + ".containingDataStocks " + typeAlias + "ds";
                    dsJoinDone = true;
                }
                DataStock excludeDS = (DataStock) new CommonDataStockDao().getDataStockById(excludeStock.getId());
                whereClauses.add("(" + typeAlias + "ds.id IS NULL OR :" + paramName + " NOT MEMBER OF " + typeAlias + ".containingDataStocks)");
                whereParamValues.put(paramName, excludeDS);
            }
        }

        if (!StringUtils.isBlank(joins)) {
            queryString.append(" ").append(joins.trim()).append(" ");
        }

        whereClauses.add(typeAlias + ".importDate > :importDate");
        whereParamValues.put("importDate", Timestamp.from(instant));

        whereClauses.add(buildMostRecentVersionsOnlySubQuery(typeAlias, this.getJpaName(), defaultJoins, joins, whereClauses));

        if (!whereClauses.isEmpty()) {
            queryString.append("WHERE ");
            queryString.append(this.join(whereClauses, " AND "));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("retrieving count for datasets newer than " + instant);
            LOGGER.debug(queryString.toString());
        }

        Query query = em.createQuery(queryString.toString());
        for (Entry<String, Object> e : whereParamValues.entrySet()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(e.getKey() + ": " + e.getValue());
            }
            query.setParameter(e.getKey(), e.getValue());
        }

        Long resultCount = (Long) query.getSingleResult();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("returning count " + resultCount);

        return resultCount.longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Contact> getAllRegistrationAuthorities() {
        EntityManager em = PersistenceUtil.getEntityManager();

        /* Check Augmentation rules */
        @Nonnull final var augmentationRulesByStockUuid = Objects.requireNonNull(ConfigurationService.INSTANCE.getAugmentRegistrationAuthorityMap());
        final var isAugmentationEnabled = !augmentationRulesByStockUuid.isEmpty();

        @Nonnull final var setOfReferencedContactsUuids = new HashSet<String>();// We want to fetch the uuids of the contact datasets which are referenced as registration authorities. (Versions are ignored because we will only return the most recent ones present in the database....)
        @Nonnull final var setContactOfReferences = new HashSet<GlobalReference>(); // We may need to look ab some short names later on...
        if (!isAugmentationEnabled) {
            final var q = em.createQuery( // We simply fetch all non-null global references' refObjectId (=uuid property) values. These can be resolved to contacts with most recent versions.
                    "SELECT DISTINCT p.referenceToRegistrationAuthority FROM Process p"); // The implicit join excludes all entities where referenceToRegistrationAuthority is null.

            final var results = q.getResultList();
            if (results != null) {
                setContactOfReferences.addAll(results);
                setContactOfReferences.remove(null);
                setOfReferencedContactsUuids.addAll(setContactOfReferences.stream()
                        .filter(Objects::nonNull)
                        .map(GlobalReference::getRefObjectId)
                        .collect(Collectors.toList()));
            }

        } else { // We've got to apply augmentation rules to the uuids before collection them.
            final var queryForStockRegAuthTuples = em.createQuery( // We collect all ({stock}, {registrationAuthority}) tuples. Augmentation rules are configured on the root stock level -- Hence, we want to know which contacts are referenced on which stocks.
                    "SELECT p.rootDataStock.uuid, p.referenceToRegistrationAuthority FROM Process p"); // TODO We should be able to use 'SELECT DISTINCT'. In some test cases, however, it leads to strange results. Investigation is potentially non-trivial and time-consuming. Note: The differences become apparent when you group the global references by the root stock uuid. The resulting maps should at least have the same number of entries (=root stocks for which we have information) - but they don't. Best guess: 'SELECT DISTINCT p.rootDataStock.uuid, regAuth FROM Process p LEFT OUTER JOIN p.referenceToRegistrationAuthority regAuth' seems to believe that (stock1, NULL) == (stock2, NULL) are to be considered the same tuple (which is probably a bad guess, though).

            final List<Object[]> results = queryForStockRegAuthTuples.getResultList();
            @Nonnull final var stockRegAuthTuples = new ArrayList<Object[]>(); // tuple structure: ( {uuid, non-null: root-stock-uuid}, {global reference, nullable: reference-to-registraition-authority} )
            if (results != null) {
                stockRegAuthTuples.addAll(results);
                stockRegAuthTuples.remove(null);
                setContactOfReferences.addAll(stockRegAuthTuples.stream()
                        .map(objArr -> (GlobalReference) objArr[1])
                        .filter(Objects::nonNull).
                        collect(Collectors.toList()));
            }

            /* Group registration authority references by root stock (augmentation is configured on root stock level) */
            @Nonnull final var registrationAuthoritiesGroupedByStockUuid = new HashMap<String, Set<GlobalReference>>();
            for (final var stockRegAuthTuple : stockRegAuthTuples) {
                var stockUuid = Objects.requireNonNull(((Uuid) stockRegAuthTuple[0]).getUuid());
                @Nullable final var registrationAuthority = (GlobalReference) stockRegAuthTuple[1];
                registrationAuthoritiesGroupedByStockUuid.computeIfAbsent(stockUuid, x -> new HashSet<>());
                registrationAuthoritiesGroupedByStockUuid.get(stockUuid).add(registrationAuthority); // Note: NULL will be added as such. It may be augmented later on...
            }

            /* Collect augmentation rules */
            @Nonnull final var augmentedStocks = augmentationRulesByStockUuid.keySet();
            final var isGlobalAugmentationConfigured = augmentedStocks.contains(null); // null refers to all stocks, see documentation about registration authority augmentation or the corresponding configuration service init methods.

            /* Augment uuids according to rules */
            @Nonnull final var augmentedRegAuthUuidSet = new HashSet<String>();
            for (final var stock : registrationAuthoritiesGroupedByStockUuid.keySet()) {

                final var dbDefinedRegAuthUuids = registrationAuthoritiesGroupedByStockUuid.get(stock).stream() // We only care about the uuids
                        .map(ref -> ref != null ? ref.getRefObjectId() : null) // NULL is a value that may be augmented to some contact.
                        .collect(Collectors.toSet());

                if (!augmentedStocks.contains(stock) && !isGlobalAugmentationConfigured) { // == no augmentation needed for this stock
                    augmentedRegAuthUuidSet.addAll(dbDefinedRegAuthUuids);

                } else { // we need to augment the uuids on this stock
                    final var key = augmentedStocks.contains(stock) ? stock : null; // not contains -> global rule needs to be applied
                    @Nonnull final var rule = Objects.requireNonNull(augmentationRulesByStockUuid.get(key));
                    @Nonnull final var mode = Objects.requireNonNull(rule[2]); // see documentation about registration authority augmentation or the corresponding configuration service init methods.
                    @Nonnull final var value = Objects.requireNonNull(rule[0]); // see documentation about registration authority augmentation or the corresponding configuration service init methods.
                    augmentedRegAuthUuidSet.addAll(augmentRegistrationAuthorityUuidSet(dbDefinedRegAuthUuids, value, mode));
                }
            }
            setOfReferencedContactsUuids.addAll(augmentedRegAuthUuidSet);
        }

        if (setOfReferencedContactsUuids.isEmpty()) {
            return new ArrayList<>(); // We don't need to fetch any contacts anyway
        }

        setOfReferencedContactsUuids.remove(null); // Now that we're done with uuid-augmentations, null can (and should be) dropped

        /* Collect referenced contacts from db */
        @Nonnull final var matchClauses = new ArrayList<String>();
        @Nonnull final var paramMap = new HashMap<String, String>();
        final var augmentRegAuthUuidIterator = setOfReferencedContactsUuids.iterator();
        for (int i = 0; i < setOfReferencedContactsUuids.size(); i++) { // We need to match the contacts with individual clauses, because we're using a most-recent version sub-query per contact.
            final var uuid = augmentRegAuthUuidIterator.next();
            final var sb = new StringBuilder();
            final var key = "contactUuid" + i;
            paramMap.put(key, uuid);
            sb.append("(c.uuid.uuid = :").append(key)
                    .append(" AND (")
                    .append(" c.version.version = (")
                    .append("SELECT MAX (cc.version.version) FROM Contact cc WHERE cc.uuid.uuid = :").append(key) // Per contact, we only want the most recent db entry.
                    .append(")))");
            matchClauses.add(sb.toString());
        }

        if (matchClauses.isEmpty()) {
            return new ArrayList<>(); // If there won't be any matches, we have to (!) return an empty list. (This is best done by skipping the db call...)
        }

        final var q = em.createQuery("SELECT c FROM Contact c WHERE " + String.join(" OR ", matchClauses));
        paramMap.forEach(q::setParameter);
        @Nonnull final var resultSet = new HashSet<Contact>(q.getResultList());
        resultSet.remove(null);

        /* Override names by config if there's augmentation rules */
        @Nonnull final var contactsByUuid = resultSet.stream().collect(Collectors.toMap(DataSet::getUuidAsString, c -> c));
        @Nonnull final var rulesByContactUuid = new HashMap<String, String[]>();
        for (final var rule : augmentationRulesByStockUuid.values()) {
            rulesByContactUuid.put(rule[0], rule); // Several rules can reference the same contact (via uuid). We only take the last rule per contact. In particular, if different names are applied for the same contact, we use the last name we find.
        }

        /* We need to deal with the references that are not known to the database, too. */
        for (final var uuid : setOfReferencedContactsUuids) {
            if (contactsByUuid.get(uuid) == null) {
                final var contactStub = new Contact();
                contactStub.setUuid(new Uuid(uuid));
                resultSet.add(contactStub);
            }
        }

        /* Augment names according to augmentation rules or using some suitable global reference's short description */
        final var mostRecentRegAuthsByContactUuid = reduceToSingleNewestGlobalReferencePerRefObject(setContactOfReferences); // There will be several conflicting global reference states available as a data backup so let's settle for one per contact

        for (final var contact : resultSet) { // Any contact may need overriding (i.e. its name or short-name). Augmentation allows unifying the display name, so aim to provide the contacts in a manner consistent with applied augmentations
            @Nonnull final var uuid = Objects.requireNonNull(contact.getUuidAsString());
            @Nullable final var name = contact.getName();
            @Nullable final var shortName = contact.getShortName();

            final var isOverrideName = name == null || name.getLStrings().isEmpty(); // The name will only be overridden, when it is not present
            final var isOverrideShortName = shortName == null || shortName.getLStrings().isEmpty() || rulesByContactUuid.containsKey(uuid); // The short name will only be overridden when it is either not present or there's an augmentation rule for this contact
            final var contactDataNeedsToBeOverridden = isOverrideName || isOverrideShortName;

            if (contactDataNeedsToBeOverridden) {
                @Nullable final var augmentationRule = rulesByContactUuid.get(uuid);
                @Nullable final var nameOverrideValue = augmentationRule != null
                        ? new MultiLangStringMapAdapter(augmentationRule[1]) // We prefer an augmentation value, should it exist
                        : mostRecentRegAuthsByContactUuid.get(uuid).getShortDescription(); // Otherwise, there has to exist a global reference. Let's use its short-description
                if (isOverrideShortName) {
                    contact.setShortName(nameOverrideValue);
                }
                if (isOverrideName) {
                    contact.setName(nameOverrideValue);
                }
            }
        }
        return new ArrayList<>(resultSet);
    }

    /**
     * This method takes a collection of global references and selects one per referenced object.
     * The selection is done by favoring the newest version that has been referenced among the provided global references.
     *
     * @param globalReferences any collection of global references
     * @return per referenced object a single global reference that points to the newest version among all referenced versions
     */
    private Map<String, GlobalReference> reduceToSingleNewestGlobalReferencePerRefObject(Collection<GlobalReference> globalReferences) {
        final var result = new HashMap<String, GlobalReference>();

        final var referencesGroupedByUuid = new HashMap<String, List<GlobalReference>>(); // For each referenced object we may have multiple global references -- so we group them accordingly
        for (final var ref : globalReferences) {
            if (ref != null && ref.getRefObjectId() != null) {
                final var uuid = ref.getRefObjectId();
                referencesGroupedByUuid.computeIfAbsent(uuid, x -> new ArrayList<>());
                referencesGroupedByUuid.get(uuid).add(ref);
            }
        }

        for (final var refsByUuid : referencesGroupedByUuid.entrySet()) { // For each referenced object we want to collect the reference pointing to the "newest" object in terms of version number. Note that versions  can be null.
            final var uuid = refsByUuid.getKey();
            final var refs = refsByUuid.getValue();

            GlobalReference candidate = refs.get(0);
            for (final var current : refs) {
                final var currentVersion = current.getVersion();
                final var candidateVersion = candidate.getVersion();
                if (candidateVersion == null) {
                    candidate = current; // We do not hold on to global references without version -- the next may have a version.
                } else if (currentVersion != null) {
                    if (candidateVersion.compareTo(currentVersion) < 0) { // If both references have versions we prefer the newer one.
                        candidate = current;
                    }
                } // else the current version is null and the candidate version isn't, so we hold on to the versioned reference.
            }
            result.put(uuid, candidate);
        }
        return result;
    }

    /**
     * This method applies an augmention rule to a given set of registration authority uuids.
     * The augmention rule consists of an override value and a mode (e.g. implying override only when original value is null).
     *
     * @param originalRegistrationAuthorities set of registration authority uuids
     * @param augmentationValue               the override value
     * @param augmentationMode                the mode to be used when overriding (e.g. override only null values or all values)
     * @return an augmented set of registration authority uuids. E.g. when the mode implies override all, it will only contain one uuid, the augmentation value.
     * @see ConfigurationService#AUGMENTATION_MODE_OVERRIDE
     * @see ConfigurationService#AUGMENTATION_MODE_NO_OVERRIDE
     */
    private Set<String> augmentRegistrationAuthorityUuidSet(Set<String> originalRegistrationAuthorities, String augmentationValue, String augmentationMode) {
        final var result = new HashSet<String>();
        if (ConfigurationService.AUGMENTATION_MODE_OVERRIDE.equals(augmentationMode)) {
            result.add(augmentationValue); // It doesn't matter what it was, we'll override anyway
        } else if (ConfigurationService.AUGMENTATION_MODE_NO_OVERRIDE.equals(augmentationMode)) {
            for (final var value : originalRegistrationAuthorities) {
                if (value != null) {
                    result.add(value); // In this mode we keep non-null values
                } else {
                    result.add(augmentationValue);
                }
            }
        } else {
            throw new IllegalStateException("Unexpected augmentation mode parameter.");
        }
        return result;
    }

    public List<Exchange> getReferenceExchanges(Process p) {
        return getReferenceExchanges(p, null);
    }

    public List<Exchange> getReferenceExchanges(Process p, ExchangeDirectionValues direction) {

        Collection<Integer> referenceExchangeIds = p.getInternalReference().getReferenceIds();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("reference exchange IDs for process " + p.getId() + ": " + referenceExchangeIds);

        if (referenceExchangeIds == null || referenceExchangeIds.size() == 0)
            return Collections.EMPTY_LIST;

        EntityManager em = PersistenceUtil.getEntityManager();

        String queryString = "SELECT e FROM Process p, p.exchanges e WHERE p.id=:processId and e.internalId IN :ids";

        if (direction != null)
            queryString += " AND e.exchangeDirection=:direction";

        Query query = em.createQuery(queryString);
        query.setParameter("processId", p.getId());
        query.setParameter("ids", referenceExchangeIds);

        if (direction != null)
            query.setParameter("direction", direction);

        List<Exchange> result = query.getResultList();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(result.size() + " reference exchanges for process " + p.getId() + ": " + result);

        return result;
    }

    @Override
    public Process getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }
}
