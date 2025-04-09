package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.LciaResultClClassStats;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRightDao;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.model.Parameter.Source;
import org.primefaces.model.SortOrder;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * DAO for data stocks
 */
public class CommonDataStockDao extends AbstractLongIdObjectDao<AbstractDataStock> {

    /**
     * Create the DAO
     */
    public CommonDataStockDao() {
        super("AbstractDataStock", AbstractDataStock.class);
    }

    private void updateTimeStampForLciaResultStats(DataStock stock) throws MergeException {
        LciaResultClClassStatsDao dao = new LciaResultClClassStatsDao();
        List<LciaResultClClassStats> stats = dao.getByStock(Long.toString(stock.getId()));
        long currentTimeInMillis = System.currentTimeMillis();
        for (LciaResultClClassStats stat : stats) {
            stat.setTsLastChange(currentTimeInMillis);
        }
        dao.merge(stats);
    }

    public Collection<AbstractDataStock> remove(Collection<AbstractDataStock> stocks) throws Exception {
        Collection<AbstractDataStock> removedStocks = super.remove(stocks);
        for (AbstractDataStock s : removedStocks) {
            if (s instanceof DataStock) {
                this.updateTimeStampForLciaResultStats((DataStock) s);
            }
        }
        return removedStocks;
    }

    public AbstractDataStock remove(AbstractDataStock stock) throws Exception {
        AbstractDataStock s = super.remove(stock);
        if (s instanceof DataStock) {
            this.updateTimeStampForLciaResultStats((DataStock) s);
        }

        return s;
    }

    public Collection<AbstractDataStock> merge(Collection<AbstractDataStock> stocks) throws MergeException {
        Collection<AbstractDataStock> mergedStocks = super.merge(stocks);
        for (AbstractDataStock mergedStock : mergedStocks) {
            if (mergedStock instanceof DataStock) {
                this.updateTimeStampForLciaResultStats((DataStock) mergedStock);
            }
        }
        return mergedStocks;
    }

    public AbstractDataStock merge(AbstractDataStock stock) throws MergeException {
        AbstractDataStock mergedStock = super.merge(stock);
        if (mergedStock instanceof DataStock) {
            this.updateTimeStampForLciaResultStats((DataStock) mergedStock);
        }
        return mergedStock;
    }

    /**
     * Get <code>pageSize</code> items starting on <code>startIndex</code>
     *
     * @param startIndex start index
     * @param pageSize   page size
     * @param sortOrder  ASC or DESC
     * @param sortField  field to sort against
     * @return list of matching elements
     */
    @SuppressWarnings("unchecked")
    public List<AbstractDataStock> get(int startIndex, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        EntityManager em = PersistenceUtil.getEntityManager();

        String queryString = buildQuery(false, sortField, sortOrder, filters);

        return em.createQuery(queryString).setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
    }

    public long getAllCount(Map<String, Object> filters) {
        EntityManager em = PersistenceUtil.getEntityManager();

        String queryString = buildQuery(true, null, null, filters);

        return (long) em.createQuery(queryString).getSingleResult();
    }

    private String buildQuery(boolean count, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        StringBuilder queryString = new StringBuilder("SELECT ");

        if (count)
            queryString.append("COUNT(a)");
        else
            queryString.append("a");

        queryString.append(" FROM AbstractDataStock a");

        queryString.append(" LEFT JOIN a.displayProperties p ");

        if (filters != null && filters.get("displayProperties.hidden") != null) {
            Boolean showHidden = (Boolean) filters.get("displayProperties.hidden");

            if (filters.get("stockProperties.logical") != null) {
                Boolean stockLogical = (Boolean) filters.get("stockProperties.logical");

                if (stockLogical != null && stockLogical) {
                    queryString.append(" LEFT JOIN DataStock d ON (a.id=d.id)");
                } else if (stockLogical != null) {
                    queryString.append(" LEFT JOIN RootDataStock d ON (a.id=d.id)");
                }
            }

            if (showHidden != null && !showHidden)
                queryString.append(" WHERE p IS NULL OR p.hidden=false");
        }

        if (!count && sortField != null && sortOrder != null) {
            final String orderJpql = this.getQueryStringOrderJpql(sortField);
            queryString.append(" ORDER BY ").append(orderJpql).append(" ").append(SortOrder.DESCENDING.equals(sortOrder) ? "DESC" : "ASC");
        } else {
            queryString.append(" ORDER by p.ordinal");
        }

        return queryString.toString();
    }

    /**
     * Get all data sets in the persistence unit of the represented type T
     *
     * @return all data sets in the persistence unit of the represented type T
     */
    @SuppressWarnings("unchecked")
    public List<AbstractDataStock> getAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("SELECT a FROM AbstractDataStock a LEFT JOIN a.displayProperties p ORDER BY p.ordinal").getResultList();
    }

    /**
     * Get the ORDER BY part of the query. If no match for the sortString is being found, return default value
     *
     * @param sortString sort string
     * @return ORDER BY part of JPQL query text
     */
    protected String getQueryStringOrderJpql(String sortString) {
        if (StringUtils.startsWith(sortString, "name")) {
            return "a.name";
        } else if ("root".equals(sortString)) {
            return "TYPE(a)";
        } else if ("ownerOrganization.name".equals(sortString)) {
            return "a.ownerOrganization.name";
        } else {
            return "p.ordinal";
        }
    }


    /**
     * Get a data stock by an identifier
     *
     * @param identifier identifier means {@link DataStock#getName() name} or
     *                   {@link DataStock#getUuid() UUID}
     * @return loaded data stock
     */
    public AbstractDataStock getDataStockByIdentifier(String identifier) {
        if (StringUtils.isNotBlank(identifier)) {
            boolean isUuid = identifier.indexOf('-') >= 0;
            if (isUuid) {
                return this.getDataStockByUuid(identifier);
            } else {
                return this.getDataStockByName(identifier);
            }
        }
        return null;
    }

    /**
     * Get a data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public AbstractDataStock getDataStockById(long id) {
        return this.getById(id);
    }

    /**
     * get a data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public AbstractDataStock getDataStockById(String id) {
        try {
            return this.getDataStockById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a root data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public RootDataStock getRootDataStockById(long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.find(RootDataStock.class, id);
    }

    /**
     * Get a non root data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public DataStock getNonRootDataStockById(long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.find(DataStock.class, id);
    }

    /**
     * Get a root data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public RootDataStock getRootDataStockById(String id) {
        try {
            return this.getRootDataStockById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a root data stock by it's database ID
     *
     * @param id id of data stock to find
     * @return loaded data stock
     */
    public DataStock getNonRootDataStockById(String id) {
        try {
            return this.getNonRootDataStockById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a data stock by name
     *
     * @param name name of data stock to load
     * @return the data stock with given name
     */
    public AbstractDataStock getDataStockByName(String name) {
        return this.getDataStockByName(name, this.getJpaName(), this.getAccessedClass());
    }

    /**
     * Get a root data stock by name
     *
     * @param name name of data stock to load
     * @return loaded root data stock
     */
    public RootDataStock getRootDataStockByName(String name) {
        return this.getDataStockByName(name, "RootDataStock", RootDataStock.class);
    }

    /**
     * Get a root data stock by name
     *
     * @param name name of data stock to load
     * @return loaded root data stock
     */
    public DataStock getNonRootDataStockByName(String name) {
        return this.getDataStockByName(name, "DataStock", DataStock.class);
    }

    /**
     * Get a data stock by name
     *
     * @param name name of data stock to load
     * @return the data stock of given name
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractDataStock> T getDataStockByName(String name, String jpaName, Class<T> type) {
        EntityManager em = PersistenceUtil.getEntityManager();

        T stock = null;
        try {
            stock = (T) em.createQuery("SELECT a FROM " + jpaName + " a WHERE a.name=:name").setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            // do nothing; we just return null
        }
        return stock;
    }

    /**
     * Get a data stock by name
     *
     * @param uuid uuid of data stock to load
     * @return the data stock with given uuid
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractDataStock> T getDataStockByUuid(String uuid) {
        EntityManager em = PersistenceUtil.getEntityManager();
        T stock = null;
        try {
            stock = (T) em.createQuery("SELECT a FROM " + this.getJpaName() + " a WHERE a.uuid.uuid=:uuid")
                    .setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException e) {
            // do nothing; we just return null
        }
        return stock;
    }

    /**
     * Get all readable data stocks
     *
     * @param uid  user ID
     * @param gids group IDs
     * @return list of all readable data stocks
     */
    public List<AbstractDataStock> getAllReadable(long uid, List<Long> gids) {
        StockAccessRightDao sarDao = new StockAccessRightDao();
        return this.getAllReadable(sarDao.get(uid, gids));
    }

    /**
     * Get all readable data stocks
     *
     * @param u user to get for
     * @return list of all readable data stocks
     */
    public List<AbstractDataStock> getAllReadable(User u) {
        StockAccessRightDao sarDao = new StockAccessRightDao();
        return this.getAllReadable(sarDao.get(u));
    }

    /**
     * Get all readable data stocks
     *
     * @param lstSar list of stock access rights
     * @return list of all readable data stocks
     */
    @SuppressWarnings("unchecked")
    public List<AbstractDataStock> getAllReadable(List<StockAccessRight> lstSar) {
        List<Long> readableStockIds = new ArrayList<>();
        for (StockAccessRight sar : lstSar) {
            if (ProtectionType.READ.isContained(sar.getValue())) {
                readableStockIds.add(sar.getStockId());
            }
        }

        if (readableStockIds.isEmpty()) {
            return Collections.emptyList();
        }
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("select a from " + this.getJpaName() + " a WHERE a.id IN("
                + StringUtils.join(readableStockIds, ",") + ")").getResultList();
    }

    /**
     * TODO needs method rename, runtime optimization
     *
     * @param stockId id of stock
     * @return false if there is no data to validate or to export otherwise true
     */
    public boolean isDataStockToValidate(Long stockId) {

        AbstractDataStock ads = this.getDataStockById(stockId);

        if (ads.isRoot()) {
            EntityManager em = PersistenceUtil.getEntityManager();
            Query processQuery = em.createQuery("SELECT ds.id FROM Process ds where ds.rootDataStock.id=:stockId",
                    Process.class);
            processQuery.setParameter("stockId", stockId);
            if (processQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query elementaryFlowsprocessQuery = em.createQuery(
                    "SELECT ds.id FROM ElementaryFlow ds where ds.rootDataStock.id=:stockId", ElementaryFlow.class);
            elementaryFlowsprocessQuery.setParameter("stockId", stockId);
            if (elementaryFlowsprocessQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query unitGroupsQuery = em.createQuery(
                    "SELECT ds.id FROM UnitGroup ds where ds.rootDataStock.id=:stockId", UnitGroup.class);
            unitGroupsQuery.setParameter("stockId", stockId);
            if (unitGroupsQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query sourcesQuery = em.createQuery("SELECT ds.id FROM Source ds where ds.rootDataStock.id=:stockId",
                    Source.class);
            sourcesQuery.setParameter("stockId", stockId);
            if (sourcesQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query contactsQuery = em.createQuery("SELECT ds.id FROM Contact ds where ds.rootDataStock.id=:stockId",
                    Contact.class);
            contactsQuery.setParameter("stockId", stockId);
            if (contactsQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query lciaMethodsQuery = em.createQuery(
                    "SELECT ds.id FROM LCIAMethod ds where ds.rootDataStock.id=:stockId", LCIAMethod.class);
            lciaMethodsQuery.setParameter("stockId", stockId);
            if (lciaMethodsQuery.setMaxResults(1).getResultList().size() > 0)
                return true;

            Query lifeCycleModelsQuery = em.createQuery(
                    "SELECT ds.id FROM LifeCycleModel ds where ds.rootDataStock.id=:stockId", LifeCycleModel.class);
            lifeCycleModelsQuery.setParameter("stockId", stockId);
            if (lifeCycleModelsQuery.setMaxResults(1).getResultList().size() > 0)
                return true;
        } else if (ads instanceof DataStock) {
            return isDataStockNoEmpty(ads);
        }

        return false;
    }

    /**
     * @param ads the stock
     * @return false if there are no datasets in the specified data stock, otherwise true
     */
    private boolean isDataStockNoEmpty(AbstractDataStock ads) {
        return !((DataStock) ads).getProcesses().isEmpty()
                || !((DataStock) ads).getElementaryFlows().isEmpty()
                || !((DataStock) ads).getFlowProperties().isEmpty()
                || !((DataStock) ads).getUnitGroups().isEmpty()
                || !((DataStock) ads).getSources().isEmpty()
                || !((DataStock) ads).getContacts().isEmpty()
                || !((DataStock) ads).getLciaMethods().isEmpty()
                || !((DataStock) ads).getLifeCycleModels().isEmpty();
    }

    @Override
    public void persist(AbstractDataStock obj) throws PersistException {
        super.persist(obj);

        final var em = PersistenceUtil.getEntityManager();
        final var tx = em.getTransaction();
        tx.begin();
        final var displayProps = obj.getDisplayProperties();
        /*
        Display props contain an ordinal value, by which the default ordering of stocks is defined.
        Because this ordering can be altered by *paginated* views, extra care has to be taken with initial values.
        null, Integer.MAX and Integer.MIN are all not suitable (re-ordered entries may unexpectedly appear on a completely different page).
        We need dynamically increasing defaults like MAX(ordinal) + 1.
        Using the id does the trick well enough but without additional READ queries or triggers.
         */
        displayProps.setOrdinal(-displayProps.getId().intValue());
        em.merge(obj);
        tx.commit();
    }

    @Override
    public void persist(Collection<AbstractDataStock> objs) throws PersistException {
        super.persist(objs);

        final var em = PersistenceUtil.getEntityManager();
        final var tx = em.getTransaction();
        tx.begin();
        for (AbstractDataStock obj : objs) {
            final var displayProps = obj.getDisplayProperties();
        /*
        Display props contain an ordinal value, by which the default ordering of stocks is defined.
        Because this ordering can be altered by *paginated* views, extra care has to be taken with initial values.
        null, Integer.MAX and Integer.MIN are all not suitable (re-ordered entries may unexpectedly appear on a completely different page).
        We need dynamically increasing defaults like MAX(ordinal) + 1.
        Using the id does the trick well enough but without additional READ queries or triggers.
         */
            displayProps.setOrdinal(-displayProps.getId().intValue());
            em.merge(obj);
        }
        tx.commit();
    }
}
