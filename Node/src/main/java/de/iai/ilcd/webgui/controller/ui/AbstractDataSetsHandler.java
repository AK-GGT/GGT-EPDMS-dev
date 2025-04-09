package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.security.SecurityUtil;
import org.apache.velocity.tools.generic.ValueParser;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Handler for DataSet lists
 * </p>
 * <p>
 * <strong>Please note:</strong> There will be <u>no filtering</u> until {@link #doFilter()} was called!
 * </p>
 *
 * @param <T> type of model objects that this handler provides
 * @param <D> type of data set DAO
 */
public abstract class AbstractDataSetsHandler<T extends DataSet, D extends DataSetDao<T, ?, ?>> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 4275620873048386353L;
    /**
     * The map / value parser with the filter
     */
    private final Map<String, Object> parameterMap = new HashMap<String, Object>();
    /**
     * DAO instance for data retrieval
     */
    private final D daoInstance;
    /**
     * View ID of data table for fix
     */
    private final String dataTableViewId;
    /**
     * Class type of data
     */
    private final Class<T> type;
    /**
     * The lazy model for data sets
     */
    private DataSetLazyDataModel<T> lazyDataModel;
    /**
     * Stock selection handler
     */
    @ManagedProperty(value = "#{stockSelection}")
    private StockSelectionHandler stockSelection;

    /**
     * Initialize the handler
     *
     * @param type      type of model objects to access
     * @param daoObject matching DAO for data access
     */
    public AbstractDataSetsHandler(Class<T> type, D daoInstance) {
        this(type, daoInstance, null);
    }

    /**
     * Initialize the handler. Please note that this implementation <strong>only loads the most recent version of a data
     * set</strong>
     * <p>
     * TODO this is required for the primefaces datatable issue workaround in {@link #doFilter()}
     *
     * @param type            type of model objects to access
     * @param daoObject       matching DAO for data access
     * @param dataTableViewId id of the data table in view
     */
    public AbstractDataSetsHandler(Class<T> type, D daoInstance, String dataTableViewId) {
        this.type = type;
        this.daoInstance = daoInstance;
        this.dataTableViewId = dataTableViewId;
    }

    /**
     * Will be called after dependency injection
     */
    @PostConstruct
    public final void init() {
        SecurityUtil.assertCanRead(this.stockSelection.getCurrentStock());

        this.lazyDataModel = this.createLazyDataModel();
        this.postConstruct();
    }

    /**
     * Create the lazy data model
     *
     * @return the lazy data model
     */
    protected DataSetLazyDataModel<T> createLazyDataModel() {
        return new DataSetLazyDataModel<T>(this.type, this.daoInstance, true, this.stockSelection.getCurrentStockAsArray());
    }

    /**
     * Method called after dependency injection by {@link #init()}. Override to perform actions.
     */
    protected void postConstruct() {

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
     * Set filter, empty strings will be considered as indicator to remove filter
     *
     * @param key   key of filter
     * @param value value of filter
     */
    protected void setFilter(String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            this.parameterMap.put(key, value);
        } else {
            this.parameterMap.remove(key);
        }
    }

    /**
     * Set filter, null means: remove filter
     *
     * @param key   key of filter
     * @param value value of filter
     */
    protected void setFilter(String key, Object value) {
        if (value != null) {
            this.parameterMap.put(key, value);
        } else {
            this.parameterMap.remove(key);
        }
    }

    /**
     * Get the value of a filter as string
     *
     * @param key key in map to get filter from
     * @return the value of the filter (<code>null</code> if not available, no exception is being thrown)
     */
    protected String getFilter(String key) {
        Object o = this.parameterMap.get(key);
        return o != null ? o.toString() : null;
    }

    /**
     * Get the value of a filter as object
     *
     * @param key key in map to get filter from
     * @return the value of the filter (<code>null</code> if not available, no exception is being thrown)
     */
    protected Object getFilterObject(String key) {
        return this.parameterMap.get(key);
    }

    /**
     * Get the string array value of a filter
     *
     * @param key key in map to get filter from
     * @return the string array value of the filter (<code>null</code> if not available or no string array, no exception
     * is being thrown)
     */
    protected String[] getFilterStringArr(String key) {
        Object o = this.parameterMap.get(key);
        return o instanceof String[] ? (String[]) o : null;
    }

    /**
     * Get the string array value of a filter
     *
     * @param key key in map to get filter from
     * @return the string array value of the filter (<code>null</code> if not available or no string array, no exception
     * is being thrown)
     */
    protected Boolean getFilterBoolean(String key) {
        Object o = this.parameterMap.get(key);
        return o instanceof Boolean ? (Boolean) o : null;
    }

    /**
     * Get the lazy data model for this handler
     *
     * @return lazy data model for this handler
     */
    public DataSetLazyDataModel<T> getLazyDataModel() {
        return this.lazyDataModel;
    }

    /**
     * Get the DAO instance
     *
     * @return DAO instance
     */
    protected D getDaoInstance() {
        return this.daoInstance;
    }

    /**
     * Do the filtering
     * <p>
     * TODO remove workaround for primefaces datatable issue
     */
    public final void doFilter() {
        // load the lazy data
        this.lazyDataModel.setParams(new ValueParser(this.parameterMap));
    }

    /**
     * Get the current parameter map as value parser
     *
     * @return current parameter map as value parser
     */
    public ValueParser getParameterMapAsValueParser() {
        return new ValueParser(this.parameterMap);
    }

    /**
     * Get the stock selection handler
     *
     * @return stock selection handler
     */
    public StockSelectionHandler getStockSelection() {
        return this.stockSelection;
    }

    /**
     * Set the stock selection handler
     *
     * @param stockSelection stock selection handler to set
     */
    public void setStockSelection(StockSelectionHandler stockSelection) {
        this.stockSelection = stockSelection;
    }
}
