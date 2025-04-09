package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.utils.DistributedSearchLog;
import de.iai.ilcd.webgui.util.PrimefacesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lazy data model for JSF for all data set types
 *
 * @param <T> type of data set
 */
public class DataSetLazyDataModel<T extends DataSet> extends LazyDataModel<T> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 651369617999373671L;

    private static final Logger logger = LogManager.getLogger(DataSetLazyDataModel.class);

    /**
     * Type of data set
     */
    private final Class<T> type;

    /**
     * Load most recent version only
     */
    private final boolean mostRecentOnly;

    /**
     * DAO for data set type
     */
    private final DataSetDao<T, ?, ?> daoObject;
    /**
     * the stocks
     */
    private final IDataStockMetaData[] stocks;
    /**
     * the parameter values
     */
    private Map<String, Object> params;
    private Map<String, Object> defaultFilters = new HashMap<>();
    /**
     * Log for distributed search
     */
    private DistributedSearchLog log;

    private boolean distributedSearch = false;

    /**
     * Initialize lazy data model, package wide visibility for the handler classes
     *
     * @param type           type of data set
     * @param daoObject      DAO for data set
     * @param mostRecentOnly Load most recent version only
     * @param stocks         stocks
     */
    DataSetLazyDataModel(Class<T> type, DataSetDao<T, ?, ?> daoObject, boolean mostRecentOnly, IDataStockMetaData[] stocks) {
        if (type == null) {
            throw new IllegalArgumentException("Type for lazy data model must not be null");
        }
        if (daoObject == null) {
            throw new IllegalArgumentException("Dao object for lazy data model must not be null");
        }
        this.stocks = stocks;
        this.mostRecentOnly = mostRecentOnly;

        // init default filters
        this.defaultFilters.put("visible", true);

        this.params = new HashMap<>(defaultFilters);
        this.type = type;
        this.daoObject = daoObject;
        this.setRowCount((int) this.daoObject.searchResultCount(new ValueParser(this.params), this.mostRecentOnly, stocks));
    }

    /**
     * Get the parameters
     *
     * @return the parameters
     */
    public Map<String, Object> getParams() {
        return this.params;
    }

    /**
     * Set the parameters
     *
     * @param params the parameters to set
     */
    public void setParams(Map<String, Object> params) {
        Map<String, Object> tmp = new HashMap<>(params); // Workaround: Maps can be read-only! Using
        // org.apache.velocity.tools.generic.ValueParser
        // is very safe.
        tmp.putAll(this.defaultFilters);

        this.params = (params instanceof ValueParser) ? new ValueParser(tmp) : tmp;
    }

    /**
     * Get the current DAO instance
     *
     * @return current DAO instance
     */
    protected DataSetDao<T, ?, ?> getDaoObject() {
        return this.daoObject;
    }

    /**
     * Get the stocks
     *
     * @return stocks
     */
    protected IDataStockMetaData[] getStocks() {
        return this.stocks;
    }

    /**
     * Get the type of the data set
     *
     * @return type of the data set
     */
    protected Class<T> getType() {
        return this.type;
    }

    /**
     * Determine if only most recent versions shall be loaded
     *
     * @return <code>true</code> if only most recent versions shall be loaded, <code>false</code> otherwise
     */
    protected boolean isMostRecentOnly() {
        return this.mostRecentOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {

        // Normalize for legacy signature in lazyLoad method
        String sortField = (String) PrimefacesUtil.getLegacySortData(sortMeta).get("sortField");
        SortOrder sortOrder = (SortOrder) PrimefacesUtil.getLegacySortData(sortMeta).get("sortOrder");
        Map<String, Object> filters = PrimefacesUtil.getLegacyFilterData(filterMeta);

        Map<String, Object> parameters = new HashMap<>(filters);

        for (String key : this.params.keySet()) {
            if (logger.isTraceEnabled())
                logger.trace(key + " : " + this.params.get(key));
            parameters.put(key, this.params.get(key));
        }

        this.setRowCount((int) this.daoObject.searchResultCount(new ValueParser(parameters), this.mostRecentOnly, this.stocks));

        return this.daoObject.lsearch(new ValueParser(parameters), first, pageSize, sortField, !SortOrder.DESCENDING.equals(sortOrder), this.isMostRecentOnly(), this.stocks, null);
    }

    public long loadTotalElementCount() {
        Map<String, Object> stdCountFilters = new HashMap<>();
        stdCountFilters.put("visible", true);

        return this.daoObject.searchResultCount(new ValueParser(stdCountFilters), this.mostRecentOnly, this.stocks);
    }

    /**
     * Get the log for distributed search
     *
     * @return log for distributed search
     */
    public DistributedSearchLog getLog() {
        return this.log;
    }

    /**
     * Set the log for distributed search
     *
     * @param log log for distributed search to set
     */
    public void setLog(DistributedSearchLog log) {
        this.log = log;
    }

    @Override
    public T getRowData(String rowKey) {

        List<T> l = getWrappedData();

        for (T o : l) {
            if (o.getId().toString().equals(rowKey))
                return o;
        }

        return null;
    }

    @Override
    public Object getRowKey(T t) {
        return t.getId();
    }

    public boolean isDistributedSearch() {
        return distributedSearch;
    }

    public void setDistributedSearch(boolean distributedSearch) {
        this.distributedSearch = distributedSearch;
    }

}
