package de.iai.ilcd.model.dao;

import com.google.common.collect.Lists;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetVO;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.fzk.iai.ilcd.service.model.IDataSetVO;
import de.fzk.iai.ilcd.service.model.ILCIAMethodListVO;
import de.fzk.iai.ilcd.service.model.IProcessListVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.adapter.GlobalReferenceAdapter;
import de.iai.ilcd.model.adapter.dataset.ProcessVOAdapter;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.utils.DistributedSearchLog;
import de.iai.ilcd.model.utils.ProcessComparator;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.StringUtil;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.apache.velocity.tools.view.ParameterTool;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.*;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.iai.ilcd.model.dao.ForeignDataSetsHelper.DIST_MAX_RESULTS;

/**
 * Common implementation for data set DAO objects
 *
 * @param <T>
 * Element type that is accessed by this DAO
 * @param <L>
 * ListVO interface type for the access
 * @param <D>
 * DataSetVO interface type for the access
 */

/**
 * @param <T>
 * @param <L>
 * @param <D>
 * @author oli
 */
public abstract class DataSetDao<T extends DataSet, L extends IDataSetListVO, D extends IDataSetVO>
        extends AbstractLongIdObjectDao<T> {

    public final static Pattern UUID_REGEX_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");
    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(DataSetDao.class);
    /**
     * The class accessed by this DAO
     */
    private final Class<L> listVOClass;
    /**
     * The class accessed by this DAO
     */
    private final Class<D> voClass;
    /**
     * The data set type
     */
    private final DataSetType dataSetType;

    /**
     * Create a data set DAO
     *
     * @param jpaName       the name in JPA of class
     * @param accessedClass the class accessed by this DAO
     * @param listVOClass   the interface based on {@link ILCIAMethodListVO} for the
     *                      accessed class
     * @param dataSetType   type of the data set
     */
    public DataSetDao(String jpaName, Class<T> accessedClass, Class<L> listVOClass, Class<D> voClass,
                      DataSetType dataSetType) {
        super(jpaName, accessedClass);
        this.listVOClass = listVOClass;
        this.voClass = voClass;
        this.dataSetType = dataSetType;
    }

    /**
     * return the suitable Dao object for a given GlobalReferenceTypeValue
     *
     * @return the suitable dao
     */
    public static DataSetDao<?, ?, ?> getDao(DataSet dataset) {
        return dataset.getCorrespondingDSDao();
    }

    /**
     * Returns the necessary Dao objects for a given DataSetType.
     *
     * @param type
     * @return
     */
    public static Set<DataSetDao<?, ?, ?>> getDaosForType(GlobalReferenceTypeValue type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        Set<DataSetDao<?, ?, ?>> daoCollection = new HashSet<>();

        switch (type) {
            case CONTACT_DATA_SET:
                daoCollection.add(new ContactDao());
                break;
            case SOURCE_DATA_SET:
                daoCollection.add(new SourceDao());
                break;
            case FLOW_DATA_SET:
                daoCollection.add(new ElementaryFlowDao());
                daoCollection.add(new ProductFlowDao());
                break;
            case LCIA_METHOD_DATA_SET:
                daoCollection.add(new LCIAMethodDao());
                break;
            case FLOW_PROPERTY_DATA_SET:
                daoCollection.add(new FlowPropertyDao());
                break;
            case UNIT_GROUP_DATA_SET:
                daoCollection.add(new UnitGroupDao());
                break;
            case PROCESS_DATA_SET:
                daoCollection.add(new ProcessDao());
                break;
            case LIFE_CYCLE_DATA_SET:
                daoCollection.add(new LifeCycleModelDao());
                break;
            default:
                throw new IllegalArgumentException();
        }
        return daoCollection;
    }

    /**
     * return the suitable Dao object for a given GlobalReferenceTypeValue
     *
     * @param type the GlobalReferenceTypeValue
     * @return the suitable dao
     */
    public static DataSetDao<?, ?, ?> getDao(GlobalReferenceTypeValue type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        switch (type) {
            case CONTACT_DATA_SET:
                return new ContactDao();
            case SOURCE_DATA_SET:
                return new SourceDao();
            case FLOW_DATA_SET:
                return new ProductFlowDao();
            case LCIA_METHOD_DATA_SET:
                return new LCIAMethodDao();
            case FLOW_PROPERTY_DATA_SET:
                return new FlowPropertyDao();
            case UNIT_GROUP_DATA_SET:
                return new UnitGroupDao();
            case PROCESS_DATA_SET:
                return new ProcessDao();
            case LIFE_CYCLE_DATA_SET:
                return new LifeCycleModelDao();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static <E extends IDataSetListVO> List<E> filter(List<E> dataSets, String[] nodeIds) {
        if (nodeIds == null || nodeIds.length == 0)
            return dataSets;

        List<String> nodeIdsList = Arrays.asList(nodeIds);

        ArrayList<E> result = new ArrayList<E>();

        for (E ds : dataSets) {
            if (nodeIdsList.contains(ds.getSourceId()))
                result.add(ds);
        }
        return result;
    }

    /**
     * Creates a simple 'where x like y' SQL Statement for multiple 'x'.
     *
     * @param typeAlias         shared type alias
     * @param fields            the fields to be considered
     * @param fieldsConjunction should all or just any field be like search term:
     *                          "AND", "OR".
     * @param param             placeholder for the search term.
     * @param comparator        "LIKE" or "NOT LIKE".
     * @return
     */
    private static String createWhereAnyLikeStatement(String typeAlias, String[] fields, String fieldsConjunction,
                                                      String param, String comparator) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < fields.length; i++) {
            sb.append("(");
            if (!typeAlias.isEmpty())
                sb.append(typeAlias + ".");
            sb.append(fields[i] + " " + comparator + " :" + param);
            sb.append(")");
            if (i < fields.length - 1)
                sb.append(" " + fieldsConjunction + " ");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Generates a sufficiently unique placeholder for the prepared statement in the
     * method 'splitMultiContainClause'.
     *
     * @param typeAlias
     * @param fields
     * @param index
     * @param outerIndex additional context string. Can be null if this level of
     *                   uniqueness is not required.
     * @return
     */
    private static String generateParamID(String typeAlias, String[] fields, int index, String outerIndex) {
        StringBuilder sb = new StringBuilder("");
        if (!typeAlias.isEmpty())
            sb.append(typeAlias);
        for (String s : fields)
            sb.append(StringUtils.remove(s, "."));
        if (outerIndex != null)
            sb.append(outerIndex);
        sb.append(index);
        return sb.toString();
    }

    /**
     * Creates a (prepared) SQL Query for likening multiple keywords to multiple
     * fields, while also modifying a given parameter-value-map.
     *
     * @param typeAlias           (optional) shared type alias
     * @param fields              (String or String[]) field(s) to be considered
     * @param fieldsConjunction   should all fields or just any be like search
     *                            term: "AND", "OR"
     * @param keyphrase           a String containing the keywords we want to
     *                            query for
     * @param keywordsConjunction logical conjunction can be "AND" or "OR"
     * @param paramValues         parameter/value Mapping
     * @param comparator          SQL-keyword used in the statement: "LIKE" or
     *                            "NOT LIKE".
     * @param outerIndex          can be used to generate more unique paramIds to
     *                            avoid overrides in paramValues. Can be set null
     *                            if not needed.
     * @return
     */
    public static String splitMultiContainClause(String typeAlias, String[] fields, String fieldsConjunction,
                                                 String keyphrase, String keywordsConjunction, Map<String, Object> paramValues, String comparator, String outerIndex) {
        StringBuilder sb = new StringBuilder("(");
        String[] keywords = StringUtils.split(keyphrase);
        for (int i = 0; i < keywords.length; i++) {
            String paramID = generateParamID(typeAlias, fields, i, outerIndex);
            paramValues.put(paramID, "%" + keywords[i] + "%");

            sb.append(createWhereAnyLikeStatement(typeAlias, fields, fieldsConjunction, paramID, comparator));
            if (i < keywords.length - 1)
                sb.append(" " + keywordsConjunction + " ");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String splitMultiContainClause(String typeAlias, String[] fields, String fieldsConjunction,
                                                 String keyphrase, String keywordsConjunction, Map<String, Object> paramValues, String comparator) {
        return splitMultiContainClause(typeAlias, fields, fieldsConjunction, keyphrase, keywordsConjunction, paramValues, comparator, null);
    }

    public static String splitMultiContainClause(String typeAlias, String[] fields, String fieldsConjunction,
                                                 String keyPhrase, String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause(typeAlias, fields, fieldsConjunction, keyPhrase, keywordsConjunction,
                paramValues, "LIKE");
    }

    public static String splitMultiContainClause(String[] fields, String fieldsConjunction, String keyphrase, String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause("", fields, fieldsConjunction, keyphrase, keywordsConjunction, paramValues);
    }

    public static String splitMultiContainClause(String typeAlias, String field, String keyphrase, String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause(typeAlias, new String[]{field}, "", keyphrase, keywordsConjunction, paramValues);
    }

    public static String splitMultiContainClause(String field, String keyphrase, String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause("", new String[]{field}, "", keyphrase, keywordsConjunction, paramValues);
    }

    public static String splitMultiContainClause(String typeAlias, String[] fields, String fieldsConjunction,
                                                 String[] keyphrases, String keyphrasesConjunction, String keywordsConjunction,
                                                 Map<String, Object> paramValues, String comparator) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < keyphrases.length; i++) {
            sb.append("(");
            sb.append(splitMultiContainClause(typeAlias, fields, fieldsConjunction, keyphrases[i], keywordsConjunction,
                    paramValues, comparator, String.valueOf(i)));
            sb.append(")");
            if (i < (keyphrases.length - 1))
                sb.append(" " + keyphrasesConjunction + " ");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String splitMultiContainClause(String field, String[] keyphrases, String keyphrasesConjunction,
                                                 String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause("", new String[]{field}, "", keyphrases, keyphrasesConjunction,
                keywordsConjunction, paramValues, "LIKE");
    }

    public static String splitMultiExcludeClause(String field, String[] keyphrases, String keyphrasesConjunction,
                                                 String keywordsConjunction, Map<String, Object> paramValues) {
        return splitMultiContainClause("", new String[]{field}, "", keyphrases, keyphrasesConjunction,
                keywordsConjunction, paramValues, "NOT LIKE");
    }

    protected static String buildMostRecentVersionsOnlySubQuery(String typeAlias, String typeName, String defaultJoins,
                                                                String joins, List<String> whereClauses) {

        // in the subquery, we need to use different aliases so we
        // replace all occurrences of " a." (typeAlias) with " c."
        // as well as replacing all aliases in JOINs and WHEREs
        StringBuilder subQuery = new StringBuilder();
        subQuery.append(typeAlias).append(".version.version=(SELECT MAX(");
        subQuery.append("c.version.version) FROM ").append(typeName).append(" c ");

        if (!StringUtils.isBlank(defaultJoins)) {
            subQuery.append(" ").append(replaceAliases(defaultJoins.trim(), typeAlias)).append(" ");
        }

        if (!StringUtils.isBlank(joins)) {
            subQuery.append(" ").append(replaceAliases(joins.trim(), typeAlias)).append(" ");
        }

        subQuery.append(" WHERE ");
        if (!whereClauses.isEmpty()) {
            subQuery.append(StringUtils.join(replaceAliases(whereClauses, typeAlias), " AND "));
            subQuery.append(" AND ");
        }

        subQuery.append(typeAlias).append(".uuid=c.uuid ");
        subQuery.append(") ");

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("most recent versions only subquery: " + subQuery.toString());

        return subQuery.toString();
    }

    protected static String replaceAliases(String input, String typeAlias) {
        input = replaceAliasDefinitions(input, typeAlias);
        input = replaceAliasReferences(input, typeAlias);
        return input;
    }

    protected static String replaceAliasDefinitions(String input, String typeAlias) {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("processing \n" + input + "\n to \n");
        input = input.replaceAll(" " + typeAlias + "$", " c ");
        input = input.replaceAll("(\\.containingDataStocks )ads", "$1ads2");
        input = input.replaceAll(" ds(\\s|$)", " ds2");
        input = input.replaceAll("(\\.classifications )cl(\\s|$)", "$1cl2 ");
        input = input.replaceAll("(\\.classes )clz(\\s|$)", "$1clza ");
        input = input.replaceAll("(\\.classes )clz0(\\s|$)", "$1clz0a ");
        input = input.replaceAll("(\\.classes )clz1(\\s|$)", "$1clz1a ");
        input = input.replaceAll("(\\.classes )clz2(\\s|$)", "$1clz2a ");
        input = input.replaceAll("(\\.classes )clz3(\\s|$)", "$1clz3a ");
        input = input.replaceAll(" slang(\\s|$)", " slang2 ");
        input = input.replaceAll(" bn(\\s|$)", " bn2 ");
        input = input.replaceAll(" nr(\\s|$)", " nr2 ");
        input = input.replaceAll(" nl(\\s|$)", " nl2 ");
        input = input.replaceAll(" nu(\\s|$)", " nu2 ");
        input = input.replaceAll(" syn(\\s|$)", " syn2 ");
        input = input.replaceAll("(\\.ownerReference\\.shortDescription )ownerref", "$1ownerref2");
        input = input.replaceAll("(\\.complianceSystems )compliancesystems", "$1compliancesystems2");
        input = input.replaceAll("(\\.compliancesystems\\.sourceReference )compliancesourceref", "$1compliancesourceref2");
        input = input.replaceAll("(\\.dataSources )datasource", "$1datasource2");
        input = input.replaceAll("(\\.tags )tag", "$1tag2");
        input = input.replaceAll("(\\.datasetGenerator )datasetgenerator", "$1datasetgenerator2");
        input = input.replaceAll("(\\.referenceToRegistrationAuthority\\.shortDescription )regAuthority", "$1regAuthority2");
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(input);
        return input;
    }

    protected static String replaceAliasReferences(String input, String typeAlias) {
        input = input.replaceAll("([\\s\\(]?)" + typeAlias + "\\.", "$1c\\.");
        input = input.replaceAll("([\\s\\(]?)ds\\.", "$1ds2\\.");
        input = input.replaceAll("([\\s\\(]?)ads\\.", "$1ads2\\.");
        input = input.replaceAll("([\\s\\(]?)cl\\.", "$1cl2\\.");
        input = input.replaceAll("([\\s\\(]?)clz\\.", "$1clza\\.");
        input = input.replaceAll("([\\s\\(]?)clz0\\.", "$1clz0a\\.");
        input = input.replaceAll("([\\s\\(]?)clz1\\.", "$1clz1a\\.");
        input = input.replaceAll("([\\s\\(]?)clz2\\.", "$1clz2a\\.");
        input = input.replaceAll("([\\s\\(]?)clz3\\.", "$1clz3a\\.");
        input = input.replaceAll("([\\s\\(]?)slang\\.", "$1slang2\\.");
        input = input.replaceAll("([\\s\\(]?)bn([\\.\\s])", "$1bn2$2");
        input = input.replaceAll("([\\s\\(]?)nr([\\.\\s])", "$1nr2$2");
        input = input.replaceAll("([\\s\\(]?)nl([\\.\\s])", "$1nl2$2");
        input = input.replaceAll("([\\s\\(]?)nu([\\.\\s])", "$1nu2$2");
        input = input.replaceAll("([\\s\\(]?)syn([\\.\\s])", "$1syn2$2");
        return input;
    }

    protected static List<String> replaceAliases(List<String> input, String typeAlias) {
        List<String> result = new ArrayList<String>();
        for (String s : input) {
            result.add(replaceAliasReferences(s, typeAlias));
        }
        return result;
    }

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    /**
     * collects all linked datasets and returns them as a list of DataSet objects
     *
     * @param dataset
     * @return the list of dependencies
     */
    public abstract Set<DataSet> getDependencies(DataSet dataset, DependenciesMode mode);

    public abstract Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList);

    protected void addDependencies(DataSet nativeRef, List<IGlobalReference> refs, IDataStockMetaData stockMeta, Set<DataSet> dependencies) {
        for (IGlobalReference ref : refs)
            addDependency(nativeRef, ref, stockMeta, dependencies);
    }

    protected void addDependencies(List<GlobalReferenceType> refs, IDataStockMetaData stockMeta, Set<DataSet> dependencies) {
        for (GlobalReferenceType ref : refs)
            addDependency(ref, stockMeta, dependencies);
    }

    protected void addDependency(GlobalReferenceType ref, IDataStockMetaData stockMeta, Set<DataSet> dependencies) {
        addDependency(null, new GlobalReferenceAdapter(ref, null), stockMeta, dependencies);
    }

    protected void addDependency(DataSet nativeRef, IGlobalReference globalRef, IDataStockMetaData stockMeta, Set<DataSet> dependencies) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("looking up local dependency with native ref id " + (nativeRef != null ? nativeRef.getUuidAsString() : "(null)") + ", globalref " + (globalRef != null ? globalRef.getRefObjectId() : "(null)") + " from data stock " + (stockMeta != null ? stockMeta.getName() : "(null)"));

        if (nativeRef == null && globalRef == null) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("no reference given, arguments are null");
            return;
        }

        DataSet candidate = getLocalDependency(nativeRef, globalRef, stockMeta);

        if (candidate != null) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("found dependency " + candidate.getUuidAsString() + " " + candidate.getDataSetVersion() + "(" + candidate.getDataSetType() + ")");
            dependencies.add((DataSet) candidate);
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("no local dependency found.");
        }
    }

    /**
     * returns a DataSet object for a pair of unknown native and global
     * references.
     *
     * @param nativeRef a native reference (from the local database)
     * @param globalRef a GlobalReference object where it is unknown whether the
     *                  object it represents is stored in the local database or not
     * @param stockMeta if supplied, resulting dataset has to be a member of the data
     *                  stock
     * @return
     */
    public DataSet getLocalDependency(DataSet nativeRef, IGlobalReference globalRef, IDataStockMetaData stockMeta) {
        DataSet result = null;
        if (nativeRef == null && globalRef == null) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("no reference given, arguments are null");
            return null;
        }
        if (nativeRef != null) {
            result = nativeRef;
        } else {
            String uuid = null;
            try {
                uuid = globalRef.getRefObjectId();

                if (uuid == null)
                    return null;

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("trying to find object with uuid " + uuid + ", type " + globalRef.getType());

                DataSetDao<?, ?, ?> dao = DataSetDao.getDao(globalRef.getType());
                result = dao.getByUuid(uuid);
                if (stockMeta != null) {
                    if (!dao.isInDatastockByUuid(uuid, stockMeta))
                        result = null;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("could not get local dependency with uuid " + uuid);
            } catch (NullPointerException e) {
                LOGGER.error("error", e);
            }
        }
        return result;
    }

    /**
     * Get <code>pageSize</code> data sets starting on <code>startIndex</code>
     *
     * @param startIndex start index
     * @param pageSize   page size
     * @return list of matching elements
     */
    public List<T> getDataSets(int startIndex, int pageSize) {
        return super.get(startIndex, pageSize);
    }

    /**
     * Get <code>pageSize</code> data sets starting on <code>startIndex</code>, returning only the latest datasets
     *
     * @param startIndex start index
     * @param pageSize   page size
     * @return list of matching elements
     */
    @SuppressWarnings("unchecked")
    public List<T> getDataSetsMostRecent(int startIndex, int pageSize) {
        EntityManager em = PersistenceUtil.getEntityManager();
        String jpaName = this.getJpaName();
        return em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.version.version=(SELECT MAX(c.version.version) FROM " + jpaName + " c WHERE a.uuid=c.uuid )").setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
    }

    /**
     * Get <code>pageSize</code> data sets starting on <code>startIndex</code>, returning only the latest datasets
     *
     * @return list of matching elements
     */
    public long getCountMostRecent() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (long) em.createQuery("SELECT COUNT(a) FROM " + this.getJpaName() + " a WHERE a.version.version=(SELECT MAX(c.version.version) FROM " + this.getJpaName() + " c WHERE a.uuid=c.uuid )").getSingleResult();
    }

    /**
     * Get data set by UUID string and version
     *
     * @param uuid    the UUID string
     * @param version the version of the method
     * @return data set for the specified UUID string/version, or
     * <code>null</code> if none found
     */
    @SuppressWarnings("unchecked")
    public T getByUuidAndVersion(String uuid, DataSetVersion version) {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("select a from " + this.getJpaName()
                    + " a where a.uuid.uuid=:uuid and a.version.majorVersion=:major and a.version.minorVersion=:minor and a.version.subMinorVersion=:subMinor");
            q.setParameter("uuid", uuid);
            q.setParameter("major", version.getMajorVersion());
            q.setParameter("minor", version.getMinorVersion());
            q.setParameter("subMinor", version.getSubMinorVersion());
            return (T) q.getSingleResult();
        } catch (NoResultException e) {
            // none found, so let's return null
            return null;
        }
    }

    public T getByUuidAndVersion(String uuid, String version) {
        DataSetVersion ver = null;
        if (version == null)
            return this.getByUuid(uuid);
        try {
            ver = DataSetVersion.parse(version);
            return this.getByUuidAndVersion(uuid, ver);
        } catch (Exception e) {
//			return this.getByUuid(uuid); // fallback to latest version
            return null;
        }
    }

    /**
     * Get data set by UUID string and version; if the version is null, get the latest one
     *
     * @param uuid    the UUID string
     * @param version the version of the method (optional)
     * @return data set for the specified UUID string/version, or the latest one if version is null, or
     * <code>null</code> if none found
     */
    public T getByUuidAndOptionalVersion(String uuid, DataSetVersion version) {
        if (version == null)
            return this.getByUuid(uuid);
        else
            return this.getByUuidAndVersion(uuid, version);
    }

    /**
     * Get a the most recent data set of type T by UUID string
     *
     * @param uuid the UUID string
     * @return data set of type T for the specified UUID string, or
     * <code>null</code> if none found
     */
    @SuppressWarnings("unchecked")
    public T getByUuid(String uuid) {
        EntityManager em = PersistenceUtil.getEntityManager();

        try {
            Query q = em.createQuery("select a from " + this.getJpaName()
                    + " a where a.uuid.uuid=:uuid order by a.version.majorVersion desc, a.version.minorVersion desc, a.version.subMinorVersion desc");
            q.setParameter("uuid", uuid);
            List<T> results = q.getResultList();
            if (results.size() > 0) {
                return results.get(0);
            } else {
                return null;
            }
        } catch (NoResultException e) {
            // none found, so let's return null
            return null;
        }
    }

    /**
     * Get data set by UUID string and version
     *
     * @param uuid    the UUID string
     * @param version the version of the method
     * @return data set for the specified UUID string/version, or
     * <code>null</code> if none found
     */
    public boolean existsByUuidAndVersion(String uuid, DataSetVersion version) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            Query q = em.createQuery("select count(a) from " + this.getJpaName()
                    + " a where a.uuid.uuid=:uuid and a.version.majorVersion=:major and a.version.minorVersion=:minor and a.version.subMinorVersion=:subMinor");
            q.setParameter("uuid", uuid);
            q.setParameter("major", version.getMajorVersion());
            q.setParameter("minor", version.getMinorVersion());
            q.setParameter("subMinor", version.getSubMinorVersion());
            if ((Long) q.getSingleResult() > 0)
                return true;
            else
                return false;
        } catch (NoResultException e) {
            // none found, so let's return null
            return false;
        }
    }

    /**
     * Get a the most recent data set of type T by UUID string
     *
     * @param uuid the UUID string
     * @return data set of type T for the specified UUID string, or
     * <code>null</code> if none found
     */
    public boolean existsByUuid(String uuid) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            Query q = em.createQuery("select count(a) from " + this.getJpaName()
                    + " a where a.uuid.uuid=:uuid");
            q.setParameter("uuid", uuid);
            if ((Long) q.getSingleResult() > 0)
                return true;
            else
                return false;
        } catch (NoResultException e) {
            // none found, so let's return null
            return false;
        }
    }

    /**
     * Get list of data sets in specified data stocks
     *
     * @param stocks                data stocks to get data sets from
     * @param language              the language of the datasets
     * @param mostRecentVersionOnly return only the most recent version of each dataset
     * @param startIndex            start index
     * @param pageSize              page size
     * @return list of data sets
     */
    public List<T> getDataSets(IDataStockMetaData[] stocks, String language, boolean mostRecentVersionOnly,
                               Integer startIndex, Integer pageSize) {
        return getDataSets(stocks, language, false, mostRecentVersionOnly, startIndex, pageSize);
    }

    public List<DataSetReference> getDataSetReferences(IDataStockMetaData[] stocks, String language,
                                                       boolean mostRecentVersionOnly, Integer startIndex,
                                                       Integer pageSize) {
        final DataSetType dataSetType = this.dataSetType;
        final Class<? extends DataSet> accessedClass = this.getAccessedClass();
        final DataSetDaoType daoType = DataSetDaoType.getFor(accessedClass);

        String typeAlias = "a";
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ")
                .append(typeAlias).append(".id, ")
                .append(typeAlias).append(".uuid, ")
                .append(typeAlias).append(".version, ")
                .append(typeAlias).append(".nameCache")
                .append(" FROM ")
                .append(this.getJpaName()).append(" ").append(typeAlias);

        Function<Object[], DataSetReference> resultToDataSetReference = arr -> {
            Long id = null;
            Uuid uuid = null;
            DataSetVersion version = null;
            String nameCache = null;
            try {
                id = (Long) arr[0];
                uuid = new Uuid(String.valueOf(arr[1]));
                version = DataSetVersion.parse(String.valueOf(arr[2]));
                nameCache = String.valueOf(arr[3]);
            } catch (Exception e) {
                LOGGER.error("Ignoring row data " + Arrays.toString(arr), e);
                return null;
            }

            DataSetReference ref = new DataSetReference();
            ref.setId(id);
            ref.setUuid(uuid);
            ref.setVersion(version);
            ref.setDisplayName(nameCache);
            ref.setType(dataSetType);
            ref.setDaoType(daoType);
            ref.setDataSetClass(accessedClass);
            return ref;
        };

        List<String> wheres = new ArrayList<>();
        List<String> joins = new ArrayList<>();
        HashMap<String, Object> paramMap = new HashMap<>();

        if (!ArrayUtils.isEmpty(stocks)) {
            List<String> stockClauses = new ArrayList<String>();
            List<String> rootStockClauses = new ArrayList<String>();
            List<Long> dsIds = new ArrayList<Long>();
            boolean dsJoinDone = false;
            for (IDataStockMetaData m : stocks) {
                // root data stock
                if (m.isRoot()) {
                    String paramName = "rootDsId" + Long.toString(m.getId());
                    rootStockClauses.add(typeAlias + ".rootDataStock.id=:" + paramName);
                    paramMap.put(paramName, m.getId());
                }
                // non root data stock
                else {
                    if (!dsJoinDone) {
                        joins.add("LEFT JOIN " + typeAlias + ".containingDataStocks ads");
                        dsJoinDone = true;
                    }
                    dsIds.add(m.getId());
                }
            }
            if (!rootStockClauses.isEmpty()) {
                stockClauses.add("(" + this.join(rootStockClauses, " OR ") + ")");
            }
            if (dsJoinDone && !dsIds.isEmpty()) {
                stockClauses.add("ads.id IN(" + StringUtils.join(dsIds, ',') + ")");
            }
            if (!stockClauses.isEmpty()) {
                wheres.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
            }
        }

        if (!joins.isEmpty()) {
            querySB.append(" ").append(StringUtils.join(joins, " "));
        }
        if (!wheres.isEmpty()) {
            querySB.append(" WHERE ").append(StringUtils.join(wheres, " AND "));
        }
        DataSetDao.LOGGER.debug("search query: {}", querySB.toString());

        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery(querySB.toString(), String.class);
        q.setFirstResult(startIndex);
        q.setMaxResults(pageSize);
        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
            q.setParameter(param.getKey(), param.getValue());
        }

        List<Object[]> resultListRaw = (List<Object[]>) q.getResultList();
        return resultListRaw.stream()
                .map(resultToDataSetReference)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get list of data sets in specified data stocks
     *
     * @param stocks                data stocks to get data sets from
     * @param language              the language of the datasets
     * @param mostRecentVersionOnly return only the most recent version of each dataset
     * @param langFallback          return also datasets of other languages in the list of
     *                              configured preferred languages
     * @param startIndex            start index
     * @param pageSize              page size
     * @return list of data sets
     */
    @SuppressWarnings("unchecked")
    public List<T> getDataSets(IDataStockMetaData[] stocks, String language, boolean langFallback,
                               boolean mostRecentVersionOnly, Integer startIndex, Integer pageSize) {
        if (ArrayUtils.isEmpty(stocks)) {
            return Collections.emptyList();
        }
        Query q = this.getQueryForDataStocks(stocks, language, mostRecentVersionOnly, false, langFallback);
        if (startIndex != null) {
            q.setFirstResult(startIndex);
        }
        if (pageSize != null) {
            q.setMaxResults(pageSize);
        }
        return q.getResultList();
    }

    /**
     * Get count of data sets in specified data stocks
     *
     * @param stocks   data stocks to get data sets from
     * @param language
     * @return count of data sets
     */
    public Long getCount(IDataStockMetaData[] stocks, String language, boolean mostRecentVersionOnly) {
        return getCount(stocks, language, false, mostRecentVersionOnly);
    }

    /**
     * Get count of data sets in specified data stocks
     *
     * @param stocks       data stocks to get data sets from
     * @param language
     * @param langFallback
     * @return count of data sets
     */
    public Long getCount(IDataStockMetaData[] stocks, String language, boolean langFallback,
                         boolean mostRecentVersionOnly) {
        if (ArrayUtils.isEmpty(stocks)) {
            return 0L;
        }
        Query q = this.getQueryForDataStocks(stocks, language, mostRecentVersionOnly, true, langFallback);
        Long result = (Long) q.getSingleResult();
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("returning count of " + result);
        return result;
    }

    /**
     * Get query for count or listing of data sets in specified data stocks
     *
     * @param stocks                data stocks to get data sets from
     * @param language
     * @param count                 count or listing query
     * @param mostRecentVersionOnly
     * @param langFallback
     * @return created query
     */
    protected Query getQueryForDataStocks(IDataStockMetaData[] stocks, String language, boolean mostRecentVersionOnly,
                                          boolean count, boolean langFallback) {

        return getQueryForDataStocks(stocks, language, mostRecentVersionOnly, count, langFallback, null, null, null);
    }

    /**
     * Get query for count or listing of data sets in specified data stocks
     *
     * @param stocks                data stocks to get data sets from
     * @param language
     * @param count                 count or listing query
     * @param mostRecentVersionOnly
     * @param langFallback
     * @return created query
     */
    protected Query getQueryForDataStocks(IDataStockMetaData[] stocks, String language, boolean mostRecentVersionOnly,
                                          boolean count, boolean langFallback, List<String> joins, List<String> wheres, Map<String, Object> paramMap) {
        EntityManager em = PersistenceUtil.getEntityManager();

        if (wheres == null)
            wheres = new ArrayList<String>();
        if (joins == null)
            joins = new ArrayList<String>();
        if (paramMap == null)
            paramMap = new HashMap<String, Object>();

        if (!ArrayUtils.isEmpty(stocks)) {
            List<String> stockClauses = new ArrayList<String>();
            List<String> rootStockClauses = new ArrayList<String>();
            List<Long> dsIds = new ArrayList<Long>();
            boolean dsJoinDone = false;
            for (IDataStockMetaData m : stocks) {
                // root data stock
                if (m.isRoot()) {
                    String paramName = "rootDsId" + Long.toString(m.getId());
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
                stockClauses.add("(" + this.join(rootStockClauses, " OR ") + ")");
            }
            if (dsJoinDone && !dsIds.isEmpty()) {
                stockClauses.add("ads.id IN(" + StringUtils.join(dsIds, ',') + ")");
            }
            if (!stockClauses.isEmpty()) {
                wheres.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
            }
        }

        if (!StringUtils.isBlank(language)) {
            joins.add("LEFT JOIN a.supportedLanguages slang");

            addLangClauses(language, langFallback, wheres, paramMap);
        }

        if (mostRecentVersionOnly) {
            wheres.add(buildMostRecentVersionsOnlySubQuery("a", this.getJpaName(), null, StringUtils.join(joins, " "),
                    wheres));
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ").append(count ? "COUNT(DISTINCT a)" : "DISTINCT a").append(" FROM ")
                .append(this.getJpaName()).append(" a");
        if (!joins.isEmpty()) {
            queryBuilder.append(" ").append(StringUtils.join(joins, " "));
        }
        if (!wheres.isEmpty()) {
            queryBuilder.append(" WHERE ").append(StringUtils.join(wheres, " AND "));
        }

        DataSetDao.LOGGER.debug("search query: {}", queryBuilder.toString());

        Query q = em.createQuery(queryBuilder.toString(), String.class);
        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
            q.setParameter(param.getKey(), param.getValue());
        }

        return q;
    }

    private void addLangClauses(String language, boolean langFallback, List<String> wheres,
                                Map<String, Object> paramMap) {
        if (langFallback) {
            wheres.add("slang.languageCode IN :lang");
            paramMap.put("lang", ConfigurationService.INSTANCE.getPreferredLanguages());
        } else {
            wheres.add("slang.languageCode=:lang");
            paramMap.put("lang", language);
        }
    }

    private void addLangClauses(String language, boolean langFallback, ValueParser params, List<String> wheres,
                                Map<String, Object> paramMap) {

        final String[] languages = params.getStrings("lang");

        if (languages != null && languages.length > 0) {
            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            int paramNo = 0;
            for (String l : languages) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" OR ");
                }
                sb.append("slang.languageCode=:lang");
                sb.append(paramNo);
                paramMap.put("lang" + paramNo, l);
                paramNo++;
            }
            if (langFallback) {
                if (!first)
                    sb.append(" OR ");
                sb.append(" slang.languageCode IN :deflang");
                paramMap.put("deflang", ConfigurationService.INSTANCE.getPreferredLanguages());
            }
            sb.append(")");
            wheres.add(sb.toString());
        }
    }

    protected abstract String getDataStockField();

    public List<T> get(IDataStockMetaData dataStock, Integer startIndex, Integer pageSize) {
        if (dataStock.isRoot()) {
            return this.getDatasetsFromRootDs(dataStock, startIndex, pageSize);
        } else {
            return this.getDatasetsFromNonRootDs(dataStock, startIndex, pageSize);
        }
    }

    public List<T> get(IDataStockMetaData dataStock) {
        return get(dataStock, null, null);
    }

    public long getCount(IDataStockMetaData dataStock) {
        if (dataStock.isRoot()) {
            return this.getDatasetsFromRootDsCount(dataStock);
        } else {
            return this.getDatasetsFromNonRootDsCount(dataStock);
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> getDatasetsFromNonRootDs(IDataStockMetaData dataStock, Integer startIndex, Integer pageSize) {
        final String dsField = this.getDataStockField();
        StringBuilder sb = new StringBuilder("SELECT b FROM DataStock a LEFT JOIN a.").append(dsField)
                .append(" b WHERE a.id=:dsId");

        EntityManager em = PersistenceUtil.getEntityManager();
        Query query = em.createQuery(sb.toString());

        query.setParameter("dsId", dataStock.getId());
        if (startIndex != null)
            query.setFirstResult(startIndex);
        if (pageSize != null)
            query.setMaxResults(pageSize);

        return query.getResultList();
    }

    private long getDatasetsFromNonRootDsCount(IDataStockMetaData dataStock) {
        final String dsField = this.getDataStockField();

        StringBuilder sb = new StringBuilder("SELECT COUNT(b) FROM DataStock a LEFT JOIN a.").append(dsField)
                .append(" b WHERE a.id=:dsId");

        EntityManager em = PersistenceUtil.getEntityManager();
        Query query = em.createQuery(sb.toString());

        query.setParameter("dsId", dataStock.getId());

        Long resultCount = (Long) query.getSingleResult();

        return resultCount.longValue();
    }

    @SuppressWarnings("unchecked")
    private List<T> getDatasetsFromRootDs(IDataStockMetaData dataStock, Integer startIndex, Integer pageSize) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query query = em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.rootDataStock.id=:dsId");

        query.setParameter("dsId", dataStock.getId());
        if (startIndex != null)
            query.setFirstResult(startIndex);
        if (pageSize != null)
            query.setMaxResults(pageSize);

        return query.getResultList();
    }

    private long getDatasetsFromRootDsCount(IDataStockMetaData dataStock) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query query = em.createQuery("SELECT COUNT(a) FROM " + this.getJpaName() + " a WHERE a.rootDataStock.id=:dsId");

        query.setParameter("dsId", dataStock.getId());

        Long resultCount = (Long) query.getSingleResult();

        return resultCount.longValue();
    }

    /**
     * Get all other versions of this data set
     *
     * @return version instances of all version numbers that are available for
     * the uuid of this data set except the current one
     */
    @SuppressWarnings("unchecked")
    public List<T> getOtherVersions(T current) {
        if (current == null) {
            return null;
        }

        final String uuid = current.getUuidAsString();
        final DataSetVersion v = current.getVersion();

        if (uuid == null || v == null) {
            return null;
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            Query q = em.createQuery("select a from " + this.getJpaName()
                    + " a WHERE a.uuid.uuid=:uuid AND (a.version.majorVersion<>:major OR a.version.minorVersion<>:minor OR a.version.subMinorVersion<>:subMinor) ORDER BY a.version.majorVersion desc, a.version.minorVersion desc, a.version.subMinorVersion desc");

            q.setParameter("uuid", uuid);
            q.setParameter("major", v.getMajorVersion());
            q.setParameter("minor", v.getMinorVersion());
            q.setParameter("subMinor", v.getSubMinorVersion());

            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    //
    // /**
    // * Create query (with set parameters) for {@link
    // #getByClass(String,String)} and
    // * {@link #getNumberByClass(String,String)}
    // *
    // * @param mainClass
    // * the main class to get data sets by
    // * @param subClass
    // * the sub class to get data sets by
    // * @param count
    // * do count query or not
    // * @return query (with set parameters) for {@link
    // #getByClass(String,String)} and
    // * {@link #getNumberByClass(String,String)}
    // */
    // private Query getByClassQuery( String mainClass, String subClass, boolean
    // count ) {
    // EntityManager em = PersistenceUtil.getEntityManager();
    // return em
    // .createQuery(
    // "select "
    // + (count ? "count(a)" : "a")
    // + " from "
    // + this.getJpaName()
    // +
    // " a join a.classification.classes cl join a.classification.classes cl2
    // where cl.level=0 and cl.name=:mainClass and cl2.level=1 and
    // cl2.name=:subClass"
    // )
    // .setParameter( "mainClass", mainClass ).setParameter( "subClass",
    // subClass );
    // }

    /**
     * Determine if a data set is assigned to the provided data stock
     *
     * @param id        ID of the data set as string
     * @param stockMeta meta data of the data stock (root or non-root)
     * @return <code>true</code> if data set is assigned to the provided data
     * stock, <code>false</code> otherwise
     */
    public boolean isInDatastockById(String id, IDataStockMetaData stockMeta) {
        try {
            return this.isInDatastockById(Long.parseLong(id), stockMeta);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a data set is assigned to the provided data stock
     *
     * @param id        ID of the data set
     * @param stockMeta meta data of the data stock (root or non-root)
     * @return <code>true</code> if data set is assigned to the provided data
     * stock, <code>false</code> otherwise
     */
    public boolean isInDatastockById(long id, IDataStockMetaData stockMeta) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            String queryString = "SELECT count(a) FROM " + this.getJpaName() + " a";
            if (stockMeta.isRoot()) {
                queryString += " WHERE a.rootDataStock.id=:stockId";
            } else {
                queryString += " LEFT JOIN a.containingDataStocks ads WHERE ads.id=:stockId";
            }
            queryString += " AND a.id=:dataSetId";

            Query q = em.createQuery(queryString);
            q.setParameter("dataSetId", id);
            q.setParameter("stockId", stockMeta.getId());

            return (Long) q.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determine if a data set is assigned to the provided data stock
     *
     * @param uuid      UUID of the data set
     * @param stockMeta meta data of the data stock (root or non-root)
     * @return <code>true</code> if data set is assigned to the provided data
     * stock, <code>false</code> otherwise
     */
    public boolean isInDatastockByUuid(String uuid, IDataStockMetaData stockMeta) {
        return this.isInDatastockByUuid(uuid, null, stockMeta);
    }

    /**
     * Determine if a data set is assigned to the provided data stock
     *
     * @param uuid      UUID of the data set
     * @param version   version of the data set (may be <code>null</code>)
     * @param stockMeta meta data of the data stock (root or non-root)
     * @return <code>true</code> if data set is assigned to the provided data
     * stock, <code>false</code> otherwise
     */
    public boolean isInDatastockByUuid(String uuid, DataSetVersion version, IDataStockMetaData stockMeta) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            final boolean doVersion = DataSetVersion.isNotBlank(version);

            String queryString = "SELECT count(a) FROM " + this.getJpaName() + " a";
            if (stockMeta.isRoot()) {
                queryString += " WHERE a.rootDataStock.id=:stockId";
            } else {
                queryString += " LEFT JOIN a.containingDataStocks ads WHERE ads.id=:stockId";
            }
            queryString += " AND a.uuid.uuid=:dataSetUuid";

            if (doVersion) {
                queryString += " AND  a.version.majorVersion=:major AND a.version.minorVersion=:minor AND a.version.subMinorVersion=:subMinor";
            }

            Query q = em.createQuery(queryString);
            q.setParameter("dataSetUuid", uuid);
            q.setParameter("stockId", stockMeta.getId());
            if (doVersion) {
                q.setParameter("major", version.getMajorVersion());
                q.setParameter("minor", version.getMinorVersion());
                q.setParameter("subMinor", version.getSubMinorVersion());
            }

            return (Long) q.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the data set of type T by JPA id
     *
     * @param id id of the data set
     * @return data set of type T by JPA id
     */
    public T getByDataSetId(long id) {
        return super.getById(id);
    }

    /**
     * Get the data set of type T by JPA id
     *
     * @param id id of the data set as string
     * @return data set of type T by JPA id
     */
    public T getByDataSetId(String id) {
        return super.getById(id);
    }

    /**
     * Get the top classes from all category systems for the data set type T
     *
     * @return top classes for the data set type T
     */
    public List<ClClass> getTopClasses(IDataStockMetaData[] stocks) {
        ClassificationDao classDao = new ClassificationDao();
        return classDao.getTopClasses(this.dataSetType, stocks);
    }

    /**
     * Get the top classes for the data set type T in a specific classification
     * system
     *
     * @param classificationSystem name of classification system
     * @param params
     * @return top classes for the data set type T
     */
    public List<ClClass> getTopClasses(String classificationSystem, ParameterTool params, IDataStockMetaData[] stocks) {
        ClassificationDao classDao = new ClassificationDao();
        return classDao.getTopClasses(this.dataSetType, classificationSystem, params, stocks);
    }

    /**
     * Get the sub classes for provided class name and level
     *
     * @param className class name
     * @param level     level
     * @return sub classes for provided class name and level
     */
    public List<ClClass> getSubClasses(String className, String level, boolean mostRecentVersionOnly,
                                       IDataStockMetaData[] stocks) {
        return this.getSubClasses(null, className, level, mostRecentVersionOnly, stocks);
    }

    /**
     * Get the sub classes for provided classification system, class name and
     * level
     *
     * @param classificationSystem classification system name
     * @param className            class name
     * @param levelStr             level
     * @return sub classes for provided class name and level
     */
    public List<ClClass> getSubClasses(String classificationSystem, String className, String levelStr,
                                       boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        ClassificationDao cDao = new ClassificationDao();
        int level = Integer.parseInt(levelStr);
        if (classificationSystem == null) {
            List<ClClass> result = new ArrayList<ClClass>();
            for (String classSys : cDao.getCategorySystemsOfTopLevelClass(this.dataSetType, className)) {
                for (ClClass subclass : cDao.getSubClasses(this.dataSetType, classSys, className, level,
                        mostRecentVersionOnly, stocks)) {
                    if (!result.contains(subclass)) {
                        result.add(subclass);
                    }
                }
            }
            return result;
        } else {
            return cDao.getSubClasses(this.dataSetType, classificationSystem, className, level, mostRecentVersionOnly,
                    stocks);
        }
    }

    /**
     * Get the sub classes for provided classification system and path of
     * classes
     *
     * @param classificationSystem classification system name
     * @param classes              path of classes
     * @param stocks               data stocks
     * @return sub classes for provided path of classes
     */
    public List<ClClass> getSubClasses(String classificationSystem, List<String> classes, boolean mostRecentVersionOnly,
                                       IDataStockMetaData... stocks) {
        ClassificationDao cDao = new ClassificationDao();
        return cDao.getSubClasses(this.dataSetType, classificationSystem, classes, null, mostRecentVersionOnly, stocks);
    }

    /**
     * Get the sub classes for provided classification system and path of
     * classes
     *
     * @param classificationSystem classification system name
     * @param classes              path of classes
     * @param stocks               data stocks
     * @return sub classes for provided path of classes
     */
    public List<ClClass> getSubClasses(String classificationSystem, List<String> classes, ValueParser params, boolean mostRecentVersionOnly,
                                       IDataStockMetaData... stocks) {
        ClassificationDao cDao = new ClassificationDao();
        return cDao.getSubClasses(this.dataSetType, classificationSystem, classes, params, mostRecentVersionOnly, stocks);
    }

    /**
     * Get data sets of type T by specified CLID of {@link ClClass}
     *
     * @param clid   CLID of {@link ClClass}
     * @param stocks data stocks to get from
     * @return data sets of type T by specified CLID of {@link ClClass}
     */
    @SuppressWarnings("unchecked")
    public List<T> getByClassClid(String clid, IDataStockMetaData... stocks) {
        EntityManager em = PersistenceUtil.getEntityManager();
        List<String> wheres = new ArrayList<String>();
        List<String> joins = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        joins.add("JOIN a.classifications cl");
        joins.add("JOIN cl.classes clz");

        wheres.add("clz.clId=:clId");
        paramMap.put("clId", clid);

        if (stocks != null) {
            List<String> stockClauses = new ArrayList<String>();
            List<String> rootStockClauses = new ArrayList<String>();
            List<Long> dsIds = new ArrayList<Long>();
            boolean dsJoinDone = false;
            for (IDataStockMetaData m : stocks) {
                // root data stock
                if (m.isRoot()) {
                    String paramName = "rootDsId" + Long.toString(m.getId());
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
                stockClauses.add("(" + this.join(rootStockClauses, " OR ") + ")");
            }
            if (dsJoinDone && !dsIds.isEmpty()) {
                stockClauses.add("ads.id IN(" + StringUtils.join(dsIds, ',') + ")");
            }
            if (!stockClauses.isEmpty()) {
                wheres.add("(" + StringUtils.join(stockClauses, " OR ") + ")");
            }
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT a FROM ").append(this.getJpaName()).append(" a ")
                .append(StringUtils.join(joins, " "));
        queryBuilder.append(" WHERE ");
        queryBuilder.append(StringUtils.join(wheres, " AND "));

        Query q = em.createQuery(queryBuilder.toString(), String.class);
        for (Map.Entry<String, Object> param : paramMap.entrySet()) {
            q.setParameter(param.getKey(), param.getValue());
        }

        return q.getResultList();
    }

    /**
     * Get data sets of type T by main class
     *
     * @param mainClass the main class to get data sets by
     * @param stocks    data stocks to get from
     * @return data sets of type T by main class
     */
    @SuppressWarnings("unchecked")
    public List<T> getByClass(String mainClass, IDataStockMetaData[] stocks, String language, boolean langFallback,
                              boolean mostRecentVersionOnly) {
        List<String> tmp = new ArrayList<String>();
        tmp.add(mainClass);
        List<T> lstName = this.getByClassQuery(ConfigurationService.INSTANCE.getDefaultClassificationSystem(), tmp, false, null, null, true,
                stocks, language, langFallback, mostRecentVersionOnly, false).getResultList();
        List<T> lstEmpty = this.getByClassQuery("", tmp, false, null, null, true, stocks, language, langFallback, mostRecentVersionOnly, false)
                .getResultList();

        lstName.addAll(lstEmpty);

        return lstName;
    }

    /**
     * Get number of data sets of type T by main class
     *
     * @param mainClass the main class to get data sets by
     * @param stocks    data stocks to get from
     * @return number of data sets of type T by main class
     */
    public long getNumberByClass(String mainClass, IDataStockMetaData[] stocks, String language, boolean langFallback,
                                 boolean mostRecentVersionOnly) {
        List<String> tmp = new ArrayList<String>();
        tmp.add(mainClass);
        long defName = (Long) this.getByClassQuery(ConfigurationService.INSTANCE.getDefaultClassificationSystem(), tmp, false, null, null, true,
                stocks, language, langFallback, mostRecentVersionOnly, true).getSingleResult();
        long emptyName = (Long) this
                .getByClassQuery("", tmp, false, null, null, true, stocks, language, langFallback, mostRecentVersionOnly, true)
                .getSingleResult();
        return defName + emptyName;
    }

    /**
     * Get data sets of type T by main class
     *
     * @param classes    the classes to get data sets by
     * @param startIndex start index
     * @param pageSize   page size
     * @param catSystem  category system name
     * @param sortOrder
     * @param sortBy
     * @param params
     * @param search
     * @param stocks     data stocks to get from
     * @return data sets of type T by classes
     */
    @SuppressWarnings("unchecked")
    public List<T> getByClass(String catSystem, List<String> classes, boolean search, ValueParser params, String sortBy, boolean sortOrder, IDataStockMetaData[] stocks, String language,
                              boolean langFallback, boolean mostRecentVersionOnly, int startIndex, int pageSize) {
        Query q = this.getByClassQuery(catSystem, classes, search, params, sortBy, sortOrder, stocks, language, mostRecentVersionOnly, langFallback,
                false);
        q.setFirstResult(startIndex);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    /**
     * Get number of data sets of type T by main class
     *
     * @param classes   the classes to get data sets by
     * @param catSystem category system name
     * @param stocks    data stocks to get from
     * @return number of data sets of type T by classes
     */
    public long getNumberByClass(String catSystem, List<String> classes, boolean search, ValueParser params, IDataStockMetaData[] stocks, String language,
                                 boolean langFallback, boolean mostRecentVersionOnly) {
        return (Long) this
                .getByClassQuery(catSystem, classes, search, params, null, true, stocks, language, langFallback, mostRecentVersionOnly, true)
                .getSingleResult();
    }

    /**
     * Get data sets of type T by main and sub class
     *
     * @param mainClass the main class to get data sets by
     * @param subClass  the sub class to get data sets by
     * @param stocks    data stocks to get from
     * @return data sets of type T by main and sub class
     */
    @SuppressWarnings("unchecked")
    public List<T> getByClass(String mainClass, String subClass, IDataStockMetaData[] stocks, String language,
                              boolean langFallback, boolean mostRecentVersionOnly) {
        List<String> tmp = new ArrayList<String>();
        tmp.add(mainClass);
        tmp.add(subClass);
        List<T> lstName = this.getByClassQuery(ConfigurationService.INSTANCE.getDefaultClassificationSystem(), tmp, false, null, null, true,
                stocks, language, langFallback, mostRecentVersionOnly, false).getResultList();
        List<T> lstEmpty = this.getByClassQuery("", tmp, false, null, null, true, stocks, language, langFallback, mostRecentVersionOnly, false)
                .getResultList();

        lstName.addAll(lstEmpty);

        return lstName;
    }

    /**
     * Get data sets of type T by main and sub class
     *
     * @param mainClass the main class to get data sets by
     * @param subClass  the sub class to get data sets by
     * @param stocks    data stocks to get from
     * @return data sets of type T by main and sub class
     */
    public long getNumberByClass(String mainClass, String subClass, IDataStockMetaData[] stocks, String language,
                                 boolean langFallback, boolean mostRecentVersionOnly) {
        List<String> categories = new ArrayList<String>();
        categories.add(mainClass);
        categories.add(subClass);
        long defName = (Long) this.getByClassQuery(ConfigurationService.INSTANCE.getDefaultClassificationSystem(), categories,
                mostRecentVersionOnly, null, language, mostRecentVersionOnly, stocks, language, langFallback, mostRecentVersionOnly, true).getSingleResult();
        long emptyName = (Long) this
                .getByClassQuery("", categories, false, null, language, mostRecentVersionOnly, stocks, language, langFallback, mostRecentVersionOnly, true)
                .getSingleResult();
        return defName + emptyName;
    }

    private String addByClassClauses(String catSystem, List<String> categories, List<String> wheres, Map<String, Object> paramMap) {

        if (categories == null || CollectionUtils.isEmpty(categories))
            return "";

        if (StringUtils.isBlank(catSystem)) {
            catSystem = ConfigurationService.INSTANCE.getDefaultClassificationSystem();
        }

        int level = 0;

        String joins = (" JOIN a.classifications cl");

        wheres.add("cl.name=:catSystem ");
        paramMap.put("catSystem", catSystem);

        for (String catName : categories) {
            String alias = "clz" + Integer.toString(level);
            String nameParam = "name" + Integer.toString(level);
            joins += (" JOIN cl.classes " + alias);
            wheres.add(alias + ".level=" + Integer.toString(level));
            wheres.add(alias + ".name=:" + nameParam);
            paramMap.put(nameParam, catName);
            level++;
        }

        return joins;
    }

    /**
     * Create query (with set parameters) for {getByClass(String,String)}
     * and {#getNumberByClass(String,String)}
     *
     * @param categories the categories list
     * @param count      do count query or not
     * @param stocks     data stocks to get from
     * @param catSystem  category system name
     * @return query (with set parameters) for
     * {#getByClass(String,String)} and
     * {#getNumberByClass(String,String)}
     */
    private Query getByClassQuery(String catSystem, List<String> categories, boolean search, ValueParser params, String sortBy, boolean sortOrder, IDataStockMetaData[] stocks,
                                  String language, boolean langFallback, boolean mostRecentVersionOnly, boolean count) {

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("getByClassQuery, datastock " + (stocks[0].getName()));

        return createQueryObject(params, catSystem, categories, sortBy, sortOrder, count, mostRecentVersionOnly, stocks, null);
    }

    /**
     * Search for data sets of type T
     *
     * @param params                parameter
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return found data sets of type T
     */
    public List<L> search(ValueParser params, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.search(this.listVOClass, params, mostRecentVersionOnly, stocks);
    }

    /**
     * Search for data sets of type T
     *
     * @param params                parameter
     * @param startIndex            start index for query
     * @param pageSize              count of result items
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return found data sets of type T
     */
    public List<L> search(ValueParser params, int startIndex, int pageSize, boolean mostRecentVersionOnly,
                          IDataStockMetaData[] stocks) {
        return this.search(this.listVOClass, params, startIndex, pageSize, null, mostRecentVersionOnly, stocks);
    }

    /**
     * Invoked at beginning of
     * {@link #checkAndPersist(DataSet, PersistType, PrintWriter)} to manipulate
     * data prior to persisting
     *
     * @param dataSet data set to manipulate
     * @see #checkAndPersist(DataSet, PersistType, PrintWriter)
     */
    protected abstract void preCheckAndPersist(T dataSet);

    /**
     * Generic persist method for data set objects.
     *
     * @param dataSet data set to persist
     * @param pType   which type of persistence operation, new, update (i.e.
     *                overwrite existing data set), ...
     * @param out     PrintWriter to log error messages which can be presented to
     *                the end user
     * @return true if persist is successful, false otherwise
     * @see #preCheckAndPersist(DataSet)
     */
    public boolean checkAndPersist(T dataSet, PersistType pType, PrintWriter out) {
        // TODO: check if version shall be excluded for some types

        if (dataSet.getRootDataStock() == null) {
            out.println("Error: root data stock must not be null!");
            return false;
        }

        // perform pre-persist actions
        this.preCheckAndPersist(dataSet);

        EntityManager em = PersistenceUtil.getEntityManager();

        // locate existing method by UUID
        T existingDataSet = this.getByUuidAndVersion(dataSet.getUuidAsString(), dataSet.getVersion());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(" ds exists? uuid: " + dataSet.getUuidAsString() + " version: " + dataSet.getVersion() + "   " + (existingDataSet != null) + " " + pType.name());

        // if existing found ...
        if (existingDataSet != null) {
            // ... and mode is set to only new ...
            if (PersistType.ONLYNEW.equals(pType)) {
                if (out != null) {
                    out.println("Warning: " + this.getAccessedClass().getSimpleName() + " data set with uuid "
                            + dataSet.getUuidAsString() + " and version " + dataSet.getVersion().getVersionString()
                            + " already exists in database; will ignore this data set.");
                }
                // ... just ignore it
                return false;
            }
        }

        EntityTransaction t = em.getTransaction();
        // now the DB interaction
        try {
            t.begin();
            // delete existing for merge operation
            if (existingDataSet != null && PersistType.MERGE.equals(pType)) {
                if (out != null) {
                    out.println("Notice: " + this.getAccessedClass().getSimpleName() + " method data set with uuid "
                            + existingDataSet.getUuidAsString() + " and version "
                            + existingDataSet.getVersion().getVersionString()
                            + " already exists in database; will replace it with this data set");
                }
                em.remove(existingDataSet);
            }

            // mark related data stocks as dirty for export
            LOGGER.debug("marking datastocks as dirty");
            markDataStocksDirty(dataSet);

            // persist the new method
            em.merge(dataSet);

            // actual write to DB
            t.commit();

            // set the most recent version flags correctly
            if (!this.setMostRecentVersionFlags(dataSet.getUuidAsString())) {
                return false;
            }

            // and return with success :)
            return true;
        } catch (RollbackException e) {
            if (out != null) {
                LOGGER.warn("Exception during storing object, most likely it already exists: ", e);
                out.println("Warning: " + this.getAccessedClass().getSimpleName() + " data set with uuid "
                        + dataSet.getUuidAsString() + " and version " + dataSet.getVersion().getVersionString()
                        + " already exists in database; will ignore this data set.");
            }
            // ... just ignore it
            return false;
        } catch (Exception e) {
            DataSetDao.LOGGER.error(
                    "Cannot persist " + this.getAccessedClass().getSimpleName() + " data set with uuid {}",
                    dataSet.getUuidAsString());
            DataSetDao.LOGGER.error("Exception is: ", e);
            if (out != null) {
                out.println("Error: " + this.getAccessedClass().getSimpleName() + " data set with uuid "
                        + dataSet.getUuidAsString() + " and version " + dataSet.getVersion().getVersionString()
                        + " could not be saved into database; unknown database error; Exception message: "
                        + e.getMessage());
            }
            t.rollback();
        }

        return false;
    }

    private void markDataStocksDirty(T dataSet) throws MergeException {
        dataSet.getRootDataStock().setModified(true);

        for (DataStock dataStock : dataSet.getContainingDataStocks()) {
            dataStock.setModified(true);
        }

        CommonDataStockDao dataStockDao = new CommonDataStockDao();
        dataStockDao.merge(dataSet.getRootDataStock());

        for (DataStock ds : dataSet.getContainingDataStocks()) {
            dataStockDao.merge(ds);
        }
    }

    /**
     * Set the {@link DataSet#setMostRecentVersion(boolean) most recent version
     * flags} for all data sets with the specified uuid. Please note that this
     * method <strong>does not open a separate transaction</strong>.
     *
     * @param uuid uuid of the data sets
     */
    @SuppressWarnings("unchecked")
    protected boolean setMostRecentVersionFlags(String uuid) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();
        try {
            Query q = em.createQuery("SELECT a FROM " + this.getJpaName()
                    + " a WHERE a.uuid.uuid=:uuid ORDER BY a.version.majorVersion desc, a.version.minorVersion desc, a.version.subMinorVersion desc");
            q.setParameter("uuid", uuid);
            List<T> res = q.getResultList();
            if (!res.isEmpty()) {
                t.begin();
                // get first element and mark it as most recent version (correct
                // order is taken care of in query!)
                T tmp = res.get(0);
                tmp.setMostRecentVersion(true);
                tmp = em.merge(tmp);

                // set "false" for all other elements if required
                final int size = res.size();
                if (size > 1) {
                    for (int i = 1; i < size; i++) {
                        tmp = res.get(i);
                        if (tmp.isMostRecentVersion()) {
                            tmp.setMostRecentVersion(false);
                            tmp = em.merge(tmp);
                        }
                    }
                }
                t.commit();
                return true;
            } else {
                DataSetDao.LOGGER.warn("Most recent version flag was called for non-existent UUID: " + uuid);
                return false;
            }
        } catch (Exception e) {
            DataSetDao.LOGGER.error("Could not set most recent version flag for UUID: " + uuid, e);
            if (t.isActive()) {
                t.rollback();
            }
            return false;
        }
    }

    /**
     * This method (local serach) works as search method for datasets only on
     * local database entities, i.e. Process, Flow, ... To use a search method
     * which also works distributed use the corresponding search method instead
     *
     * @param <T>    Class name of &quot;type&quot; of objects to return, i.e.
     *               Process, Flow, ...
     * @param params search parameters as key-value pairs
     * @param stocks stocks
     * @return list of datasets of type T
     */
    public List<T> lsearch(ValueParser params, IDataStockMetaData[] stocks) {
        return this.lsearch(params, 0, 0, null, true, stocks);
    }

    /**
     * This method (local lsearch) works as lsearch method for datasets only on
     * local database entities, i.e. Process, Flow, ... To use a lsearch method
     * which also works distributed use the corresponding lsearch method
     *
     * @param <T>                   Class name of &quot;type&quot; of objects to return, i.e.
     *                              Process, Flow, ...
     * @param params                lsearch parameters as key-value pairs
     * @param startPosition         start index within the whole list of lsearch results
     * @param pageSize              pages size, i.e. maximum count of results to return
     * @param sortCriterium         name of the objects field which should be used for sorting
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return list of datasets of type T matching the lsearch criteria
     */
    public List<T> lsearch(ValueParser params, int startPosition, int pageSize, String sortCriterium,
                           boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.lsearch(params, startPosition, pageSize, sortCriterium, true, mostRecentVersionOnly, stocks);
    }

    // ---------------------------------------

    /**
     * This method (local lsearch) works as lsearch method for datasets only on
     * local database entities, i.e. Process, Flow, ... To use a lsearch method
     * which also works distributed use the corresponding lsearch method
     *
     * @param <T>                   Class name of &quot;type&quot; of objects to return, i.e.
     *                              Process, Flow, ...
     * @param params                lsearch parameters as key-value pairs
     * @param startPosition         start index within the whole list of lsearch results
     * @param pageSize              pages size, i.e. maximum count of results to return
     * @param sortCriterium         name of the objects field which should be used for sorting
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return list of datasets of type T matching the lsearch criteria
     */
    public List<T> lsearch(ValueParser params, int startPosition, int pageSize, String sortCriterium,
                           boolean mostRecentVersionOnly, IDataStockMetaData[] stocks, IDataStockMetaData excludeStock) {
        return this.lsearch(params, startPosition, pageSize, sortCriterium, true, mostRecentVersionOnly, stocks,
                excludeStock);
    }

    /**
     * This method (local lsearch) works as lsearch method for datasets only on
     * local database entities, i.e. Process, Flow, ... To use a lsearch method
     * which also works distributed use the corresponding lsearch method
     *
     * @param <T>                   Class name of &quot;type&quot; of objects to return, i.e.
     *                              Process, Flow, ...
     * @param params                lsearch parameters as key-value pairs
     * @param startPosition         start index within the whole list of lsearch results
     * @param pageSize              pages size, i.e. maximum count of results to return
     * @param sortCriterium         name of the objects field which should be used for sorting
     * @param ascending             define ordering of result
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return list of datasets of type T matching the lsearch criteria
     */
    public List<T> lsearch(ValueParser params, int startPosition, int pageSize, String sortCriterium, boolean ascending,
                           boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.lsearch(params, startPosition, pageSize, sortCriterium, ascending, mostRecentVersionOnly, stocks,
                null);
    }

    /**
     * This method (local lsearch) works as lsearch method for datasets only on
     * local database entities, i.e. Process, Flow, ... To use a lsearch method
     * which also works distributed use the corresponding lsearch method
     *
     * @param <T>                   Class name of &quot;type&quot; of objects to return, i.e.
     *                              Process, Flow, ...
     * @param params                lsearch parameters as key-value pairs
     * @param startPosition         start index within the whole list of lsearch results
     * @param pageSize              pages size, i.e. maximum count of results to return
     * @param sortCriterium         name of the objects field which should be used for sorting
     * @param ascending             define ordering of result
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @param excludeStock          stock that shall be excluded from the search (may be
     *                              <code>null</code>)
     * @return list of datasets of type T matching the lsearch criteria
     */
    @SuppressWarnings("unchecked")
    public List<T> lsearch(ValueParser params, int startPosition, int pageSize, String sortCriterium, boolean ascending,
                           boolean mostRecentVersionOnly, IDataStockMetaData[] stocks, IDataStockMetaData excludeStock) {
        List<T> dataSets = null;

        Query query = this.createQueryObject(params, null, null, sortCriterium,
                ascending, false, mostRecentVersionOnly, stocks,
                excludeStock);

        if (startPosition > 0) {
            query.setFirstResult(startPosition);
        }
        if (pageSize > 0) {
            query.setMaxResults(pageSize);
        }

        dataSets = query.getResultList();

        return dataSets;
    }

    /**
     * This search method can also be used for distributed search because it
     * uses the common interface types of the ServiceAPI For a version without
     * the dataSetClassType parameter look in the subclasses
     *
     * @param <E>                   interface name of &quot;type&quot; of objects to return, i.e.
     *                              IProcessListVO, IFlowListVo, ...
     * @param dataSetClassType      Class object of T, i.e. IProcessListVO.class
     * @param params                search parameter
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return List of objects of the give interface type E
     */
    public <E extends IDataSetListVO> List<E> search(Class<E> dataSetClassType, ValueParser params,
                                                     boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.search(dataSetClassType, params, 0, 0, null, mostRecentVersionOnly, stocks);
    }

    /**
     * This search method can also be used for distributed search because it
     * uses the common interface types of the ServiceAPI
     *
     * @param <E>                   interface name of &quot;type&quot; of objects to return, i.e.
     *                              IProcessListVO, IFlowListVo, ...
     * @param dataSetClassType      Class object of T, i.e. IProcessListVO.class
     * @param params                search parameter as ParameterTool object
     * @param startPosition         start index for first search result
     * @param pageSize              size of one page of search results
     * @param sortCriterium         field name of object which is used for ordering
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @return list of objects of the given interface E
     */
    public <E extends IDataSetListVO> List<E> search(Class<E> dataSetClassType, ValueParser params, int startPosition,
                                                     int pageSize, String sortCriterium, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.searchDist(dataSetClassType, params, startPosition, pageSize, sortCriterium, true,
                null, false, mostRecentVersionOnly, stocks, null, null);
    }

    public <E extends IDataSetListVO> List<E> search(Class<E> dataSetClassType, ValueParser params, int startPosition,
                                                     int pageSize, String sortCriterium, String language, boolean langFallback, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.searchDist(dataSetClassType, params, startPosition, pageSize, sortCriterium, true,
                language, langFallback, mostRecentVersionOnly, stocks, null, null);
    }

    public <E extends IDataSetListVO> List<E> search(Class<E> dataSetClassType, ValueParser params, int startPosition,
                                                     int pageSize, String sortCriterium, boolean sortOrder, boolean mostRecentVersionOnly,
                                                     IDataStockMetaData[] stocks) {
        return this.searchDist(dataSetClassType, params, startPosition, pageSize, sortCriterium,
                sortOrder, null, false, mostRecentVersionOnly, stocks, null, null);
    }

    public <E extends IDataSetListVO> List<E> search(Class<E> dataSetClassType, ValueParser params, int startPosition,
                                                     int pageSize, String sortCriterium, boolean sortOrder, String language, boolean langFallback, boolean mostRecentVersionOnly,
                                                     IDataStockMetaData[] stocks, SearchResultCount searchResultCount) {
        return this.searchDist(dataSetClassType, params, startPosition, pageSize, sortCriterium,
                (sortOrder), language, langFallback, mostRecentVersionOnly, stocks, null, null, searchResultCount);
    }

    public <E extends IDataSetListVO> List<E> searchDist(Class<E> dataSetClassType, ValueParser params,
                                                         int startPosition, int pageSize, String sortCriterium, boolean sortOrder, String language, boolean langFallback, boolean mostRecentVersionOnly,
                                                         IDataStockMetaData[] stocks, IDataStockMetaData excludeStock, DistributedSearchLog log) {
        return searchDist(dataSetClassType, params, startPosition, pageSize, sortCriterium, sortOrder, language, langFallback, mostRecentVersionOnly, stocks, excludeStock, log, null);
    }

    /**
     * This search method can also be used for distributed search because it
     * uses the common interface types of the ServiceAPI
     *
     * @param <E>                   interface name of &quot;type&quot; of objects to return, i.e.
     *                              IProcessListVO, IFlowListVo, ...
     * @param dataSetClassType      Class object of T, i.e. IProcessListVO.class
     * @param params                search parameter as ParameterTool object
     * @param startPosition         start index for first search result
     * @param pageSize              size of one page of search results
     * @param sortCriterium         field name of object which is used for ordering
     * @param sortOrder             define the ordering of the result
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                stocks
     * @param excludeStock          stocks to exclude from the search
     * @param log                   a log object for distributed search
     * @param searchResultCount     the total number of items retrieved by the query before pagination is applied
     * @return list of objects of the given interface E
     */
    @SuppressWarnings("unchecked")
    public <E extends IDataSetListVO> List<E> searchDist(Class<E> dataSetClassType, ValueParser params,
                                                         int startPosition, int pageSize, String sortCriterium, Boolean sortOrder, String language, boolean langFallback, boolean mostRecentVersionOnly,
                                                         IDataStockMetaData[] stocks, IDataStockMetaData excludeStock, DistributedSearchLog log, SearchResultCount searchResultCount) {

        // avoid a lot of null checks
        if (params == null) {
            params = new ValueParser();
        }

        LOGGER.trace("searching for {}", dataSetClassType.getName());

        List<E> dataSets = new ArrayList<E>();

        if (!(ConfigurationService.INSTANCE.isProxyMode() && Process.class.equals(dataSetClassType))) {
            Query query = this.createQueryObject(params, null, null, sortCriterium, sortOrder, false, mostRecentVersionOnly, stocks,
                    excludeStock);

            if (params.exists("distributed") && params.getBoolean("distributed")) {
                query.setMaxResults(DIST_MAX_RESULTS);
            } else {
                if (startPosition > 0) {
                    query.setFirstResult(startPosition);
                }
                if (pageSize > 0) {
                    query.setMaxResults(pageSize);
                }
            }

            dataSets = query.getResultList();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(dataSets.size() + " local results");
                for (IDataSetListVO ds : dataSets) {
                    LOGGER.trace("name: {}, defaultname: {}", ds.getName().getValue(), ds.getDefaultName());
                }
            }

            // root data stock ids are only needed if augmentation is enabled
            if (DataSetType.PROCESS.equals(dataSetType) && ConfigurationService.INSTANCE.isAugmentRegistrationAuthority())
                augmentRegistrationAuthoritiesOnProcesses((List<IProcessListVO>) dataSets);
        }

        // OK, search also in other systems
        if (params.exists("distributed") && params.getBoolean("distributed")) {
            // add full URI to local results
            if (!(ConfigurationService.INSTANCE.isProxyMode() && dataSetClassType.equals(Process.class))) {
                addNodeId(dataSets);
            }

            LOGGER.debug("initiating distributed search");
            ForeignDataSetsHelper foreignHelper = new ForeignDataSetsHelper(this);
            List<E> foreignProcesses = foreignHelper.foreignSearch(dataSetClassType, params, log);
            if (foreignProcesses != null) {
                LOGGER.debug("returning {} results from foreign nodes, " + (foreignHelper.isCachedResult() ? "from cache" : "fresh query"), foreignProcesses.size());
            }
            dataSets.addAll(foreignProcesses);

            // filter for nodeId
            dataSets = filter(dataSets, params.getStrings("nodeid"));

            // process dupes every time only if not in proxy mode
            // otherwise this happens in ForeignDataSetsHelper before cachin the result
            if (!ConfigurationService.INSTANCE.isProxyMode())
                dataSets = this.processDupes((List<IDataSetListVO>) dataSets);

            // now sort results
            this.sort(dataSets, sortCriterium, sortOrder, language);

            if (searchResultCount != null)
                searchResultCount.setValue(dataSets.size());

            // and apply pagination
            dataSets = this.paginate(dataSets, pageSize, startPosition);
        }
        return dataSets;
    }

    private void augmentRegistrationAuthoritiesOnProcesses(List<IProcessListVO> processes) {
        if (processes != null && processes.size() > 0) {
            LOGGER.debug("Augmenting registration authorities for {} processes", processes.size());
            for (IProcessListVO p : processes) {
                augmentRegistrationAuthority(p, p.getRootDataStockId());
            }
        }
    }

    private void augmentRegistrationAuthority(IProcessListVO process, String key) {
        Map<String, String[]> augmentationMap = ConfigurationService.INSTANCE.getAugmentRegistrationAuthorityMap();
        final String[] augmentation;
        final var dataStockAugmentation = augmentationMap.get(key);
        final var defaultAugmentation = augmentationMap.get(null);
        augmentation = dataStockAugmentation != null ? dataStockAugmentation : defaultAugmentation;

        if (augmentation != null) {
            final var isOverride = ConfigurationService.AUGMENTATION_MODE_OVERRIDE.equals(augmentation[2]);
            if (isOverride || process.getRegistrationAuthority() == null) {
                GlobalReference reference = new GlobalReference();
                reference.setRefObjectId(augmentation[0]);
                reference.setShortDescription(new MultiLangStringMapAdapter(augmentation[1], "en"));
                ((Process) process).setReferenceToRegistrationAuthority(reference);
            }
        }
    }

    /**
     * This merges all duplicates (items with the same UUID) inside the "duplicates" property of the first
     * item in the list with that UUID.
     *
     * @param dataSets
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends IDataSetListVO> List<E> processDupes(List<IDataSetListVO> dataSets) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("items before dupe processing: " + dataSets.size());

        Map<String, IDataSetListVO> map = new ConcurrentHashMap<String, IDataSetListVO>(dataSets.size());

        for (IDataSetListVO ds : dataSets) {
            String uuid = ds.getUuidAsString();
            if (map.containsKey(uuid)) {
                IDataSetVO existingEntry = (IDataSetVO) map.get(uuid);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("dupes on item before dupe processing: " + existingEntry.getDuplicates().size());

                existingEntry.getDuplicates().clear();

                // we convert instances of Process to ProcessDataSetVO
                if (existingEntry instanceof Process) {
                    map.remove(uuid, existingEntry);
                    existingEntry = new ProcessVOAdapter((IProcessListVO) existingEntry).getDataSet();
                    map.put(uuid, existingEntry);
                }
                if (ds instanceof Process)
                    existingEntry.getDuplicates().add(new ProcessVOAdapter((IProcessListVO) ds).getDataSet());
                else
                    existingEntry.getDuplicates().add((DataSetVO) ds);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("dupes on item after dupe processing: " + existingEntry.getDuplicates().size());

            } else {
                map.put(uuid, ds);
            }
        }

        List<E> result = new ArrayList<E>();
        for (IDataSetListVO o : map.values()) {
            result.add((E) o);
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("items after dupe processing: " + result.size());

        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends IDataSetListVO> void sort(List<E> dataSets, String sortCriterium, boolean sortOrder, String language) {
        Collections.sort(dataSets, new ProcessComparator(sortCriterium, sortOrder, language));
    }

    public <E extends IDataSetListVO> List<E> paginate(List<E> dataSets, final int pageSize, final int startIndex) {
        if (dataSets == null || dataSets.size() == 0 || startIndex > dataSets.size())
            return dataSets;

        int endIndex = startIndex + pageSize;
        if (endIndex > dataSets.size())
            endIndex = dataSets.size();

        ArrayList<E> result = new ArrayList<E>(dataSets.subList(startIndex, endIndex));

        return result;
    }

    public <E extends IDataSetListVO> void addNodeId(List<E> dataSets) {
        String baseURI = ConfigurationService.INSTANCE.getBaseURI().toString();
        for (E ds : dataSets) {
            ds.setHref(baseURI + "/resource/processes/" + ds.getUuidAsString() + "?version=" + ds.getDataSetVersion());
        }
    }

    /**
     * Get result count for lsearch call
     *
     * @param params                lsearch parameters
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                meta data of data stocks
     * @return returns the count of results when a lsearch is issued with this
     * lsearch parameters
     */
    public long searchResultCount(ValueParser params, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks) {
        return this.searchResultCount(params, mostRecentVersionOnly, stocks, null);
    }

    /**
     * Get result count for lsearch call
     *
     * @param params                lsearch parameters
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                meta data of data stocks
     * @param excludeStock          stock that shall be excluded from the search (may be
     *                              <code>null</code>)
     * @return returns the count of results when a lsearch is issued with this
     * lsearch parameters
     */
    public long searchResultCount(ValueParser params, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks,
                                  IDataStockMetaData excludeStock) {
        Query query = this.createQueryObject(params, null, null, null, true, true, mostRecentVersionOnly, stocks,
                excludeStock);

        Long resultCount = (Long) query.getSingleResult();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("returning count " + resultCount);

        return resultCount.longValue();
    }

    /**
     * Get the ORDER BY part of the query. If no match for the sortString is
     * being found, return default value
     *
     * @param typeAlias  alias of the type
     * @param sortString sort string
     * @return ORDER BY part of JPQL query text
     */
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("classes".equals(sortString) || StringUtils.startsWith(sortString, "classification.classHierarchyAsString")) {
            return buildOrderBy(typeAlias, "classificationCache", typeAlias, "nameCache", sortOrder);
        } else if ("rootDataStock.name".equals(sortString)) {
            return buildOrderBy(typeAlias, "rootDataStock.name", typeAlias, "nameCache", sortOrder);
        } else if ("importDate".equals(sortString)) {
            return buildOrderBy(typeAlias, "importDate", typeAlias, "nameCache", sortOrder);
        } else if ("version".equals(sortString)) {
            return buildOrderBy(typeAlias, "version", typeAlias, "nameCache", sortOrder);
        } else if ("mostRecentVersion".equals(sortString)) {
            return buildOrderBy(typeAlias, "mostRecentVersion", typeAlias, "nameCache", sortOrder);
        } else {
            return buildOrderBy(typeAlias, "nameCache", null, null, sortOrder);
        }
    }

    /**
     * <p>
     * Add default where clauses (applying for all dataset types). They will act
     * in conjunction (logic AND link).
     * </p>
     *
     * @param typeAlias        alias of the type
     * @param params           parameters
     * @param whereClauses     add parts for the where clause, please use named parameters
     *                         for user inputs!
     * @param whereParamValues map to add values for named parameters in
     */
    private void addDefaultWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params,
                                                                         List<String> whereClauses, Map<String, Object> whereParamValues) {
        final String classId = params.getString("classId");
        if (StringUtils.isNotBlank(classId)) {
            whereClauses.add("clz.clId=:clzClassId");
            whereParamValues.put("clzClassId", classId);
        }
        final String lang = params.getString("lang");
        if (StringUtils.isNotBlank(lang)) {
            final Boolean langFallback = params.getBoolean("langFallback", false);
            addLangClauses(lang, langFallback, params, whereClauses, whereParamValues);
        }
        final String rootStockName = params.getString("rootDataStock.name");
        if (StringUtils.isNotBlank(rootStockName)) {
            whereClauses.add(typeAlias + ".rootDataStock.name like :rootDataStockName");
            whereParamValues.put("rootDataStockName", "%" + rootStockName + "%");
        }
        final Boolean mostRecentVersionsOnly = params.getBoolean("mostRecentVersion");
        if (mostRecentVersionsOnly != null) {
            whereClauses.add(typeAlias + ".mostRecentVersion = :mostRecent");
            whereParamValues.put("mostRecent", mostRecentVersionsOnly);
        }
        final Boolean visibilityFlagValue = params.getBoolean("visible", null);
        if (visibilityFlagValue != null) {
            whereClauses.add(typeAlias + ".visible = :visibilityFlagValue");
            whereParamValues.put("visibilityFlagValue", visibilityFlagValue);
        }

        addImportDateWhereClausesAndNamedParamesForQueryStringJpql(typeAlias, params, whereClauses, whereParamValues);

    }

    private void addImportDateWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses,
                                                                            Map<String, Object> whereParamValues) {

        String date = (String) params.getValue("importDate");

        if (StringUtils.isBlank(date))
            return;

        if (date.contains(TimeFrame.FROM.getValue()) && date.contains(TimeFrame.UNTIL.getValue())) {

            String startDate = StringUtils.substringBefore(date, "until");
            String endDate = StringUtil.concat("until ", StringUtils.substringAfter(date, "until"));

            Map<String, Object> mStart = new HashMap<String, Object>();
            mStart.put("importDate", startDate);
            ValueParser pStart = new ValueParser(mStart);

            Map<String, Object> mEnd = new HashMap<String, Object>();
            mEnd.put("importDate", endDate);
            ValueParser pEnd = new ValueParser(mEnd);

            addImportDateWhereClausesAndNamedParamesForQueryStringJpql(typeAlias, pStart, whereClauses, whereParamValues);
            addImportDateWhereClausesAndNamedParamesForQueryStringJpql(typeAlias, pEnd, whereClauses, whereParamValues);

            return;
        }

        TimeFrame timeFrame = TimeFrame.EXACT;

        if (date.contains(TimeFrame.UNTIL.getValue())) {
            timeFrame = TimeFrame.UNTIL;
            date = StringUtils.substringAfter(date, TimeFrame.UNTIL.getValue());
        } else if (date.contains(TimeFrame.FROM.getValue())) {
            timeFrame = TimeFrame.FROM;
            date = StringUtils.substringAfter(date, TimeFrame.FROM.getValue());
        }

        date = date.trim();

        if (StringUtils.countMatches(date, ".") == 1) {
            parseDate(date, "d.", timeFrame, true, false, false, typeAlias, whereClauses, whereParamValues);
            parseDate(date, "MM.yyyy", timeFrame, false, true, true, typeAlias, whereClauses, whereParamValues);
        } else if (StringUtils.countMatches(date, ".") == 2) {
            parseDate(date, "d.MM.", timeFrame, true, true, false, typeAlias, whereClauses, whereParamValues);
            parseDate(date, "d.MM.yyyy", timeFrame, true, true, true, typeAlias, whereClauses, whereParamValues);
        }
    }

    // TODO: when updating app requirements to Java 1.8, remove dependency on jodatime
    private void parseDate(String date, String pattern, TimeFrame timeFrame, boolean day, boolean month, boolean year, String typeAlias, List<String> whereClauses,
                           Map<String, Object> whereParamValues) {
        try {

            if (LOGGER.isTraceEnabled())
                LOGGER.trace("trying to parse date " + date + " with pattern " + pattern);

            DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
            DateTime dt = fmt.parseDateTime(date);

            Integer importDateDay = dt.getDayOfMonth();
            Integer importDateMonth = dt.getMonthOfYear();
            Integer importDateYear = dt.getYear();

            // if we filter UNTIL a certain month, we want to set the day to the max days of that month
            if (!day && timeFrame.equals(TimeFrame.UNTIL)) {
                LocalDate lastDayOfMonth = new LocalDate(importDateYear, importDateMonth, 1).dayOfMonth().withMaximumValue();
                dt = lastDayOfMonth.toDateTimeAtStartOfDay();
            }

            if (LOGGER.isTraceEnabled())
                LOGGER.trace(importDateDay + ". " + importDateMonth + ". " + importDateYear);

            switch (timeFrame) {
                case EXACT:
                    if (importDateDay != null && day) {
                        addExactClause(importDateDay, "DAY", timeFrame, typeAlias, whereClauses, whereParamValues);
                    }

                    if (importDateMonth != null && month) {
                        addExactClause(importDateMonth, "MONTH", timeFrame, typeAlias, whereClauses, whereParamValues);
                    }

                    if (importDateYear != null && year) {
                        addExactClause(importDateYear, "YEAR", timeFrame, typeAlias, whereClauses, whereParamValues);
                    }
                    break;
                case FROM:
                case UNTIL:
                    addTimeFrameClause(dt.toString("dd.MM.YYYY"), timeFrame, typeAlias, whereClauses, whereParamValues);
                    break;
            }


        } catch (Exception e) {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("could not parse date " + date + ", pattern is " + pattern + (day ? " day " : "") + (month ? " month " : "") + (year ? " year " : ""));
        }

    }

    private void addExactClause(Integer dateValue, String datePart, TimeFrame timeFrame, String typeAlias, List<String> whereClauses,
                                Map<String, Object> whereParamValues) {

        String operation = "=";

        switch (timeFrame) {
            case EXACT:
                operation = "=";
                break;
            case FROM:
                operation = ">=";
                break;
            case UNTIL:
                operation = "<=";
                break;
        }

        String paramName = StringUtil.concat("importDate", datePart, timeFrame.getValue());
        String clause = StringUtil.concat("FUNCTION('" + datePart + "', " + typeAlias + ".importDate) " + operation + " :" + paramName);
        whereClauses.add(clause);
        whereParamValues.put(paramName, dateValue);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("adding " + datePart + " " + dateValue);
    }

    private void addTimeFrameClause(String date, TimeFrame timeFrame, String typeAlias, List<String> whereClauses,
                                    Map<String, Object> whereParamValues) {

        String operation = null;

        switch (timeFrame) {
            case FROM:
                operation = ">=";
                break;
            case UNTIL:
                operation = "<=";
                break;
            default:
                return;
        }

        String paramName = StringUtil.concat("importDate", timeFrame.getValue());
        String clause = StringUtil.concat("FUNCTION('DATE', " + typeAlias + ".importDate) " + operation + " FUNCTION('STR_TO_DATE',:" + paramName + ", '%d.%m.%Y') ");
        whereClauses.add(clause);
        whereParamValues.put(paramName, date);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("adding " + timeFrame.getValue() + " " + date);
    }

    /**
     * <p>
     * Add all where clauses. They will act in conjunction (logic AND link).
     * </p>
     * <p>
     * <b><u>Please note:</u></b>
     * <ul>
     * <li>Name and description filter will be added by default! Do not add them
     * manually. If name and description filter differ for a concrete class,
     * override
     * {@link #addNameDescWhereClauseAndNamedParamForQueryStringJpql(String, ValueParser, List, Map)}
     * </li>
     * <li>Most recent version condition will be added automatically as well, do
     * not add it manually!</li>
     * </ul>
     * </p>
     *
     * @param typeAlias        alias of the type
     * @param params           parameters
     * @param whereClauses     add parts for the where clause, please use named parameters
     *                         for user inputs!
     * @param whereParamValues map to add values for named parameters in
     */
    protected abstract void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params,
                                                                             List<String> whereClauses, Map<String, Object> whereParamValues);

    /**
     * Add default name and description filter. Override this method if name and
     * description filter shall differ for a concrete type.
     *
     * @param typeAlias        type alias
     * @param params           parameters
     * @param whereClauses     add parts for the where clause, please use named parameters
     *                         for user inputs!
     * @param whereParamValues map to add values for named parameters in
     */
    protected void addNameDescWhereClauseAndNamedParamForQueryStringJpql(String typeAlias, ValueParser params,
                                                                         List<String> whereClauses, Map<String, Object> whereParamValues) {
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
                sb.append(splitMultiContainClause(typeAlias, "nameCache", namePhrase, "AND", whereParamValues));
            }
            if (hasDescParam) {
                if (hasNameParam) {
                    sb.append("OR");
                }
                sb.append(splitMultiContainClause(typeAlias, "description.value", descriptionPhrase, "AND", whereParamValues));
            }
            sb.append(")");
            whereClauses.add(sb.toString());
        }
    }

    /**
     * Get the default joins for the query (for all dataset types).
     *
     * @param params    parameter map
     * @param typeAlias type alias (to prevent collisions)
     * @return joins string
     */
    protected final String getDefaultQueryStringJoinPart(ValueParser params, String typeAlias) {
        final String classId = params.getString("classId");
        final String lang = params.getString("lang");

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(classId)) {
            sb.append("LEFT JOIN ");
            sb.append(typeAlias).append(".classifications cl LEFT JOIN cl.classes clz");
        }
        if (StringUtils.isNotBlank(lang)) {
            sb.append(" LEFT JOIN ");
            sb.append(typeAlias).append(".supportedLanguages slang ");
        }
        return sb.toString();
    }

    /**
     * Get the joins for the query, default is <code>null</code> (which means
     * none). Override method if joins are required.
     *
     * @param params    parameter map
     * @param typeAlias type alias (to prevent collisions)
     * @return joins string
     */
    protected String getQueryStringJoinPart(ValueParser params, String typeAlias, String sortCriterium) {
        return null;
    }


    // ---------------------------------------

    /**
     * <p>
     * Create a query in order to search for data sets. The following methods
     * affect the created query:
     * <ul>
     * <li>{@link #getQueryStringJoinPart(ValueParser, String, String)} (additional
     * joins)</li>
     * <li>
     * {@link #addNameDescWhereClauseAndNamedParamForQueryStringJpql(String, ValueParser, List, Map)}
     * (name and description filter)</li>
     * <li>
     * {@link #addWhereClausesAndNamedParamesForQueryStringJpql(String, ValueParser, List, Map)}
     * (other filters)</li>
     * </ul>
     * Please see their documentation for details. Additionally, the filter for
     * <i>most recent version</i> is being added here, do not add it in a
     * subclass via one of the methods above (you won't get the boolean flag
     * indicating it anyway)!
     * </p>
     * <p>
     * This method is being used by the other generic lsearch functions to issue
     * the special query.
     * </p>
     *
     * @param params                lsearch parameter
     * @param sortCriterium         field of result object which will be used for ordering of
     *                              lsearch results
     * @param sortOrder             enum: ASCENDING, DESCENDING, UNSORTED
     * @param returnCount           if true return count of lsearch result instead of lsearch
     *                              results
     * @param mostRecentVersionOnly flag to indicate if only the most recent version of a data set
     *                              shall be returned if multiple versions exist
     * @param stocks                meta data of data stocks
     * @param excludeStock          stock that shall be excluded from the search (may be
     *                              <code>null</code>)
     * @return JPA Query objects for doing the lsearch
     */
    protected final Query createQueryObject(ValueParser params, String catSystem, List<String> categories, String sortCriterium, Boolean sortOrder,
                                            boolean returnCount, boolean mostRecentVersionOnly, IDataStockMetaData[] stocks,
                                            IDataStockMetaData excludeStock) {

        // filter out exclude stock
        final String excludeStockName = excludeStock != null ? excludeStock.getName() : null;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("excluding stock " + excludeStockName);

        List<IDataStockMetaData> searchStocks = new ArrayList<IDataStockMetaData>();
        for (IDataStockMetaData dsm : stocks) {
            if (!dsm.getName().equals(excludeStockName)) {
                searchStocks.add(dsm);
            }
        }

        EntityManager em = PersistenceUtil.getEntityManager();

        if (params == null) {
            params = new ValueParser(); // avoids lots of null checks
        }

        final String typeAlias = "a";
        final String typeName = this.getJpaName();

        StringBuffer queryString = new StringBuffer();
        if (returnCount) {
            queryString.append("SELECT COUNT(DISTINCT " + typeAlias + ") FROM " + typeName + " " + typeAlias + " ");
        } else {
            queryString.append("SELECT DISTINCT " + typeAlias + " FROM " + typeName + " " + typeAlias + " ");
        }

        String defaultJoins = this.getDefaultQueryStringJoinPart(params, typeAlias);

        String joins = this.getQueryStringJoinPart(params, typeAlias, sortCriterium);
        if (joins == null) {
            joins = "";
        }

        List<String> whereClauses = new ArrayList<String>();
        Map<String, Object> whereParamValues = new LinkedHashMap<String, Object>();

        joins += this.addByClassClauses(catSystem, categories, whereClauses, whereParamValues);

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

        // containing data stocks filter
        String[] contDSIdsAsStrings = params.getStrings("'containingDataStocksColFilter'") != null ? params.getStrings("'containingDataStocksColFilter'") : new String[0];
        List<Long> containingDataStockIds = Stream.of(contDSIdsAsStrings)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (containingDataStockIds.size() > 0) {
            filterForContainingDataStocks(containingDataStockIds, whereParamValues, whereClauses, typeAlias);
        }

        // let's collect all name and description where clauses (for all dataset types)
        this.addNameDescWhereClauseAndNamedParamForQueryStringJpql(typeAlias, params, whereClauses, whereParamValues);

        // let's collect all default where clauses (for all dataset types)
        this.addDefaultWhereClausesAndNamedParamesForQueryStringJpql(typeAlias, params, whereClauses, whereParamValues);

        // let's collect all where clauses beside name, description and most
        // recent version
        this.addWhereClausesAndNamedParamesForQueryStringJpql(typeAlias, params, whereClauses, whereParamValues);

        if (mostRecentVersionOnly) {
            whereClauses.add(
                    buildMostRecentVersionsOnlySubQuery("a", this.getJpaName(), defaultJoins, joins, whereClauses));
        }

        if (!StringUtils.isBlank(defaultJoins)) {
            queryString.append(" ").append(defaultJoins.trim()).append(" ");
        }
        if (!StringUtils.isBlank(joins)) {
            queryString.append(" ").append(joins.trim()).append(" ");
        }

        if (!whereClauses.isEmpty()) {
            queryString.append("WHERE ");
            queryString.append(this.join(whereClauses, " AND "));
        }

        if (!returnCount) {
            final String orderJpql = this.getQueryStringOrderJpql(typeAlias, sortCriterium, sortOrder);
            queryString
                    .append(" ORDER BY " + orderJpql);
        }
        DataSetDao.LOGGER.debug("search query: {}", queryString);

        Query query = em.createQuery(String.valueOf(queryString));
        for (Entry<String, Object> e : whereParamValues.entrySet()) {
            if (DataSetDao.LOGGER.isTraceEnabled()) {
                DataSetDao.LOGGER.trace(e.getKey() + ": " + e.getValue());
            }
            query.setParameter(e.getKey(), e.getValue());
        }

        return query;
    }

    private void filterForContainingDataStocks(List<Long> dataStockIds, Map<String, Object> whereParamValues, List<String> whereClauses, String typeAlias) {
        for (int i = 0; i < dataStockIds.size(); i++) {
            String filterKey = "containingStocksColFilter";
            String subjectAlias = filterKey + "_typeAlias_" + i;
            String fieldAlias = filterKey + "_containingStockVar_" + i;
            String paramKey = filterKey + "Param" + i;
            Long paramValue = dataStockIds.get(i);

            String subquery = "(SELECT " + fieldAlias + ".id from Process " + subjectAlias +
                    " LEFT JOIN " + subjectAlias + ".containingDataStocks " + fieldAlias +
                    " WHERE " + subjectAlias + ".id = " + typeAlias + ".id)";
            whereClauses.add("(:" + paramKey + " IN " + subquery + ")");
            whereParamValues.put(paramKey, paramValue);
        }
    }

    /**
     * Remove a given data set from the database
     *
     * @param dataSet data set to be removed
     * @throws DeleteDataSetException on removing error from persistence layer
     * @throws Exception              on removing error from persistence layer
     */
    @Override
    public T remove(T dataSet) throws DeleteDataSetException {
        if (dataSet == null || dataSet.getId() == null) {
            return null;
        }
        try {
            T tmp = this.removeById(dataSet.getId());
            if (tmp != null) {
                final String uuid = tmp.getUuidAsString();
                // if the dataset to delete is the last one of its uuid, nothing
                // is wrong if setMostRecentVersionFlags
                // turns out false
                if (!this.setMostRecentVersionFlags(uuid) && this.getOtherVersions(tmp).size() != 0) {
                    throw new IllegalStateException("Could not set most recent version flag!");
                }
            }

            return tmp;
        } catch (Exception e) {
            DataSetDao.LOGGER.error("Cannot remove data set {} from database", dataSet.getName().getDefaultValue(), e);
            throw new DeleteDataSetException("Cannot remove data set " + dataSet.getName().getDefaultValue(), e);
        }
    }

    @Override
    public T removeById(long id) throws Exception {
        var ds = getById(id);
        markDataStocksDirty(ds); // mark stocks dirty before delete to avoid referential data loss
        ds = super.removeById(id);
        return ds;
    }

    /**
     * Get a foreign data set
     *
     * @param nodeShortName short name / id of remote node
     * @param uuid          uuid of data set
     * @return loaded data set
     */
    public D getForeignDataSet(String nodeShortName, String uuid, Long registryId) {
        ForeignDataSetsHelper helper = new ForeignDataSetsHelper(this);
        return helper.getForeignDataSet(this.voClass, nodeShortName, uuid, registryId);
    }

    /**
     * Build the ORDER BY clause for a primary and an optional secondary criterium
     *
     * @param typeAliasPrimary
     * @param primary
     * @param typeAliasSecondary
     * @param secondary
     * @param sortOrder          if true, default (ASC), else DESC
     * @return
     */
    protected String buildOrderBy(String typeAliasPrimary, String primary, String typeAliasSecondary, String secondary, boolean sortOrder) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(typeAliasPrimary))
            sb.append(typeAliasPrimary).append(".");
        sb.append(primary).append(" ");
        applySort(sb, sortOrder);
        if (StringUtils.isNotBlank(secondary)) {
            sb.append(", ");
            if (StringUtils.isNotBlank(typeAliasSecondary))
                sb.append(typeAliasSecondary).append(".");
            sb.append(secondary);
            applySort(sb, sortOrder);
        }
        return sb.toString();
    }

    private void applySort(StringBuilder sb, boolean sortOrder) {
        if (sortOrder)
            sb.append(" ASC ");
        else
            sb.append(" DESC ");
    }

    private void flagForDeletion(DataSet ds, boolean unflag) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();
        Long id = ds.getId();

        String updateStatement;
        if (unflag)
            updateStatement = "UPDATE " + this.getJpaName() + " SET visible = 1 WHERE ID = :idParam";
        else
            updateStatement = "UPDATE " + this.getJpaName() + " SET visible = 0 WHERE ID = :idParam";

        if (id != null) {
            t.begin();
            Query q = em.createQuery(updateStatement);
            q.setParameter("idParam", id);
            q.executeUpdate();
            t.commit();
        } // else we will ignore the request.
    }

    public void flagForDeletion(Collection<DataSetReference> refs) {
        Set<Long> ids = refs.stream()
                .filter(ref -> ref != null && ref.getId() != null)
                .map(DataSetReference::getId)
                .collect(Collectors.toSet());
        flagForDeletion(ids);
    }

    private void flagForDeletion(Set<Long> ids) {
        flagForDeletion(ids, false);
    }

    public void unflagForDeletion(Collection<DataSetReference> refs) {
        Set<Long> ids = refs.stream()
                .filter(ref -> ref != null && ref.getId() != null)
                .map(DataSetReference::getId)
                .collect(Collectors.toSet());
        unflagForDeletion(ids);
    }

    private void unflagForDeletion(Set<Long> ids) {
        flagForDeletion(ids, true);
    }

    private void flagForDeletion(Set<Long> ids, boolean unflag) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        String updateStatement;
        if (unflag)
            updateStatement = "UPDATE " + this.getJpaName() + " SET visible = true WHERE ID IN :idList";
        else
            updateStatement = "UPDATE " + this.getJpaName() + " SET visible = false WHERE ID IN :idList";


        for (List<Long> idList : Lists.partition(new ArrayList<>(ids), 1000)) {
            Query q = em.createQuery(updateStatement);
            q.setParameter("idList", idList);

            tx.begin();
            q.executeUpdate();
            tx.commit();
        }
    }

    public Long getCountImportedAfter(Instant instant, IDataStockMetaData... stocks) {
        final var qBuilder = new StringBuilder("SELECT COUNT(DISTINCT a) FROM ")
                .append(this.getJpaName())
                .append(" a");

        final var params = new HashMap<String, Object>();
        final var whereClauses = new ArrayList<String>();

        final var importDateKey = "importDate";
        params.put(importDateKey, Timestamp.from(instant));
        whereClauses.add("a.importDate > :" + importDateKey);

        final var stockMatches = new ArrayList<String>();
        boolean isLogicalStocksJoined = false;
        for (int i = 0; i < stocks.length; i++) {
            final var stock = stocks[i];

            String field;
            if (stock.isRoot()) {
                field = "a.rootDataStock.uuid";
            } else {
                final var logicalStockAlias = "lds";
                if (!isLogicalStocksJoined) {
                    qBuilder.append(" LEFT JOIN a.containingDataStocks " + logicalStockAlias);
                }

                field = logicalStockAlias + ".uuid";
            }

            final var stockUuidKey = "stockUuid" + i;
            params.put(stockUuidKey, new Uuid(stocks[i].getUuidAsString()));
            stockMatches.add(field + " = :" + stockUuidKey);
        }
        whereClauses.add("(" + String.join(" OR ", stockMatches) + ")");

        qBuilder.append(" WHERE ").append("(").append(String.join(" AND ", whereClauses)).append(")");

        final var q = PersistenceUtil.getEntityManager().createQuery(qBuilder.toString(), Long.class);
        for (final var param : params.entrySet()) {
            q.setParameter(param.getKey(), param.getValue());
        }

        return q.getSingleResult();
    }

    public abstract T getSupersedingDataSetVersion(String uuid);
}
