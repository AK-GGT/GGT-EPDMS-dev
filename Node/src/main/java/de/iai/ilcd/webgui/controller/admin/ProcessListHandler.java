package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSetState;
import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.model.utils.DatasourcesFilterUtil;
import de.iai.ilcd.rest.DataSetDeregistrationService;
import de.iai.ilcd.rest.DataSetRegistrationService;
import de.iai.ilcd.rest.RegistryService;
import de.iai.ilcd.service.glad.GLADRegistrationService;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.glad.DeregistrationTask;
import de.iai.ilcd.webgui.util.Consts;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import eu.europa.ec.jrc.lca.commons.view.util.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SortOrder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
@ManagedBean(name = "processListHandler")
@ViewScoped
public class ProcessListHandler extends AbstractDataSetListHandler<Process> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8347365494158300406L;
    private static Logger logger = LogManager.getLogger(ProcessListHandler.class);
    private final DataSetDeregistrationService dataSetDeregistrationService;
    private final DataSetRegistrationService dataSetRegistrationService;
    private final RegistryService registryService;
    private final Map<String, Object> paramsMap = new HashMap<String, Object>();
    @ManagedProperty(value = "#{gladRegistrationService}")
    private GLADRegistrationService gladRegistrationService;
    private String registry;
    private String reason;
    private List<Process> registered;

    public ProcessListHandler() {
        super(Process.class, new ProcessDao());
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.dataSetDeregistrationService = ctx.getBean(DataSetDeregistrationService.class);
        this.dataSetRegistrationService = ctx.getBean(DataSetRegistrationService.class);
        this.registryService = ctx.getBean(RegistryService.class);

    }

    public static String tagListPrinter(Process process) {
        List<Tag> tagList = process.getAssociatedTagsList();
        if (tagList == null || tagList.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder("");
        sb.append(tagList.get(0).getName());
        for (int i = 1; i < tagList.size(); i++)
            sb.append(System.lineSeparator() + tagList.get(i).getName());
        return sb.toString();
    }

    public static String dataSourceListPrinter(Process process) {
        List<IGlobalReference> dataSourceList = new ArrayList<>();
        DatasourcesFilterUtil dfu = new DatasourcesFilterUtil();
        dataSourceList = dfu.filterDataSources(process.getDataSources());

        if (dataSourceList == null || dataSourceList.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder("");
        sb.append(dataSourceList.get(0).getShortDescription().getValue());
        for (int i = 1; i < dataSourceList.size(); i++)
            sb.append(System.lineSeparator() + dataSourceList.get(i).getShortDescription().getValue());
        return sb.toString();
    }

    public GLADRegistrationService getGladRegistrationService() {
        return gladRegistrationService;
    }

    public void setGladRegistrationService(GLADRegistrationService service) {
        this.gladRegistrationService = service;
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public Process[] getSelectedProcesses() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     *                 see #setSelectedItems(Process[])
     * @deprecated use {link #setSelectedItems(Process[])}
     */
    @Deprecated
    public void setSelectedProcesses(Process[] selItems) {
        this.setSelectedItems(selItems);
    }

    public void releaseSelected() {
        this.changeReleaseState(DataSetState.RELEASED, false);
    }

    public void unreleaseSelected() {
        this.changeReleaseState(DataSetState.UNRELEASED, false);
    }

    public void unlockSelected() {
        this.changeReleaseState(DataSetState.UNRELEASED, true);
    }

    private void changeReleaseState(DataSetState state, boolean forceUnlock) {
        final Process[] selItems = this.getSelectedItems();
        if (selItems == null) {
            return;
        }

        for (Process process : selItems) {
            try {
                if (process.getReleaseState().equals(state)) {
                    continue;
                }
                if (process.getReleaseState().equals(DataSetState.LOCKED) && !forceUnlock) {
                    continue; // Locked data sets will only change state they should be explicitly unlocked
                }
                process.setReleaseState(state);
                this.getDao().merge(process);
                this.addI18NFacesMessage("facesMsg.proc.changeStateSuccess", FacesMessage.SEVERITY_INFO, this.getDisplayString(process));
            } catch (MergeException e) {
                this.addI18NFacesMessage("facesMsg.proc.changeStateError", FacesMessage.SEVERITY_ERROR, this.getDisplayString(process));
            }
        }

        this.setSelectedItems(null);

        super.reloadCount();
        // DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
        // .findComponent("tableForm:processTable");
        // int page = dataTable.getPage();
        // if (dataTable != null) {
        // dataTable.loadLazyData();
        // }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDisplayString(Process obj) {
        try {
            return obj.getName().getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public void registerSelected() {

        if (!this.selectionValid()) {
            return;
        }

        FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.SELECTED_DATASETS, Arrays.asList(this.getSelectedItems()));
        FacesUtils.redirectToPage("/admin/datasets/registerDatasets");
    }

    public String getRegistry() {
        return this.registry;
    }

    public void setRegistry(String registry) {
        if (logger.isDebugEnabled())
            logger.debug("setting registry to " + registry);
        this.registry = registry;
    }

    @Override
    public List<Process> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        if (this.registry != null && !this.registry.trim().isEmpty())
            filters.put("registeredIn", registry);
        else if (filters.containsKey("registeredIn"))
            filters.remove("registeredIn");

        return super.lazyLoad(first, pageSize, sortField, sortOrder, filters);
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void deregisterSelected(ActionEvent event) {
        if (!this.selectionValid()) {
            return;
        }

        boolean redirectToJobs = false; // just a redirection flag for later on
        try {
            if (ConfigurationService.INSTANCE.isGladEnabled() && registry.equals("-1")) {
                DeregistrationTask<Process> deregistrationTask = new DeregistrationTask<>(Arrays.asList(this.getSelectedItems()), this.getCurrentUserOrNull());
                TaskResult taskResult = deregistrationTask.runIn(this.getGlobalQueue());

                redirectToJobs = (taskResult != null && TaskStatus.SCHEDULED.equals(taskResult.getStatus()));
            } else
                this.dataSetDeregistrationService.deregisterDatasets(Arrays.asList(this.getSelectedItems()), this.reason, this.registryService.findByUUID(
                        this.registry).getId());
            this.setSelectedItems(null);
            this.reason = null;

            if (redirectToJobs) {
                FacesMessage message = Messages.getMessage("resources.lang", "facesMsg.stock.createdNewJob", null);
                message.setSeverity(FacesMessage.SEVERITY_INFO);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }

            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesUtils.redirectToPage("/admin/datasets/manageProcessList");
        } catch (RestWSUnknownException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "admin.deregisterDataSets.restWSUnknownException", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (AuthenticationException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "authenticationException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private boolean selectionValid() {
        if (this.getSelectedItems() == null || this.getSelectedItems().length == 0) {
            FacesMessage message = Messages.getMessage("resources.lang", "admin.deregisterDataSets.noDatasetsSelected", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
            return false;
        }
        return true;
    }

    public void validateSelected() {
        if (!this.selectionValid()) {
            return;
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.SELECTED_DATASETS, Arrays.asList(this.getSelectedItems()));
        FacesUtils.redirectToPage("/admin/validateDatasets?stock=" + this.getStockSelection().getCurrentStockName());
    }

    public void qualityCheckSelected() {
        if (!this.selectionValid()) {
            return;
        }

        FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.SELECTED_DATASETS, Arrays.asList(this.getSelectedItems()));

        FacesUtils.redirectToPage("/admin/qualityCheck?stock=" + this.getStockSelection().getCurrentStockName());

    }

    public List<Process> getRegistered() {
        return this.registered;
    }

    public List<String> getLocations() {
        List<String> locations = new ArrayList<String>();

        for (GeographicalArea a : ((ProcessDao) this.getDao()).getUsedLocations())
            locations.add(a.getAreaCode());

        Collections.sort(locations);

        return locations;
    }

    public List<Source> getDatabases() {
        SourceDao dao = new SourceDao();
        return dao.getDatabases();
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        this.setRegistry(null);
        super.clearAllFilters("processTableForm:processTable");
    }

}
