package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.registry.DataSetRegistrationData;
import de.iai.ilcd.rest.DataSetRegistrationService;
import de.iai.ilcd.rest.util.InvalidGLADUrlException;
import de.iai.ilcd.service.glad.GLADRegistrationData;
import de.iai.ilcd.service.glad.GLADRegistrationService;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.glad.RegistrationTask;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.util.Consts;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import eu.europa.ec.jrc.lca.commons.view.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Scope("view")
public class DataSetsRegistrationBean extends AbstractHandler implements Serializable {

    private static final long serialVersionUID = -1734997832360544545L;
    private final List<Process> processes;
    private final Set<String> registeredProcesses = new HashSet<>();
    @Autowired
    private DataSetRegistrationService dataSetRegistrationService;
    @Autowired
    private GLADRegistrationService gladRegistrationService;
    private Long registry;

    @SuppressWarnings("unchecked")
    public DataSetsRegistrationBean() {
        super();
        this.processes = (List<Process>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_DATASETS);
    }

    public List<Process> getProcesses() {
        return this.processes;
    }

    public int getCount() {
        return this.processes.size();
    }

    public Long getRegistry() {
        return this.registry;
    }

    public void setRegistry(Long registry) {
        this.registry = registry;
        this.getRegisteredDataSets();
    }

    private void getRegisteredDataSets() {
        this.registeredProcesses.clear();
        if (this.registry != null && registry != -1) {
            Collection<DataSetRegistrationData> registrationData = this.dataSetRegistrationService.getRegistered(this.registry);
            for (DataSetRegistrationData dsrd : registrationData) {
                this.registeredProcesses.add(dsrd.getUuid() + dsrd.getVersion());
            }
        } else if (registry != null) {
            Collection<GLADRegistrationData> registrationData = this.gladRegistrationService.getRegistered();
            for (GLADRegistrationData dsrd : registrationData) {
                this.registeredProcesses.add(dsrd.getUuid() + dsrd.getVersion());
            }
        }
    }

    public boolean isRegistered(Process process) {
        return this.registeredProcesses.contains(process.getUuidAsString() + process.getVersion().getVersionString());
    }

    public void register() {
        if (this.registry == null) {
            FacesMessage message = Messages.getMessage("resources.lang", "registryIsReguired", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        boolean redirectToJobs = false;
        try {
            List<DataSetRegistrationResult> result;
            if (ConfigurationService.INSTANCE.isGladEnabled() && registry == -1) {
                RegistrationTask<Process> registrationTask = new RegistrationTask<>(this.processes, this.getCurrentUserOrNull());
                TaskResult taskResult = registrationTask.runIn(this.getGlobalQueue());
                if (taskResult != null) {
                    result = (List<DataSetRegistrationResult>) taskResult.getList(RegistrationTask.TASK_RESULT_DETAILS_KEY);
                    redirectToJobs = (TaskStatus.SCHEDULED.equals(taskResult.getStatus()));
                } else {
                    result = null;
                }

            } else {
                result = this.dataSetRegistrationService.register(this.processes, this.registry);
            }
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.REGISTRATION_RESULT, result);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.SELECTED_DATASETS, this.processes);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put(Consts.SELECTED_REGISTRY, this.registry);
            this.registry = null;
            this.registeredProcesses.clear();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            if (redirectToJobs) {
                FacesMessage message = Messages.getMessage("resources.lang", "facesMsg.stock.createdNewJob", null);
                message.setSeverity(FacesMessage.SEVERITY_INFO);
                FacesContext.getCurrentInstance().addMessage(null, message);
                FacesUtils.redirectToPage("/admin/datasets/manageProcessList");
            } else
                FacesUtils.redirectToPage("datasetRegistrationSummary.xhtml");
        } catch (RestWSUnknownException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "restWSUnknownException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (AuthenticationException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "authenticationException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (NodeIllegalStatusException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "nodeIllegalStatusException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (InvalidGLADUrlException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "invalidGLADUrlException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

}
