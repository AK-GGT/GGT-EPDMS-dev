package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.service.task.delete.DeleteTask;
import de.iai.ilcd.util.DependenciesOptions;
import de.iai.ilcd.util.DependenciesUtil;
import de.iai.ilcd.util.IDatasetListBackingBean;
import de.iai.ilcd.webgui.controller.ui.StockSelectionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.event.ValueChangeEvent;
import java.util.*;

/**
 * Base class for all data set lists
 *
 * @param <T> type of data set
 */
public abstract class AbstractDataSetListHandler<T extends DataSet> extends AbstractAdminListHandler<T> implements IDatasetListBackingBean<T> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6411162747167696159L;
    private static final Logger logger = LogManager.getLogger(AbstractDataSetListHandler.class);
    /**
     * Data access object to use
     */
    private final DataSetDao<T, ?, ?> dao;
    /**
     * Type class
     */
    private final Class<T> type;
    /**
     * Parameters
     */
    protected ValueParser params;
    /**
     * Stock handler
     */
    @ManagedProperty(value = "#{stockHandler}")
    protected StockHandler stockHandler;
    protected boolean mostRecentVersionOnly = ConfigurationService.INSTANCE.getDisplayConfig().isDefaultMostRecentVersionOnly();
    protected IDataStockMetaData moveTarget = null;
    /**
     * Stock selection handler
     */
    @ManagedProperty(value = "#{stockSelection}")
    private StockSelectionHandler stockSelection;
    private DependenciesOptions dependenciesOptions = new DependenciesOptions();
    private DependenciesUtil dependenciesUtil = new DependenciesUtil();
    /**
     * Filters that are independent of UI requests. Can't be overridden by UI.
     * E.g. { "visible"=true }
     */
    private Map<String, Object> stdFilters = new Hashtable<>();

    /**
     * Create the handler
     *
     * @param type Type class
     * @param dao  Data access object to use
     */
    public AbstractDataSetListHandler(Class<T> type, DataSetDao<T, ?, ?> dao) {
        this.dao = dao;
        this.type = type;

        stdFilters.put("visible", true);
        this.params = new ValueParser(stdFilters);

    }

    public String getDatasetType() {
        return this.type.getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {
    }

    /**
     * Get the display string for faces message. May be overridden by sub classes.
     *
     * @param obj object to use
     * @return display string
     */
    protected String getDisplayString(T obj) {
        try {
            return obj.getName().getDefaultValue();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get the data access object
     *
     * @return data access object
     */
    protected final DataSetDao<T, ?, ?> getDao() {
        return this.dao;
    }

    /**
     * Getter for the view. Provides the initial value.
     *
     * @return the dependencies mode
     */
    public DependenciesMode getDepOption() {
        return this.dependenciesOptions.getDependenciesOption();
    }

    /**
     * Setter for the view. This does nothing as we're using the value change listener
     * for setting the value due to an issue with values being set to null
     *
     * @param dop depMode
     */
    public void setDepOption(DependenciesMode dop) {
    }

    /**
     * Value change method that actually sets the dependency option
     *
     * @param event the event
     */
    public void depOptionChange(ValueChangeEvent event) {
        if (logger.isDebugEnabled())
            logger.debug("valuechange depoption: " + event.getNewValue());
        if (event.getNewValue() != null)
            this.dependenciesOptions.setDependenciesOption((DependenciesMode) event.getNewValue());
    }

    /**
     * Delete selected items
     */
    @Override
    public final void deleteSelected() {
        final T[] selectedItems = this.getSelectedItems();
        if (this.getSelectedItems() == null || this.getSelectedItems().length == 0) {
            return;
        }

        for (T ds : selectedItems)
            SecurityUtil.assertCan(ds, ProtectionType.DELETE);

        if (logger.isDebugEnabled())
            logger.debug("deleting selected items with dependencies option " + this.dependenciesOptions.getDependenciesOption());

        DeleteTask deleteTask = DeleteTask.from(Arrays.asList(selectedItems), this.dependenciesOptions.getDependenciesOption(),
                this.getCurrentUserOrNull());
        try {
            deleteTask.runIn(this.getGlobalQueue()).message(this, true);
            this.clearSelection();

        } catch (Throwable t) {
            t.printStackTrace();
        }

        this.reloadCount();

        // reset dependency option to default
        this.dependenciesOptions.resetDependenciesOption();
    }

    public void invokeMove(String dataTableId) {
        if (logger.isTraceEnabled())
            logger.trace("invokeMove on " + dataTableId);
        if (this.isNothingSelected() || !this.stockSelection.getCurrentStock().isRoot()) {
            if (logger.isTraceEnabled())
                logger.trace("nothing selected or no root datastock");
            return;
        }

        String sb = "PF('" + dataTableId + "MoveWarningDlg').show();";
        PrimeFaces.current().executeScript(sb);
    }

    public final void moveSelected() {
        final T[] selectedItems = this.getSelectedItems();
        if (this.getSelectedItems() == null || this.moveTarget == null) {
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("moving selected items with dependencies option " + this.dependenciesOptions.getDependenciesOption());

        Set<AbstractDataStock> dirtyDataStocks = new HashSet<>();


        for (T item : selectedItems) {
            // as permissions may be different between previous and new root data stocks,
            // all affected data stocks (root and logical) will be marked dirty
            dirtyDataStocks.add(item.getRootDataStock());
            dirtyDataStocks.addAll(item.getContainingDataStocks());

            try {
                if (logger.isTraceEnabled())
                    logger.trace("moving " + item.getDefaultName() + " " + item.getUuidAsString() + " to " + this.moveTarget.getName());

                CommonDataStockDao dsDao = new CommonDataStockDao();
                RootDataStock targetRds = dsDao.getRootDataStockById(this.moveTarget.getId());

                item.setRootDataStock(targetRds);
                dirtyDataStocks.add(targetRds);
                this.dao.merge(item);

                if (this.dependenciesOptions.getDependenciesOption() != DependenciesMode.NONE) {
                    DataSet dep = null;
                    try {
                        for (DataSet dependency : DependenciesUtil.getDependencyCluster(item,
                                this.dependenciesOptions.getDependenciesOption(), true)) {
                            dep = dependency;
                            if (logger.isDebugEnabled())
                                logger.debug("moving dependency " + dependency.getDataSetType() + " " + dependency.getUuidAsString() + " " + dependency.getDefaultName());

                            dirtyDataStocks.add(dep.getRootDataStock());
                            dirtyDataStocks.addAll(dep.getContainingDataStocks());

                            DataSetDao<DataSet, ?, ?> depDao = (DataSetDao<DataSet, ?, ?>) DataSetDao.getDao(dependency);
                            dependency.setRootDataStock(targetRds);
                            depDao.merge(dependency);
                            this.addI18NFacesMessage("facesMsg.moveDependencySuccess", FacesMessage.SEVERITY_INFO, this.getDisplayString(item), dependency.getDataSetType(), dependency.getDefaultName(), this.moveTarget.getName());
                        }
                    } catch (Exception ex) {
                        this.addI18NFacesMessage("facesMsg.moveDependencyError", FacesMessage.SEVERITY_ERROR, this.getDisplayString(item), dep.getDataSetType(), dep.getDefaultName(), this.moveTarget.getName());
                    }
                }

                // mark all datastocks affected by this transaction dirty
                for (AbstractDataStock ds : dirtyDataStocks) {
                    ds.setModified(true);
                    dsDao.merge(ds);
                }

                this.addI18NFacesMessage("facesMsg.moveSuccess", FacesMessage.SEVERITY_INFO, this.getDisplayString(item), this.moveTarget.getName());
            } catch (Exception ex) {
                logger.error("Move operation failed.", ex);
                this.addI18NFacesMessage("facesMsg.moveError", FacesMessage.SEVERITY_ERROR, this.getDisplayString(item), this.moveTarget.getName());
            }
        }
        this.clearSelection();
        this.reloadCount();
        // reset dependency option to default
        this.dependenciesOptions.resetDependenciesOption();
    }

    public List<IDataStockMetaData> getAvailableStocksForMove() {
        List<IDataStockMetaData> result = new ArrayList<>(this.stockHandler.getAvailableStocks().getRootStocksMeta());

        if (logger.isTraceEnabled()) {
            logger.trace("available root stocks: " + result);
            logger.trace("currently selected stock: " + this.stockSelection.getCurrentStock());
        }

        result.remove(this.stockSelection.getCurrentStock());

        if (logger.isTraceEnabled())
            logger.trace("available root stocks for move: " + result);

        return result;
    }

    /**
     * Get the stock selection handler
     *
     * @return stock selection handler
     */
    public final StockSelectionHandler getStockSelection() {
        return this.stockSelection;
    }

    /**
     * Set the stock selection handler
     *
     * @param stockSelection stock selection handler to set
     */
    public final void setStockSelection(StockSelectionHandler stockSelection) {
        this.stockSelection = stockSelection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long loadElementCount() {
        return this.dao.searchResultCount(this.params, this.mostRecentVersionOnly, this.stockSelection.getCurrentStockAsArray());
    }

    public long loadTotalElementCount() {
        return this.dao.searchResultCount(new ValueParser(stdFilters), this.mostRecentVersionOnly, this.stockSelection.getCurrentStockAsArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        filters.putAll(stdFilters);
        this.params = new ValueParser(filters);

        try {
            for (String s : this.params.keySet()) {
                filters.put(s, this.params.get(s));
                if (logger.isTraceEnabled())
                    logger.trace("param " + s + ": " + this.params.get(s));
            }
        } catch (NullPointerException e) {
        }

        if (logger.isTraceEnabled())
            logger.trace("loading data from stock " + this.stockSelection.getCurrentStockAsArray()[0].getName());

        return this.dao.search(this.type, this.params, first, pageSize, sortField, !SortOrder.DESCENDING.equals(sortOrder), this.mostRecentVersionOnly,
                this.stockSelection.getCurrentStockAsArray());
    }

    public boolean isMostRecentVersionOnly() {
        return mostRecentVersionOnly;
    }

    public void setMostRecentVersionOnly(boolean arg) {
        this.mostRecentVersionOnly = arg;
    }

    public StockHandler getStockHandler() {
        return stockHandler;
    }

    public void setStockHandler(StockHandler stockHandler) {
        this.stockHandler = stockHandler;
    }

    public DependenciesOptions getDependenciesOptions() {
        return dependenciesOptions;
    }

    public IDataStockMetaData getMoveTarget() {
        return moveTarget;
    }

    public void setMoveTarget(IDataStockMetaData moveTarget) {
        this.moveTarget = moveTarget;
    }

}
