package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataStockRestriction;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRightDao;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.ReorderEvent;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.*;
import java.util.stream.Collectors;

import static de.iai.ilcd.webgui.controller.admin.UserListHandler.log;
import static org.primefaces.component.datatable.DataTableBase.PropertyKeys.sortField;

/**
 * Admin handler for stock list
 */
@ManagedBean
@ViewScoped
public class StockListHandler extends AbstractAdminListHandler<AbstractDataStock> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8296677018448858250L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(StockListHandler.class);
    /**
     * DAO for model access
     */
    private final CommonDataStockDao dao = new CommonDataStockDao();
    private boolean showHidden = ConfigurationService.INSTANCE.getDisplayConfig().isShowHiddenDatastocksDefault();
    private DataStockRestriction restriction = DataStockRestriction.BOTH;
    private List<AbstractDataStock> viewCache;
    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;
    /**
     * ProcessListHandler for handling registration
     */
    @ManagedProperty(value = "#{processListHandler}")
    private ProcessListHandler processListHandler;

    /**
     * invoked on manual reorder of rows, the new order will be persisted
     *
     * @param event
     */
    public void onRowReorder(ReorderEvent event) {
        final var dataTable = (DataTable) Objects.requireNonNull(event.getComponent());
        final var sortField = dataTable.getSortField();
        final var sortOrder = sortField != null ? SortOrder.valueOf(dataTable.getSortOrder()) : null;
        final var pageSize = dataTable.getRows();
        final var totalCount = dataTable.getRowCount();

        /*
        A note on terminology within this method

        Reordering is done by drag and drop of rows.
        The action singles out two special rows (=stocks):
        The one which is dragged and eventually dropped and the other on which the former is dropped.
        We will refer to the former as *moving* row (move stock) and the latter as *targeted* row (target stock).

        The order is modelled by a stock property called *ordinal*.
        The ordinal of the move stock will be set by the target ordinal and the target stock's ordinal needs to shift.
        Transitively, other stock's ordinals need to shift, too.
        The shift affects all stocks that lie "in between" the move and target stocks.

        The concept of "in between" is not necessarily defined by ordinals, it's defined by what the user sees (or has filtered out).
        The correct notion of the "in between" is, in fact, derived from the query used to populate the table and can span multiple pages.
        We call this the *query-specific natural ordinal* or index.

        Natural, because -- to make things worse -- we use negative/inverted ordinals in the db.
        That means the actual ordinals used, will be offset by the total number of stocks in the db.

        As a last remark, it is generally favorable to update all ordinals, even the ones that do not need shifting.
        Sorts demand a global update anyway.
        Without sorts, we could determine the affected pages (favourable over single page approach with "appropriate" size).
        But this will unnecessarily (as of now) increase the complexity of this already complex method.
         */
        final var moveStock = viewCache.get(event.getFromIndex() % pageSize);
        final var moveStockId = moveStock.getId();
        final var targetStockId = viewCache.get(event.getToIndex() % pageSize).getId();
        int querySpecificMoveStockIndex = Integer.MAX_VALUE;
        int querySpecificTargetStockIndex = Integer.MAX_VALUE;
        for (int i = 0; i * pageSize < totalCount; i++) {
            final var firstIndex = i * pageSize;
            final var stocksInPage = lazyLoad(firstIndex, pageSize, sortField, sortOrder, new HashMap<>(), false); // don't use filters, reordering needs propagation to all stocks

            final var updatedStocks = new ArrayList<AbstractDataStock>();
            for (int k = 0; k < stocksInPage.size(); k++) {
                final var naturalOrdinal = k + firstIndex; // you could call this the query specific index
                final var currentStock = stocksInPage.get(k);

                if (currentStock.getId().equals(moveStockId)) {
                    querySpecificMoveStockIndex = naturalOrdinal;
                } else if (currentStock.getId().equals(targetStockId)) {
                    querySpecificTargetStockIndex = naturalOrdinal;
                }

                int shiftOrdinal;
                if (querySpecificMoveStockIndex < naturalOrdinal && naturalOrdinal <= querySpecificTargetStockIndex) { // we're one entry short -> left shift
                    shiftOrdinal = naturalOrdinal - 1 - totalCount;

                } else if (querySpecificTargetStockIndex <= naturalOrdinal && naturalOrdinal < querySpecificMoveStockIndex) { // we've got one entry too much -> right shift
                    shiftOrdinal = naturalOrdinal + 1 - totalCount;

                } else if (currentStock.getId().equals(moveStockId)) {
                    continue; // we may not know the toIndex yet, which we need to set the from-stock's ordinal

                } else {
                    shiftOrdinal = naturalOrdinal - totalCount;
                }
                currentStock.getDisplayProperties().setOrdinal(shiftOrdinal);
                updatedStocks.add(currentStock);
            }

            if (querySpecificTargetStockIndex < Integer.MAX_VALUE) { // it has been set (it's known)
                final var targetOrdinal = querySpecificTargetStockIndex - totalCount;
                final var actualOrdinal = moveStock.getDisplayProperties().getOrdinal(); // can be null
                if (!Objects.equals(actualOrdinal, targetOrdinal)) { // move stock has not yet been updated
                    moveStock.getDisplayProperties().setOrdinal(targetOrdinal);
                    updatedStocks.add(moveStock); // let's sneak it in to be persistence-merged alongside the others
                }
            }

            try {
                dao.merge(updatedStocks);

            } catch (MergeException e) {
                log.error("Failed to update stocks: {}", stocksInPage.stream()
                        .map(AbstractDataStock::getId)
                        .collect(Collectors.toList()), e);

                try {
                    dao.merge(updatedStocks);

                } catch (MergeException e1) {
                    throw new RuntimeException(String.format("Failed to update stocks: %s", stocksInPage.stream()
                            .map(AbstractDataStock::getId)
                            .collect(Collectors.toList())), e1);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected long loadElementCount() {
        Map<String, Object> filters = new HashMap<String, Object>();
        if (!this.showHidden) {
            filters.put("displayProperties.hidden", false);
        } else
            filters.put("displayProperties.hidden", true);

        switch (restriction) {
            case LOGICAL:
                filters.put("stockProperties.logical", true);
                break;
            case ROOT:
                filters.put("stockProperties.logical", false);
                break;
            default:
                ;
        }
        return this.dao.getAllCount(filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {
    }

    /**
     * Get the selected stocks.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#getSelectedItems() getSelectedItems} in Facelets (see
     * it's documentation for the reason)
     * </p>
     *
     * @return selected root data stocks
     */
    public AbstractDataStock[] getSelectedStocks() {
        return super.getSelectedItems();
    }

    /**
     * Set the selected stocks.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#setSelectedItems(Object[]) setSelectedItems} in Facelets
     * (see it's documentation for the reason)
     * </p>
     *
     * @param selected selected root data stocks
     */
    public void setSelectedStocks(AbstractDataStock[] selected) {
        super.setSelectedItems(selected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSelected() {
        final AbstractDataStock[] selected = this.getSelectedItems();
        if (selected == null) {
            return;
        }

        StockAccessRightDao sarDao = new StockAccessRightDao();
        for (AbstractDataStock rds : selected) {
            // Default root stock not deletable
            if (ObjectUtils.equals(rds.getId(), SodaUtil.DEFAULT_ROOTSTOCK_ID)) {
                continue;    // not selectable in facelet and not deletable (double check)
            }
            List<StockAccessRight> sars = sarDao.get(rds);
            try {
                sarDao.remove(sars);
                this.dao.remove(rds);
                this.addI18NFacesMessage("facesMsg.removeSuccess", FacesMessage.SEVERITY_INFO, rds.getLongTitle().getDefaultValue() + " (" + rds.getName()
                        + ")");
            } catch (Exception e) {
                this.addI18NFacesMessage("facesMsg.removeError", FacesMessage.SEVERITY_ERROR, rds.getLongTitle().getDefaultValue() + " (" + rds.getName()
                        + ")");
            }
        }
        this.dirty.stockModified();
        this.clearSelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractDataStock> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return lazyLoad(first, pageSize, sortField, sortOrder, filters, true);
    }

    public List<AbstractDataStock> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters, boolean updateViewCache) {
        if (!this.showHidden) {
            filters.put("displayProperties.hidden", false);
        } else
            filters.put("displayProperties.hidden", true);

        switch (restriction) {
            case LOGICAL:
                filters.put("stockProperties.logical", true);
                break;
            case ROOT:
                filters.put("stockProperties.logical", false);
                break;
            default:
                ;
        }

        List<AbstractDataStock> list = this.dao.get(first, pageSize, sortField, sortOrder, filters);

        if (updateViewCache) {
            viewCache = list;
        }

        return list;
    }

    public void registerSelected() {
        List<Process> selectedProcesses = new ArrayList<Process>();

        // select all newest processes in selected stock(s)
        ProcessDao pDao = new ProcessDao();
        long count = pDao.getCount(this.getSelectedStocks(), null, true);
        selectedProcesses.addAll(pDao.getDataSets(this.getSelectedStocks(), null, true, 0, (int) count));
        this.processListHandler.setSelectedItems(selectedProcesses.toArray(new Process[0]));
        this.processListHandler.registerSelected();
    }

    /**
     * Get the dirty flag bean
     *
     * @return dirty flag bean
     */
    public DirtyFlagBean getDirty() {
        return this.dirty;
    }

    /**
     * Set the dirty flag bean
     *
     * @param dirty the dirty flag bean
     */
    public void setDirty(DirtyFlagBean dirty) {
        this.dirty = dirty;
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }


    public ProcessListHandler getProcessListHandler() {
        return processListHandler;
    }


    public void setProcessListHandler(ProcessListHandler processListHandler) {
        this.processListHandler = processListHandler;
    }

    public DataStockRestriction getDataStockRestriction() {
        return restriction;
    }

    public void setDataStockRestriction(DataStockRestriction restriction) {
        this.restriction = restriction;
    }


    public List<AbstractDataStock> getViewCache() {
        return viewCache;
    }

}
