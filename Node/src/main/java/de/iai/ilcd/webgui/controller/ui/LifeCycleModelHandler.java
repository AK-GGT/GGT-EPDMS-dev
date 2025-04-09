package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.LifeCycleModelDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ILifeCycleModelVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.dao.FlowDao;
import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.lifecyclemodel.*;
import de.iai.ilcd.model.registry.CommonRegistrationData;
import de.iai.ilcd.rest.DataSetRegistrationService;
import de.iai.ilcd.service.glad.GLADRegistrationService;
import de.iai.ilcd.webgui.controller.url.AbstractDataSetURLGenerator;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;

@ManagedBean
@ViewScoped
public class LifeCycleModelHandler extends
        AbstractDataSetHandler<ILifeCycleModelVO, LifeCycleModel, LifeCycleModelDao, LifeCycleModelDataSetType> {

    private static final long serialVersionUID = 1546417646852045980L;
    private static final String BLANK_SYMBOL = "-";
    /**
     * Character used to split between different paramters when combining them
     * in a single string to be send to the view
     */
    private static final String PARAMS_SPLIT = ",";
    private final DataSetRegistrationService dataSetRegistrationService;
    private final GLADRegistrationService gladRegistrationService;
    private LCMTreeArborist selectedNode;

    /**
     * only used for expand and collapse functionality.
     * <p>
     * other workaround has been implemented to avoid regenerating the tree
     * structure.
     */
    private TreeNode root;

    public LifeCycleModelHandler() {
        super(new LifeCycleModelDao(), DatasetTypes.LIFECYCLEMODELS.getValue(), ILCDTypes.LIFECYCLEMODEL);
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.gladRegistrationService = ctx.getBean(GLADRegistrationService.class);
        this.dataSetRegistrationService = ctx.getBean(DataSetRegistrationService.class);
    }

    private static void expandOrCollapse(TreeNode treeNode, boolean option) {

        if (treeNode.getChildCount() == 0)
            treeNode.setSelected(false);
        else {
            for (TreeNode t : treeNode.getChildren())
                expandOrCollapse(t, option);

            treeNode.setExpanded(option);
            treeNode.setSelected(false);
        }
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public LifeCycleModel getLcm() {
        return (LifeCycleModel) this.getDataSet();
    }

    public String getBaseName(String lang) {
        LifeCycleModel lcm = this.getLcm();
        try {
            return lcm.getBaseName().getValueWithFallback(lang);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getFormat(String lang) {
        LifeCycleModel lcm = this.getLcm();
        try {
            return lcm.getAdministrativeInformation().getDataEntryBy().getReferenceToDataSetFormat().get(0)
                    .getShortDescription().getValueWithFallback(lang);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public String getUseAdvice(String lang) {
        LifeCycleModel lcm = this.getLcm();
        return LifeCycleModel.SafeLangMapValue(lcm.getUseAdviceForDataSet(), lang);
    }

    public Date getTimeStamp() {
        LifeCycleModel lcm = this.getLcm();
        try {
            return lcm.getAdministrativeInformation().getDataEntryBy().getTimeStamp();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getGeneralComment(String lang) {
        LifeCycleModel lcm = this.getLcm();
        return LifeCycleModel.SafeLangMapValue(lcm.getGeneralComment(), lang);
    }

    public String getAccessInfo(String lang) {
        LifeCycleModel lcm = this.getLcm();
        return LifeCycleModel.SafeLangMapValue(
                lcm.getAdministrativeInformation().getPublicationAndOwnership().getUseRestrictions(), lang);
    }

    public String getProject(String lang) {
        LifeCycleModel lcm = this.getLcm();
        return LifeCycleModel.SafeLangMapValue(lcm.getAdministrativeInformation().getCommissionerAndGoal().getProject(),
                lang);
    }

    public String getIntendedApplications(String lang) {
        LifeCycleModel lcm = this.getLcm();
        return LifeCycleModel.SafeLangMapValue(
                lcm.getAdministrativeInformation().getCommissionerAndGoal().getIntendedApplications(), lang);
    }

    public String getGroupDetails(TechnologyGroup g, String lang) {
        return LifeCycleModel.SafeLangMapValue(g.getGroupdetails(), lang);
    }

    // If it possible to get the lang from UI (to generate the corresponding
    // description)
    // in a cleaner way, feel free to store the lang as an attribute that you
    // populate at @PostConstruct
    // TODO: maybe f:setPropertyActionListener?
    public TreeNode generateRootNode(String lang) {
        if (this.root != null)
            return this.root;

        LifeCycleModel lcm = this.getLcm();

        TreeNode root = new DefaultTreeNode(
                new LCMTreeArborist("root", "flowUUID", "ID", "Location", "Parameters", null), null);

        for (ProcessInstance processInstance : lcm.getProcesses()) {
            String params = "";

            for (String k : processInstance.getParameters().keySet())
                params += String.format("<b>%s</b>:\t <u>%s</u>%s", k,
                        processInstance.getParameters().getOrDefault(k, 0.0), PARAMS_SPLIT);

            TreeNode p = new DefaultTreeNode("ProcessInstance",
                    new LCMTreeArborist(
                            processInstance.getReferenceToProcess().getShortDescription().getValueWithFallback(lang),
                            BLANK_SYMBOL, BLANK_SYMBOL, BLANK_SYMBOL, params, processInstance),
                    root);
            for (OutputExchange outputExchange : processInstance.getConnections()) {

                TreeNode o = new DefaultTreeNode("OutputExchange",
                        new LCMTreeArborist("Output Exchange",
                                outputExchange.getFlow(),
                                Long.toString(outputExchange.getInternaloutputexchange_id()), BLANK_SYMBOL, "",
                                outputExchange),
                        p);
                for (DownstreamProcess downstreamProcess : outputExchange.getDownstreamProcesses()) {
                    new DefaultTreeNode("DownstreamProcess",
                            new LCMTreeArborist("Downstream Process",
                                    downstreamProcess.getFlow(), BLANK_SYMBOL,
                                    downstreamProcess.getLocation(), "", downstreamProcess),
                            o);
                }
            }
        }

        this.root = root;
        return root;
    }

    public Flow getFlow(String flowUUID) {
        Flow erg = null;
        for (FlowDao<?> fd : Arrays.asList(new ElementaryFlowDao(), new ProductFlowDao())) {
            erg = fd.getByUuid(flowUUID);
            if (erg != null)
                break;
        }
        return erg;
    }

    public LCMTreeArborist getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(LCMTreeArborist selectedNode) {
        this.selectedNode = selectedNode;
    }

    /**
     * Workaround to get number of parameters in a given processInstance
     * <p>
     * Attempting to do that from the view results in: SEVERE:
     * java.lang.NumberFormatException: For input string: "length"
     *
     * @return wheter a node has ProcessInstance parameters or not
     */
    public boolean isSelectedNodeParams() {
        if (this.selectedNode == null)
            return false;
        return this.selectedNode.getParameters().length() > 0;
    }

    public void expandAll() {
        expandOrCollapse(root, true);
    }

    public void collapseAll() {
        expandOrCollapse(root, false);
    }

    public List<Integer> getMemberOf(Object p) { // for a given processInstance
        if (!(p instanceof ProcessInstance))
            return null;
        return ((ProcessInstance) p).getMemberOf();
    }

    public String getGroupName(Integer groupID, String lang) {
        LifeCycleModel lcm = this.getLcm();
        for (TechnologyGroup t : lcm.getGroups())
            if (t.getGroupid() == groupID)
                return LifeCycleModel.SafeLangMapValue(t.getGroupdetails(), lang);
        return "";
    }

    /**
     * In datasetdetails for LCM, expanding a node in the treetable calls
     * getReferenceUrlXml's globalRef with a null.
     * <p>
     * Cheap trick to avoid ambiguous methods when globalRef is null.
     *
     * @param <E>
     * @param globalRef
     * @param generator
     * @return
     */
    public <E extends DataSet> String getReferenceUrlXml2(GlobalReference globalRef,
                                                          AbstractDataSetURLGenerator<E> generator) {
        if (globalRef == null) {
            return null;
        }

        return this.getReferenceUrl(globalRef, generator);
    }

    public Set<ProcessInstance> uniqSortedProcesses() {
        return new TreeSet<ProcessInstance>(this.getLcm().getProcesses());
    }

    public List<CommonRegistrationData> getRegistrations() {
        List<CommonRegistrationData> registrationData = new ArrayList<CommonRegistrationData>();
        // registrationData.addAll(this.dataSetRegistrationService.getListOfRegistrations((LifeCycleModel)
        // this.getDataSet()));
        // registrationData.addAll(this.gladRegistrationService.getListOfRegistrations((LifeCycleModel)
        // this.getDataSet()));
        return registrationData;
    }

    public LifeCycleModel getSupersedingVersion() {
        return this.getDaoInstance()
                .getSupersedingDataSetVersion(((LifeCycleModel) this.getDataSet()).getUuidAsString());
    }

    /**
     * Returns the most recent version or null if this already is the most
     * recent version.
     */
    public LifeCycleModel getNewerVersion() {
        if (isMostRecentVersion())
            return null;
        LifeCycleModel mostRecentDataSet = (LifeCycleModel) this.getDataSet();
        for (LifeCycleModel po : this.getOtherVersions()) {
            if (mostRecentDataSet.getVersion().compareTo(po.getVersion()) == -1)
                mostRecentDataSet = po;
        }
        return mostRecentDataSet;
    }

    public boolean isMostRecentVersion() {
        LifeCycleModel p = (LifeCycleModel) this.getDataSet();
        for (LifeCycleModel po : this.getOtherVersions()) {
            if ((p.getVersion().compareTo(po.getVersion()) == -1))
                return false;
        }
        return true;
    }

    public List<LifeCycleModel> getOtherVersionsWithoutMostRecentVersion() {
        List<LifeCycleModel> result = new ArrayList<LifeCycleModel>();
        result.addAll(this.getOtherVersions());

        if (!this.isMostRecentVersion())
            result.remove(this.getNewerVersion());
        return result;
    }

}
