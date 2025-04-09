package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.PersistException;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.service.exception.JobNotScheduledException;
import de.iai.ilcd.service.exception.StockBusyException;
import de.iai.ilcd.service.exception.UserNotExistingException;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignTask;
import de.iai.ilcd.service.task.dataSetAssignment.assign.AssignTask;
import de.iai.ilcd.service.task.dataSetAssignment.unassign.UnassignTask;
import de.iai.ilcd.service.util.DataSetsTypes;
import de.iai.ilcd.service.util.JobType;
import de.iai.ilcd.util.DataSetSelectableDataModel;
import de.iai.ilcd.util.DependenciesOptions;
import de.iai.ilcd.util.StockChildrenWrapper;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.util.BatchAssignMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.primefaces.event.FileUploadEvent;
import org.quartz.SchedulerException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * Handler to assign and remove entries.
 *
 * @author alhajras
 */
@ManagedBean
@ViewScoped
public class AssignRemoveDataSetHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = -2499183803964247343L;

    /*
     * The logger for logging data
     */
    protected final Logger log = LogManager.getLogger(this.getClass());

    /*
     * The mode of the assign/remove batch operation: all, none, secondary
     */
    public String selectMode = "2";

    /*
     * Shows the job redirecting button after batching
     */
    public boolean showJobsButton = false;

    @ManagedProperty("#{excelSelectionHandler}")
    private ExcelSelectionHandler excelSelectionHandler = new ExcelSelectionHandler();

    private Boolean isFileKnown = false;

    private Boolean fetchFromAllStocks = false;

    /*
     * The decidable dependency options
     */
    private DependenciesOptions depOptions = new DependenciesOptions();

    private BatchAssignMode selectionBy = BatchAssignMode.DATA_SET_TYPE;

    /*
     * The handler for managing source stocks
     */
    @ManagedProperty(value = "#{stockListHandler}")
    private StockListHandler stockListHandler = new StockListHandler();

    @ManagedProperty(value = "#{stockHandler}")
    private StockHandler stockHandler = new StockHandler();

    /*
     * Flag for checking whether current view is editable
     */
    private boolean edit = false;

    /*
     * Checks whether batching the big datasets is needed
     */
    private boolean batchNeeded = false;

    /*
     * Checks if the user manually clicks on the create job button
     */
    private boolean isForced = false;

    /*
     * The number of datasets selected in from the table
     */
    private long datasetsSelectedCount = 0;

    private List<DataSetsTypes> dataSets = new ArrayList<>();

    private ArrayList<DataSetsTypes> selectedDataSetTypes = new ArrayList<>();

    public AssignRemoveDataSetHandler() {
        super();
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Gets the dependencies option.
     *
     * @return dependencies option for push configuration
     */
    public DependenciesOptions getDependenciesOptions() {
        return this.depOptions;
    }

    public DependenciesMode getDepOption() {
        log.trace("dependencies mode is {} ", this.depOptions.getDependenciesOption());
        return this.depOptions.getDependenciesOption();
    }

    /**
     * Setter for the view. This does nothing as we're using the value change listener
     * for setting the value due to an issue with values being set to null
     *
     * @param dop depMode
     */
    public void setDepOption(DependenciesMode dop) {
    }

    public void depOptionChange(ValueChangeEvent event) {
        if (event.getNewValue() != null)
            this.depOptions.setDependenciesOption((DependenciesMode) event.getNewValue());
    }

    /**
     * Gets the current stock handler for managing available source stocks.
     *
     * @return A StockHandler for managing available source stocks
     */
    public StockListHandler getStockListHandler() {
        return stockListHandler;
    }

    /**
     * Sets the StockHandler for managing source stocks.
     *
     * @param stockListHandler The stock handler for managing source stocks
     */
    public void setStockListHandler(StockListHandler stockListHandler) {
        this.stockListHandler = stockListHandler;
    }

    /**
     * Detach Selected datasets from stock
     */
    public void detachSelectedFromStock() {
        if (stockListHandler.getSelectedStocks() == null || stockListHandler.getSelectedStocks().length == 0) {
            this.addI18NFacesMessage("facesMsg.admin.batchAssign.noStocksSelected", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (selectionBy == BatchAssignMode.EXCEL_IMPORT) {
            attachDetachExcelSelectionFromStock(JobType.DETACH);
        } else {
            if (selectedDataSetTypes == null || selectedDataSetTypes.isEmpty()) {
                this.addI18NFacesMessage("facesMsg.admin.batchAssign.selectionByType.pleaseSelectType", FacesMessage.SEVERITY_ERROR);
                return;
            }


            String standardCatchLogMessage = "Error when trying to detach data sets to data stock "
                    + stockHandler.getEntry().getUuidAsString();
            try {
                AbstractDataStock abstractTargetStock = stockHandler.getEntry();
                if (!abstractTargetStock.isRoot()) {
                    UnassignTask unassignTask = UnassignTask.fromTypes((DataStock) stockHandler.getEntry(),
                            selectedDataSetTypes,
                            new HashSet<>(Arrays.asList(stockListHandler.getSelectedStocks())),
                            stockHandler.getProcessWrapper().getDepOption(),
                            this.getCurrentUserOrNull());
                    unassignTask.runIn(this.getGlobalQueue()).message(this, true);
                }

                showJobsButton = true;

            } catch (StockBusyException e) {
                this.addI18NFacesMessage("facesMsg.stock.isBusy", FacesMessage.SEVERITY_ERROR);
                log.error(standardCatchLogMessage, e);

            } catch (NullPointerException npe) {
                if (stockHandler.getEntry().isRoot())
                    log.error("Attempted to 'assign' data sets to to root stock, which is not allowed.", npe);
                else
                    log.error(standardCatchLogMessage, npe);

            } catch (Throwable throwable) {
                log.error(standardCatchLogMessage, throwable);

            }
        }
    }

    /**
     * Attach Selected datasets to stock
     */
    public void attachSelectedToStock() {
        if (stockListHandler.getSelectedStocks() == null || stockListHandler.getSelectedStocks().length == 0) {
            this.addI18NFacesMessage("facesMsg.admin.batchAssign.noStocksSelected", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (selectionBy == BatchAssignMode.EXCEL_IMPORT) {
            attachDetachExcelSelectionFromStock(JobType.ATTACH);
        } else {
            if (selectedDataSetTypes == null || selectedDataSetTypes.isEmpty()) {
                this.addI18NFacesMessage("facesMsg.admin.batchAssign.selectionByType.pleaseSelectType", FacesMessage.SEVERITY_ERROR);
                return;
            }

            String standardCatchLogMessage = "Error when trying to attach data sets to data stock "
                    + stockHandler.getEntry().getUuidAsString();
            try {
                AbstractDataStock abstractTargetStock = stockHandler.getEntry();
                if (!abstractTargetStock.isRoot()) {
                    AssignTask assignTask = AssignTask.fromTypes((DataStock) stockHandler.getEntry(),
                            selectedDataSetTypes,
                            new HashSet<>(Arrays.asList(stockListHandler.getSelectedStocks())),
                            getDepOption(),
                            this.getCurrentUserOrNull());
                    assignTask.runIn(this.getGlobalQueue()).message(this, true);
                }
                showJobsButton = true;

            } catch (StockBusyException e) {
                this.addI18NFacesMessage("facesMsg.stock.isBusy", FacesMessage.SEVERITY_ERROR);
                log.error(standardCatchLogMessage, e);

            } catch (NullPointerException npe) {
                if (stockHandler.getEntry().isRoot())
                    log.error("Attempted to 'assign' data sets to to root stock, which is not allowed.", npe);
                else
                    log.error(standardCatchLogMessage, npe);

            } catch (Throwable throwable) {
                log.error(standardCatchLogMessage, throwable);

            }
        }
    }

    public void handleExcelImport(FileUploadEvent event) {
        try {
            excelSelectionHandler.handleUpload(event);
            this.isFileKnown = true;
        } catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
            this.addI18NFacesMessage("admin.assignRemove.severeUploadError", e);
        } catch (DataFormatException e) {
            this.addI18NFacesMessage("facesMsg.import.fileuploadError2", e);
        }
    }

    public void attachDetachExcelSelectionFromStock(JobType jobType) {
        Set<GlobalReference> parsedReferences = excelSelectionHandler.getParsedReferences();
        if (parsedReferences == null) {
            this.addI18NFacesMessage("facesMsg.admin.batchAssign.selectionByExcelImport.pleaseSelectFileAndImport", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (parsedReferences.size() == 0) {
            this.addI18NFacesMessage("facesMsg.admin.batchAssign.selectionByExcelImport.noReferencesParsed", FacesMessage.SEVERITY_ERROR);
            return;
        }

        AbstractDataStock[] selectedSourceStocks = stockListHandler.getSelectedStocks() != null ?
                stockListHandler.getSelectedStocks() :
                new AbstractDataStock[0];

        String standardCatchLogMessage = "Error when trying to " + jobType.name() + " data sets to data stock "
                + stockHandler.getEntry().getUuidAsString();
        try {
            AbstractDataStock targetStock = stockHandler.getEntry();
            if (!targetStock.isRoot()) {
                AssignUnassignTask task = null;
                if (JobType.ATTACH.equals(jobType))
                    task = AssignTask.fromReferences((DataStock) targetStock, parsedReferences,
                            Arrays.asList(selectedSourceStocks), getDepOption(), this.getCurrentUserOrNull());
                else if (JobType.DETACH.equals(jobType))
                    task = UnassignTask.fromReferences((DataStock) targetStock, parsedReferences,
                            Arrays.asList(selectedSourceStocks), getDepOption(), this.getCurrentUserOrNull());

                if (task == null)
                    throw new IllegalStateException("Task should have been initialised! Maybe there's an unexpected JobType that needs to be dealt with.");
                task.runIn(this.getGlobalQueue()).message(this, true);
            }
            showJobsButton = true;

        } catch (StockBusyException e) {
            this.addI18NFacesMessage("facesMsg.stock.isBusy", FacesMessage.SEVERITY_ERROR);
            log.error(standardCatchLogMessage, e);

        } catch (NullPointerException npe) {
            if (stockHandler.getEntry().isRoot())
                log.error("Attempted to 'assign' data sets to to root stock, which is not allowed.", npe);
            else
                log.error(standardCatchLogMessage, npe);

        } catch (Throwable throwable) {
            log.error(standardCatchLogMessage, throwable);

        }
    }

    public List<DataSetsTypes> getDataSets() {
        dataSets = Arrays.asList(DataSetsTypes.values());
        return dataSets;
    }

    public void setDataSets(List<DataSetsTypes> dataSets) {
        this.dataSets = dataSets;
    }

    /**
     * Checks if batch operation is needed for large datasets
     */
    public void batchNeeded(DataSetSelectableDataModel<DataSet> ds, StockChildrenWrapper<DataSet> dsw) {
        if (dsw != null)
            depOptions = dsw.getDependenciesOptions();
        if (ds != null && ds.getSelected() != null)
            this.datasetsSelectedCount = ds.getSelected().length;
    }

    /**
     * Batching big datasets and create a job in the job list, only attach and detach are supported
     */
    public void batch(DataSetSelectableDataModel<DataSet> ds, String action) {
        JobType jobType = JobType.ATTACH;
        if (action.contains("detach"))
            jobType = JobType.DETACH;

        String standardCatchLogMessage = "Error when trying to " + jobType.name() + " data sets to data stock "
                + stockHandler.getEntry().getUuidAsString();
        try {
            AbstractDataStock targetStock = stockHandler.getEntry();
            if (!targetStock.isRoot()) {
                AssignUnassignTask task;
                if (JobType.ATTACH.equals(jobType))
                    task = AssignTask.fromData(new HashSet<>(Arrays.asList(ds.getSelected())), (DataStock) targetStock,
                            getDepOption(), this.getCurrentUserOrNull());
                else
                    task = UnassignTask.fromData(new HashSet<>(Arrays.asList(ds.getSelected())), (DataStock) targetStock,
                            getDepOption(), this.getCurrentUserOrNull());
                task.runIn(this.getGlobalQueue()).message(this, true);
            }

        } catch (SchedulerException | PersistException | MergeException | JobNotScheduledException
                 | StockBusyException | UserNotExistingException e) {
            this.addI18NFacesMessage("facesMsg.stock.isBusy", FacesMessage.SEVERITY_ERROR);
            log.error(standardCatchLogMessage, e);

        } catch (NullPointerException npe) {
            if (stockHandler.getEntry().isRoot())
                log.error("Attempted to 'assign' data sets to to root stock, which is not allowed.", npe);
            else
                log.error(standardCatchLogMessage, npe);

        } catch (Throwable throwable) {
            log.error(standardCatchLogMessage, throwable);

        }
    }

    public ArrayList<DataSetsTypes> getselectedDataSetTypes() {
        switch (this.selectMode) {
            case "1":
                selectedDataSetTypes.addAll(Arrays.asList(DataSetsTypes.values()));
                break;

            case "2":
                selectedDataSetTypes.clear();
                break;

            case "3":
                selectedDataSetTypes.clear();
                selectedDataSetTypes.addAll(Arrays.asList(DataSetsTypes.values()));
                selectedDataSetTypes.remove(DataSetsTypes.PROCESSES);
                selectedDataSetTypes.remove(DataSetsTypes.LCIA_METHODS);
                break;

            default:
                break;
        }
        return selectedDataSetTypes;
    }

    public void selectionByListener() {
        if (selectionBy == BatchAssignMode.DATA_SET_TYPE) {
            this.unselectAllStocks();
            fetchFromAllStocks = false;
        } else {
            this.selectAllVisibleStocks();
            fetchFromAllStocks = true;
        }
    }

    private void selectAllVisibleStocks() {
        this.stockListHandler.setSelectedStocks(
                this.stockListHandler.getViewCache().toArray(new AbstractDataStock[0]));
    }

    private void unselectAllStocks() {
        this.stockListHandler.clearSelection();
    }

    public void fetchFromAllStocksListener() {
        if (fetchFromAllStocks) {
            this.selectAllVisibleStocks();
        } else {
            this.unselectAllStocks();
        }
    }

    public void setselectedDataSetTypes(ArrayList<DataSetsTypes> selectedDataSetTypes) {
        this.selectedDataSetTypes = selectedDataSetTypes;
    }

    public String getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(String selectMode) {
        this.selectMode = selectMode;
    }

    public StockHandler getStockHandler() {
        return stockHandler;
    }

    public void setStockHandler(StockHandler stockHandler) {
        this.stockHandler = stockHandler;
    }

    public boolean isShowJobsButton() {
        return showJobsButton;
    }

    public void setShowJobsButton(boolean showJobsButton) {
        this.showJobsButton = showJobsButton;
    }

    public boolean isBatchNeeded() {
        if (isForced)
            return this.batchNeeded;
        return datasetsSelectedCount >= 500;
    }

    public void setBatchNeeded(boolean batchNeeded) {
        isForced = true;
        this.batchNeeded = batchNeeded;
    }

    public void resetForce() {
        isForced = false;
    }

    public String getSelectionBy() {
        return selectionBy.getValue();
    }

    public void setSelectionBy(String selectionBy) {
        this.selectionBy = BatchAssignMode.fromValue(selectionBy);
    }

    public ExcelSelectionHandler getExcelSelectionHandler() {
        return excelSelectionHandler;
    }

    public void setExcelSelectionHandler(ExcelSelectionHandler excelSelectionHandler) {
        this.excelSelectionHandler = excelSelectionHandler;
    }

    public Boolean getIsFileKnown() {
        return isFileKnown;
    }

    public Boolean getFetchFromAllStocks() {
        return fetchFromAllStocks;
    }

    public void setFetchFromAllStocks(Boolean fetchFromAllStocks) {
        this.fetchFromAllStocks = fetchFromAllStocks;
    }

    public Boolean selectionIsByExcel() {
        return (selectionBy == BatchAssignMode.EXCEL_IMPORT);
    }

    public Boolean selectionIsByType() {
        return (selectionBy == BatchAssignMode.DATA_SET_TYPE);
    }
}
