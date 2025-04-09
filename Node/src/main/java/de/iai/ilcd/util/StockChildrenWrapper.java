package de.iai.ilcd.util;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignBatchMethod;
import de.iai.ilcd.service.task.dataSetAssignment.assign.AssignBatchMethod;
import de.iai.ilcd.service.task.dataSetAssignment.unassign.UnassignBatchMethod;
import de.iai.ilcd.util.DataSetSelectableDataModel.IDataSetLoader;
import de.iai.ilcd.webgui.controller.admin.StockHandler;
import de.iai.ilcd.webgui.controller.admin.export.DBConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.SortOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Lazy data model for children (assigned data sets) of a stock as well as
 * detachment management for the stock.
 * </p>
 * <p>
 * <b>Purpose:</b> In order to required as little code as possible, this lazy
 * model also is responsible for the detachment of it's selected elements from
 * the stock. <br />
 * This way, there is no need for separate detachment methods for each data type
 * and no additional ui:params must be injected into the common stockEntry.xhtml
 * facelet - only the lazy model is required to do all actions.
 * </p>
 *
 * @param <E> data set type
 */
public class StockChildrenWrapper<E extends DataSet> implements IDatasetListBackingBean<E>, Serializable {

    static final Logger logger = LogManager.getLogger(StockChildrenWrapper.class);
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -6538508118524915048L;
    /**
     * Parent stock handler
     */
    private final StockHandler stockHandler;

    /**
     * Model with current children of data stock
     */
    private final DataSetSelectableDataModel<E> contentModel;

    /**
     * Model with potential candidate accessible by current user
     */
    private final DataSetSelectableDataModel<E> candidateModel;


    private DependenciesUtil dependenciesUtil = new DependenciesUtil();

    private DependenciesOptions dependenciesOptions = new DependenciesOptions();

    private final UserDao userdao = new UserDao();

    private final JdbcTemplate jdbcTemplate = DBConfig.getJdbcTemplate();

    /**
     * Create the lazy data model
     *
     * @param handler        Parent stock handler
     * @param contentModel   model with content
     * @param candidateModel model with candidates
     */
    public StockChildrenWrapper(StockHandler handler, DataSetSelectableDataModel<E> contentModel,
                                DataSetSelectableDataModel<E> candidateModel) {
        super();
        this.stockHandler = handler;
        this.contentModel = contentModel;
        this.candidateModel = candidateModel;
    }

    /**
     * Create the stock children wrapper for process data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<Process> getProcessWrapper(StockHandler handler) {
        final ProcessDao dao = new ProcessDao();
        final DataSetSelectableDataModel.ProcessSelectableDataModel contentModel = new DataSetSelectableDataModel.ProcessSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.ProcessSelectableDataModel candidateModel = new DataSetSelectableDataModel.ProcessSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<Process>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for LCIA method data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<LCIAMethod> getLCIAMethodWrapper(StockHandler handler) {
        final LCIAMethodDao dao = new LCIAMethodDao();
        final DataSetSelectableDataModel.LCIAMethodSelectableDataModel contentModel = new DataSetSelectableDataModel.LCIAMethodSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.LCIAMethodSelectableDataModel candidateModel = new DataSetSelectableDataModel.LCIAMethodSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<LCIAMethod>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for elementary flow data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<ElementaryFlow> getElementaryFlowWrapper(StockHandler handler) {
        final ElementaryFlowDao dao = new ElementaryFlowDao();
        final DataSetSelectableDataModel.ElementaryFlowSelectableDataModel contentModel = new DataSetSelectableDataModel.ElementaryFlowSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.ElementaryFlowSelectableDataModel candidateModel = new DataSetSelectableDataModel.ElementaryFlowSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<ElementaryFlow>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for flow data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<ProductFlow> getProductFlowWrapper(StockHandler handler) {
        final ProductFlowDao dao = new ProductFlowDao();
        final DataSetSelectableDataModel.ProductFlowSelectableDataModel contentModel = new DataSetSelectableDataModel.ProductFlowSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.ProductFlowSelectableDataModel candidateModel = new DataSetSelectableDataModel.ProductFlowSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<ProductFlow>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for flow property data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<FlowProperty> getFlowPropertyWrapper(StockHandler handler) {
        final FlowPropertyDao dao = new FlowPropertyDao();
        final DataSetSelectableDataModel.FlowPropertySelectableDataModel contentModel = new DataSetSelectableDataModel.FlowPropertySelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.FlowPropertySelectableDataModel candidateModel = new DataSetSelectableDataModel.FlowPropertySelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<FlowProperty>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for unit group data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<UnitGroup> getUnitGroupWrapper(StockHandler handler) {
        final UnitGroupDao dao = new UnitGroupDao();
        final DataSetSelectableDataModel.UnitGroupSelectableDataModel contentModel = new DataSetSelectableDataModel.UnitGroupSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.UnitGroupSelectableDataModel candidateModel = new DataSetSelectableDataModel.UnitGroupSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<UnitGroup>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for source data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<Source> getSourceWrapper(StockHandler handler) {
        final SourceDao dao = new SourceDao();
        final DataSetSelectableDataModel.SourceSelectableDataModel contentModel = new DataSetSelectableDataModel.SourceSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.SourceSelectableDataModel candidateModel = new DataSetSelectableDataModel.SourceSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<Source>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for contact data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<Contact> getContactWrapper(StockHandler handler) {
        final ContactDao dao = new ContactDao();
        final DataSetSelectableDataModel.ContactSelectableDataModel contentModel = new DataSetSelectableDataModel.ContactSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.ContactSelectableDataModel candidateModel = new DataSetSelectableDataModel.ContactSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<Contact>(handler, contentModel, candidateModel);
    }

    /**
     * Create the stock children wrapper for LifeCycleModel data sets
     *
     * @param handler the stock handler to use
     * @return created wrapper
     */
    public static StockChildrenWrapper<LifeCycleModel> getLifeCycleModelWrapper(StockHandler handler) {
        final LifeCycleModelDao dao = new LifeCycleModelDao();
        final DataSetSelectableDataModel.LifeCycleModelSelectableDataModel contentModel = new DataSetSelectableDataModel.LifeCycleModelSelectableDataModel(
                getContentLoader(dao, handler));
        final DataSetSelectableDataModel.LifeCycleModelSelectableDataModel candidateModel = new DataSetSelectableDataModel.LifeCycleModelSelectableDataModel(
                getCandidateLoader(dao, handler));
        contentModel.setShowDeleteButton(handler.getEntry().isRoot());
        return new StockChildrenWrapper<LifeCycleModel>(handler, contentModel, candidateModel);
    }

    /**
     * Get a loader for the content (already assigned children) for stock and
     * DAO
     *
     * @param <T>     type of data set
     * @param dao     DAO to load data from
     * @param handler handler to get stock information from
     * @return created loader
     */
    public static <T extends DataSet> IDataSetLoader<T> getContentLoader(final DataSetDao<T, ?, ?> dao,
                                                                         final StockHandler handler) {
        final AbstractDataStock entry = handler.getEntry();
        final IDataStockMetaData[] metaArr = entry != null ? new IDataStockMetaData[]{entry}
                : new IDataStockMetaData[0];

        return new IDataSetLoader<T>() {

            @Override
            public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                                Map<String, Object> filters, IDataStockMetaData dsMeta) {
                ValueParser params = new ValueParser(filters);
                return dao.lsearch(params, first, pageSize, sortField, !SortOrder.DESCENDING.equals(sortOrder),
                        handler.isMostRecentVersionOnly(), metaArr);
            }

            @Override
            public long loadCount(IDataStockMetaData dsMeta, Map<String, Object> filters) {
                ValueParser params = new ValueParser(filters);
                return metaArr.length > 0 ? dao.searchResultCount(params, handler.isMostRecentVersionOnly(), metaArr)
                        : 0;
            }

        };
    }

    /**
     * Get a loader for the candidates (data sets that can be added to stock by
     * current user) for stock and DAO
     *
     * @param <T>     type of data set
     * @param dao     DAO to load data from
     * @param handler handler to get stock information from
     * @return created loader
     */
    public static <T extends DataSet> IDataSetLoader<T> getCandidateLoader(final DataSetDao<T, ?, ?> dao,
                                                                           final StockHandler handler) {
        final AbstractDataStock entry = handler.getEntry();

        return new IDataSetLoader<T>() {

            @Override
            public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                                Map<String, Object> filters, IDataStockMetaData dsMeta) {
                final IDataStockMetaData[] metaArr = dsMeta != null ? new IDataStockMetaData[]{dsMeta}
                        : handler.getCandidateStocks().toArray(new IDataStockMetaData[0]);
                ValueParser params = new ValueParser(filters);
                return dao.lsearch(params, first, pageSize, sortField, !SortOrder.DESCENDING.equals(sortOrder),
                        handler.isMostRecentVersionOnly(), metaArr, entry);
            }

            @Override
            public long loadCount(IDataStockMetaData dsMeta, Map<String, Object> filters) {
                ValueParser params = new ValueParser(filters);
                final IDataStockMetaData[] metaArr = dsMeta != null ? new IDataStockMetaData[]{dsMeta}
                        : handler.getCandidateStocks().toArray(new IDataStockMetaData[0]);
                return metaArr.length > 0
                        ? dao.searchResultCount(params, handler.isMostRecentVersionOnly(), metaArr, entry) : 0;
            }
        };
    }

    /**
     * Detach the selected entries from the data stock
     */
    public void detachSelectedFromStock() {
        detachFromStock(this.stockHandler.getEntry(), this.contentModel.getSelected(), true);
    }

    public void isBatch() {
        if (candidateModel != null && candidateModel.getSelected() != null)
            return;
        return;
    }

    /**
     * Attach the selected entries to the data stock
     */
    public void attachSelectedToStock() {
        attachToStock(this.stockHandler.getEntry(), this.candidateModel.getSelected(), true);
    }

    /**
     * Get the candidate model (model with all data sets that can be added to
     * the stock by current user)
     *
     * @return candidate model
     */
    public DataSetSelectableDataModel<E> getCandidateModel() {
        return this.candidateModel;
    }

    /**
     * Get the content model (model with all data sets already in the stock)
     *
     * @return content model
     */
    public DataSetSelectableDataModel<E> getContentModel() {
        return this.contentModel;
    }

    public StockHandler getStockHandler() {
        return stockHandler;
    }

    public List<String> getLocations() {
        List<String> locations = new ArrayList<String>();

        for (GeographicalArea a : (new ProcessDao()).getUsedLocations())
            locations.add(a.getAreaCode());

        Collections.sort(locations);

        return locations;
    }

    public DependenciesOptions getDependenciesOptions() {
        return this.dependenciesOptions;
    }

    public List<IDataStockMetaData> getAvailableStocksForMove() {
        return null;
    }

    public IDataStockMetaData getMoveTarget() {
        return null;
    }

    public void setMoveTarget(IDataStockMetaData moveTarget) {
    }

    /**
     * Getter for the view. Provides the initial value.
     *
     * @return
     */
    public DependenciesMode getDepOption() {
        return this.dependenciesOptions.getDependenciesOption();
    }

    /**
     * Setter for the view. This does nothing as we're using the value change listener
     * for setting the value due to an issue with values being set to null
     *
     * @param dop
     */
    public void setDepOption(DependenciesMode dop) {
    }

    /**
     * Value change method that actually sets the dependency option
     *
     * @param event
     */
    public void depOptionChange(ValueChangeEvent event) {
        if (logger.isDebugEnabled())
            logger.debug("valuechange depoption: " + event.getNewValue());
        if (event.getNewValue() != null)
            this.dependenciesOptions.setDependenciesOption((DependenciesMode) event.getNewValue());
    }

    public List<Source> getDatabases() {
        SourceDao dao = new SourceDao();
        return dao.getDatabases();
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        //creating a map containing all data tables of assign data sets tab together with component ID
        Map<String, DataTable> dataTables = new HashMap<String, DataTable>();
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctProcessDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctLCIAMDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctEFlowDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctPFlowDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctFlowPropDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctUnitGrDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctSourceDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctContactDataTable", dataTables);
        dataTables = this.putDataTableMap("generalForm:stockTabs:dataSetTabView:ctLifecyclemodelDataTable", dataTables);

        //Going through each data table and clearing each filter
        Iterator<Entry<String, DataTable>> iterator = dataTables.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, DataTable> pair = (Map.Entry<String, DataTable>) iterator.next();
            DataTable dataTable = pair.getValue();
            String componentId = pair.getKey();
            if (!dataTable.getFilterBy().isEmpty()) {

                dataTable.reset();
                PrimeFaces.current().ajax().update("form:dataTable");
            }
        }
    }

    private Map<String, DataTable> putDataTableMap(String componentId, Map<String, DataTable> dataTables) {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(componentId);
        dataTables.put(componentId, dataTable);

        return dataTables;
    }

    /**
     * This method is only suitable for single batches, since it resets the dependencyMode after completion
     *
     * @param abstractDataStockTarget
     * @param dataSetArray
     * @param showMessage
     */
    public void attachToStock(AbstractDataStock abstractDataStockTarget, DataSet[] dataSetArray, boolean showMessage) {
        this.attachToStock(abstractDataStockTarget, dataSetArray, showMessage, true);
    }

    public void attachToStock(AbstractDataStock abstractDataStockTarget, DataSet[] dataSetArray, boolean showMessage, boolean resetDependenciesOption) {
        if (!(abstractDataStockTarget instanceof DataStock)) {

            throw new RuntimeException(String.format(
                    "Invalid stock type: '%s'. Attaching/Assigning is a concept for logical target stocks. Root targets require 'movement' of datasets.",
                    abstractDataStockTarget.getClass().getName()));

        } else {
            final var logicalTargetStockMetaData = new DataStockMetaData(abstractDataStockTarget);

            this.attachDetach(logicalTargetStockMetaData, dataSetArray, showMessage, resetDependenciesOption, new AssignBatchMethod(jdbcTemplate, logicalTargetStockMetaData));
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        final var stock = em.find(DataStock.class, abstractDataStockTarget.getId());
        this.stockHandler.setEntry(stock); // Updating the stock entry

        // reset dependency option to default
        if (resetDependenciesOption)
            this.dependenciesOptions.resetDependenciesOption();
    }

    /**
     * This method is only suitable for single batches, since it resets the dependencyMode after completion
     *
     * @param abstractDataStockTarget
     * @param dataSetArray
     * @param showMessage
     */
    public void detachFromStock(AbstractDataStock abstractDataStockTarget, DataSet[] dataSetArray, boolean showMessage) {
        this.detachFromStock(abstractDataStockTarget, dataSetArray, showMessage, true);
    }

    public void detachFromStock(AbstractDataStock abstractDataStockTarget, DataSet[] dataSetArray, boolean showMessage,
                                boolean resetDependenciesOption) {
        if (!(abstractDataStockTarget instanceof DataStock)) {

            throw new RuntimeException(String.format(
                    "Invalid stock type: '%s'. Attaching/Assigning is a concept for logical target stocks. Root targets require 'movement' of datasets.",
                    abstractDataStockTarget.getClass().getName()));

        } else {
            final var logicalTargetStockMetaData = new DataStockMetaData(abstractDataStockTarget);

            this.attachDetach(logicalTargetStockMetaData, dataSetArray, showMessage, resetDependenciesOption, new UnassignBatchMethod(jdbcTemplate, logicalTargetStockMetaData));
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        final var stock = em.find(DataStock.class, abstractDataStockTarget.getId());
        this.stockHandler.setEntry(stock); // Updating the stock entry

        // reset dependency option to default
        if (resetDependenciesOption)
            this.dependenciesOptions.resetDependenciesOption();

        }

    private void attachDetach(DataStockMetaData targetStock, DataSet[] dataSetArray, boolean showMessage, boolean resetDependenciesOption, AssignUnassignBatchMethod method) {
        if (targetStock.isRoot()) {

            throw new RuntimeException(String.format(
                    "Invalid stock type: '%s'. Attaching/Assigning is a concept for logical target stocks. Root targets require 'movement' of datasets.",
                    targetStock.getClass().getName()));
        } else {
            final var depMode = this.dependenciesOptions.getDependenciesOption();
            if (logger.isDebugEnabled())
                logger.debug("dependencies mode is {}", depMode);

            final var alreadyAssigned = new HashSet<String>();
            for (DataSet dataset : dataSetArray) {
                try {
                    final var associatedRefs = DependenciesUtil.getDependencyCluster(dataset, depMode, alreadyAssigned).stream()
                            .map(DataSetReference::new)
                            .collect(Collectors.toSet());

                    method.applyTo(associatedRefs);

                    alreadyAssigned.addAll(associatedRefs.stream()
                            .map(DataSetReference::getUuidAsString)
                            .collect(Collectors.toList()));

                    if (showMessage) {
                        this.stockHandler.addI18NFacesMessage((method instanceof AssignBatchMethod) ? "facesMsg.assignDatasetSuccess" : "facesMsg.removeSuccess",
                                FacesMessage.SEVERITY_INFO, dataset.getDefaultName());
                    }

                } catch (Exception e) {
                    logger.error("Error occurred during attachment of dataset {}", dataset, e);
                    this.stockHandler.addI18NFacesMessage((method instanceof AssignBatchMethod) ? "facesMsg.assignDatasetError" : "facesMsg.removeError", FacesMessage.SEVERITY_ERROR,
                            dataset.getDefaultName());
                }
            }
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        final var stock = em.find(DataStock.class, targetStock.getId());
        this.stockHandler.setEntry(stock); // Updating the stock entry

        // reset dependency option to default
        if (resetDependenciesOption)
            this.dependenciesOptions.resetDependenciesOption();
    }


}