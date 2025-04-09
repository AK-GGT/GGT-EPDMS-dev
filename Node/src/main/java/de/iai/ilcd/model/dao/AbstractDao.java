package de.iai.ilcd.model.dao;

import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

/**
 * Common implementation for DAO objects
 *
 * @param <T> Class that is being accessed
 */
public abstract class AbstractDao<T> {

    protected static final String JPA_STANDARD_ALIAS = "a";
    protected final Map<String, String> registeredAliases = new HashMap<>();
    /**
     * The name in JPA of class
     */
    private final String jpaName;
    /**
     * The class accessed by this DAO
     */
    private final Class<T> accessedClass;

    /**
     * Create a DAO
     *
     * @param jpaName       the name in JPA of class
     * @param accessedClass the class accessed by this DAO
     */
    public AbstractDao(String jpaName, Class<T> accessedClass) {
        this.jpaName = jpaName;
        this.accessedClass = accessedClass;
    }

    protected static String[] cleanParams(String[] tagParams) {
        List<String> result = new ArrayList<String>();
        if (tagParams != null && tagParams.length > 0) {
            int n = tagParams.length;
            for (int i = 0; i < n; i++) {
                String param = tagParams[i];
                if (StringUtils.isNotBlank(param))
                    result.add(param);
            }
        }
        return result.stream().toArray(String[]::new);
    }

    /**
     * Get the name in JPA of class
     *
     * @return name in JPA of class
     */
    protected String getJpaName() {
        return this.jpaName;
    }

    /**
     * Get the class accessed by this DAO
     *
     * @return class accessed by this DAO
     */
    protected Class<T> getAccessedClass() {
        return this.accessedClass;
    }

    /**
     * Get all data sets in the persistence unit of the represented type T
     *
     * @return all data sets in the persistence unit of the represented type T
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select a from " + this.jpaName + " a").getResultList();
    }

    /**
     * Get the count of elements in the database for type T
     *
     * @return count of elements in the database for type T
     */
    public Long getAllCount() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return (Long) em.createQuery("select count(a) from " + this.jpaName + " a").getSingleResult();
    }

    /**
     * Get <code>pageSize</code> items starting on <code>startIndex</code>
     *
     * @param startIndex start index
     * @param pageSize   page size
     * @return list of matching elements
     */
    @SuppressWarnings("unchecked")
    public List<T> get(int startIndex, int pageSize) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select a from " + this.jpaName + " a")
                .setFirstResult(startIndex)
                .setMaxResults(pageSize)
                .getResultList();
    }

    /**
     * <p>
     * This method can be utilized for simple queries that need paging, sorting (on
     * one field only) and simple conditions on fields that don't require
     * collection handling, subqueries, ... . <br/>
     * Conditions on String fields are implemented as
     * "<code>a.filterKey LIKE '%filterValue%'</code>" while other objects or native
     * types are implemented as "<code> a.filterKey = filterValue </code>".
     * </p>
     * <p>
     * Example: "SELECT DISTINCT a FROM user a WHERE (a.whereKey[0] LIKE
     * '%whereValue[0]%') AND (a.whereKey[1] = whereValue[1]) ORDER BY sortField
     * sortOrder"
     * </p>
     * <p>
     * Adding join statements can be achieved by overriding the
     * generateBasicJoinStatements method.
     * </p>
     *
     * @param startIndex
     * @param pageSize
     * @param whereConditions {key: fieldName, value:value}
     * @param sortField
     * @param sortOrder       ASC or DESC
     * @return
     */
    public List<T> get(int startIndex, int pageSize, Map<String, Object> whereConditions, String sortField,
                       String sortOrder) {

        // e.g. "SELECT DISTINCT a FROM User a",
        // with condition {"firstName" = "Allan"}
        QueryData queryBaseData = generateJPQLBaseQueryData("SELECT DISTINCT " + JPA_STANDARD_ALIAS + " FROM " + this.jpaName + " " + JPA_STANDARD_ALIAS, whereConditions);

        StringBuilder queryStringBuilder = new StringBuilder(queryBaseData.getQuery());

        // ORDER BY
        if (sortField != null && sortOrder != null && !sortField.trim().isEmpty() && !sortOrder.trim().isEmpty())
            queryStringBuilder.append(" ORDER BY " + JPA_STANDARD_ALIAS + "." + sortField + " " + sortOrder); // e.g. "ORDER BY a.firstName ASC"

        String queryString = queryStringBuilder.toString();

        // Now we build the query.
        EntityManager em = PersistenceUtil.getEntityManager();
        TypedQuery<T> query = em.createQuery(queryString, this.accessedClass);

        query.setFirstResult(startIndex);
        query.setMaxResults(pageSize);

        // Set parameters for the where clause
        Map<String, Object> whereParams = queryBaseData.getParams();
        for (String key : whereParams.keySet()) {
            query.setParameter(key, whereParams.get(key));
        }

        return query.getResultList();

    }

    /**
     * Returns the count of entities that match the provided (simple) constraints.
     * <p>
     * This method can't handle collections, subqueries, etc. <br/>
     * Conditions on String fields are implemented as
     * "<code>a.filterKey LIKE '%filterValue%'</code>" while other objects or native
     * types are implemented as "<code> a.filterKey = filterValue </code>".
     * </p>
     * <p>
     * Adding join statements can be achieved by overriding the
     * generateBasicJoinStatements method.
     * </p>
     *
     * @param whereConditions {key: fieldName, value:value}
     * @return
     */
    public Long getCount(Map<String, Object> whereConditions) {
        // e.g. "SELECT COUNT(DISTINCT a) FROM User a",
        // with condition {"firstName" = "Allan"}
        QueryData qd = generateJPQLBaseQueryData("SELECT COUNT(DISTINCT " + JPA_STANDARD_ALIAS + ") FROM " + this.jpaName + " " + JPA_STANDARD_ALIAS, whereConditions);

        EntityManager em = PersistenceUtil.getEntityManager();
        TypedQuery<Long> q = em.createQuery(qd.getQuery(), Long.class);

        // Set parameters for the where clause
        Map<String, Object> whereParams = qd.getParams();
        for (String key : whereParams.keySet()) {
            q.setParameter(key, whereParams.get(key));
        }

        return q.getSingleResult();
    }

    /**
     * This method mainly makes sure that counting and fetching uses the same base
     * query construction.
     * <p>
     * This method can't handle collections, subqueries, etc. <br/>
     * Conditions on String fields are implemented as
     * "<code>a.filterKey LIKE '%filterValue%'</code>" while other objects or native
     * types are implemented as "<code> a.filterKey = filterValue </code>".
     * </p>
     * <p>
     * Adding join statements can be achieved by overriding the
     * generateBasicJoinStatements method.
     * </p>
     *
     * @param selectPhrase    The complete select phrase<br/>
     *                        e.g. "SELECT DISTINCT " + THIS_JPA_ALIAS + " FROM " +
     *                        this.jpaName + " " + THIS_JPA_ALIAS
     * @param whereConditions {key: fieldName, value:value}.
     * @return Query string and parameters wrapped in a QueryData object.
     */
    protected QueryData generateJPQLBaseQueryData(String selectPhrase, Map<String, Object> whereConditions) {
        // select statement
        StringBuilder sb = new StringBuilder(selectPhrase);

        // join statement
        String joinStatement = getStandardJoinStatement();
        if (joinStatement != null && !joinStatement.trim().isEmpty()) {
            sb.append(" " + joinStatement);
        }

        // where clause
        Map<String, Object> whereParams = new HashMap<String, Object>();

        if (whereConditions != null && whereConditions.size() > 0) {
            List<String> whereClauses = new ArrayList<String>();

            for (String field : whereConditions.keySet()) {
                String fieldAlias = registeredAliases.get(field) != null ? registeredAliases.get(field) : JPA_STANDARD_ALIAS + "." + field;

                Object o = whereConditions.get(field);
                String identifier = field.replaceAll("\\W", "");

                if (o instanceof String) {

                    // Strings are matched loosely
                    // e.g. "(fieldAlias LIKE :fieldNameAsParam)"
                    // where fieldNameAsParam = "%hello world%" is registered instead of "hello world"
                    whereClauses.add("(" + fieldAlias + " LIKE :" + identifier + ")");
                    whereParams.put(identifier, "%" + String.valueOf(o) + "%");

                } else if (o instanceof Map) {

                    // Maps are a bit delicate... we will iterate over entries and write
                    // e.g. for the third entry of a provided map (i = 2):
                    // "(KEY(mapName) =:mapName2Key AND VALUE(mapName) =:mapName2Value)"
                    // which translates to something that is equivalent to the relation
                    // 'the map 'mapName' contains pair {key=mapName2Key, value=mapName2Value}'
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> m = (Map<Object, Object>) o;

                    int i = 0;
                    for (Entry<Object, Object> e : m.entrySet()) {
                        final String keyIdentifier = identifier + i + "Key";
                        final String valueIdentifier = identifier + i + "Value";

                        StringBuilder whereMapContainsClause = new StringBuilder("");
                        whereMapContainsClause.append("(KEY(" + fieldAlias + ") =:" + keyIdentifier + ")");
                        whereMapContainsClause.append(" AND ");
                        whereMapContainsClause.append("(VALUE(" + fieldAlias + ") =:" + valueIdentifier + ")");

                        whereParams.put(keyIdentifier, e.getKey());
                        whereParams.put(valueIdentifier, e.getValue());

                        whereClauses.add("(" + whereMapContainsClause.toString() + ")");
                        i++;
                    }

                } else if (o != null) {

                    // trying the naive way for all other types
                    // e.g. (fieldAlias =: fieldNameAsParam)
                    whereClauses.add("(" + fieldAlias + " = :" + identifier + ")");
                    whereParams.put(identifier, o);
                }
            }

            if (whereClauses.size() > 0) {
                sb.append(" WHERE ");
                sb.append(String.join(" AND ", whereClauses));
            }
        }

        return new QueryData(sb.toString(), whereParams);
    }

    /**
     * <p>
     * The basic <code>get</code> method appends the Join-Statement that is
     * generated in this method (if not null, which is returned by the default
     * implementation). If you need joins there, override this method.
     * </p>
     *
     * <p>
     * If the alias doesn't match the field name, the mapping must be registered,
     * e.g:<br/>
     * <code>{</code><br/>
     * <code>List< String > singleJoinStatements = new ArrayList<>();</code><br/>
     * <code>singleJoinStatements.add("LEFT JOIN " + typeAlias + ".fieldName fieldAlias");</code><br/>
     * <code>this.registeredAliases.put("fieldName", "fieldAlias");</code><br/>
     * <code>return String.join(" ", singleJoinStatements);</code><br/>
     * <code>}</code><br/>
     * </p>
     * <p>
     * Please make sure the aliases are sufficiently unique. Uniqueness is neither
     * checked nor enforced.
     * </p>
     *
     * @return String of joins to be appended between SELECT and WHERE clauses..
     */
    protected String getStandardJoinStatement() {

        // Overrride this if you want to add joins to simple get Method.

        return null;
    }

    /**
     * Join a list of strings via {@link StringUtils#join(java.util.Iterator, String)}
     *
     * @param list      list to join
     * @param separator separator to use
     * @return joined list
     */
    protected String join(List<String> list, String separator) {
        return StringUtils.join(list.iterator(), separator);
    }

    /**
     * Default persist
     *
     * @param obj object to persist
     * @throws PersistException on errors (transaction is being rolled back)
     */
    public void persist(T obj) throws PersistException {
        if (obj == null) {
            return;
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            em.persist(obj);
            t.commit();
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();
            throw new PersistException(e.getMessage(), e);
        }
    }

    /**
     * Default persist
     *
     * @param objs objects to persist
     * @throws PersistException on errors (transaction is being rolled back)
     */
    public void persist(Collection<T> objs) throws PersistException {
        if (objs == null || objs.isEmpty()) {
            return;
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            for (T obj : objs) {
                em.persist(obj);
            }
            t.commit();
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();
            throw new PersistException(e.getMessage(), e);
        }
    }

    /**
     * Default remove: bring back to persistence context if required and delete
     *
     * @param obj object to remove
     * @return remove object
     * @throws Exception on errors (transaction is being rolled back)
     */
    public T remove(T obj) throws Exception {
        if (obj == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        em.clear();

        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            T tmp = em.contains(obj) ? obj : em.merge(obj);
            em.remove(tmp);
            t.commit();
            return tmp;
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();
            throw e;
        }
    }

    /**
     * Default remove: bring back to persistence context if required and delete
     *
     * @param objs objects to remove
     * @return removed objects
     * @throws Exception on errors (transaction is being rolled back)
     */
    public Collection<T> remove(Collection<T> objs) throws Exception {
        if (objs == null || objs.isEmpty()) {
            return null;
        }
        Collection<T> res = new ArrayList<T>();
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            for (T obj : objs) {
                T tmp = em.contains(obj) ? obj : em.merge(obj);
                em.remove(tmp);
                res.add(tmp);
            }
            t.commit();
            return res;
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();
            throw e;
        }
    }

    /**
     * Default merge
     *
     * @param obj object to merge
     * @return managed object
     * @throws MergeException on errors (transaction is being rolled back)
     */
    public T merge(T obj) throws MergeException {
        if (obj == null) {
            return null;
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {

            Instant startInstant = Instant.now();
            while (t.isActive() && Duration.between(startInstant, Instant.now()).getSeconds() < 5) {
                // wait
            }

            t.begin();
            obj = em.merge(obj);
            t.commit();
            return obj;
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();

            throw new MergeException("Cannot merge changes to " + String.valueOf(obj) + " into the database", e);
        }
    }

    /**
     * Default merge
     *
     * @param objs objects to merge
     * @return list of merged/managed objects
     * @throws MergeException on errors (transaction is being rolled back)
     */
    public Collection<T> merge(Collection<T> objs) throws MergeException {
        if (objs == null || objs.isEmpty()) {
            return null;
        }
        final Collection<T> tmp = new ArrayList<T>();
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction t = em.getTransaction();

        try {
            t.begin();
            for (T obj : objs) {
                tmp.add(em.merge(obj));
            }
            t.commit();
            return tmp;
        } catch (Exception e) {
            if (t.isActive())
                t.rollback();
            throw new MergeException("Cannot merge changes to " + String.valueOf(objs) + " into the database", e);
        }
    }

    /**
     * Simple wrapper for query data
     */
    protected class QueryData {

        final String query;

        final Map<String, Object> params;

        protected QueryData(String query, Map<String, Object> params) {
            super();
            this.query = query;
            this.params = params;
        }

        protected String getQuery() {
            return query;
        }

        protected Map<String, Object> getParams() {
            return params;
        }

    }
}
