package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.rest.RegistryService;
import de.iai.ilcd.webgui.util.Consts;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component("datasetRegistrationSummaryBean")
@Scope("view")
public class DatasetRegistrationSummaryBean implements Serializable {

    private static final long serialVersionUID = -3144746029685291342L;

    @Autowired
    private RegistryService registryService;

    private Registry registry;

    private int accepted;

    private int rejected;

    private int errorCount;

    private List<Process> rejectedProcesses;

    private List<Process> rejectedCompliance;

    private List<Process> rejectedNoDifference;

    private List<Process> errorList;

    private boolean isGLAD = false;

    @PostConstruct
    private void init() {
        List<DataSetRegistrationResult> result = (List<DataSetRegistrationResult>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(
                Consts.REGISTRATION_RESULT);

        List<Process> processes = (List<Process>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_DATASETS);

        Long registryId = (Long) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_REGISTRY);

        if (registryId != -1)
            registry = registryService.findById(registryId);
        else
            isGLAD = true;
        displaySummaryMessage(processes, result);
    }

    private void displaySummaryMessage(List<Process> datasets, List<DataSetRegistrationResult> result) {

        rejectedCompliance = groupBy(DataSetRegistrationResult.REJECTED_COMPLIANCE, result, datasets);

        rejectedNoDifference = groupBy(DataSetRegistrationResult.REJECTED_NO_DIFFERENCE, result, datasets);

        errorList = groupBy(DataSetRegistrationResult.ERROR, result, datasets);

        if (!isGLAD) {
            rejected = rejectedCompliance.size() + rejectedNoDifference.size();
            errorCount = errorList.size();
            accepted = result.size() - rejected - errorCount;
        } else {
            rejected = rejectedCompliance.size() + rejectedNoDifference.size() + errorList.size();
            accepted = result.size() - rejected;
        }

        rejectedProcesses = new ArrayList<>(rejectedNoDifference);
        rejectedProcesses.addAll(rejectedCompliance);
        rejectedProcesses.addAll(errorList);
    }

    private List<Process> groupBy(DataSetRegistrationResult regResult, List<DataSetRegistrationResult> result, List<Process> datasets) {
        List<Process> grouped = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) == regResult) {
                grouped.add(datasets.get(i));
            }
        }
        return grouped;
    }

    public Registry getRegistry() {
        return registry;
    }

    public int getAccepted() {
        return accepted;
    }

    public int getRejected() {
        return rejected;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<Process> getRejectedProcesses() {
        return rejectedProcesses;
    }

    public List<Process> getRejectedCompliance() {
        return rejectedCompliance;
    }

    public List<Process> getRejectedNoDifference() {
        return rejectedNoDifference;
    }

    public List<Process> getErrorList() {
        return errorList;
    }

    public boolean isRejectedCompliance(Process process) {
        return rejectedCompliance.contains(process);
    }

    public boolean isRejectedNoDifference(Process process) {
        return rejectedNoDifference.contains(process);
    }

    public boolean isError(Process process) {
        return errorList.contains(process);
    }

    public boolean isGlad() {
        return isGLAD;
    }
}
