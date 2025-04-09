package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.app.process.Exchanges;
import de.fzk.iai.ilcd.api.binding.generated.common.ClassType;
import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.api.binding.generated.process.ProcessDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.api.extension.ef.originatingprocess.OriginatingProcess;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IProcessVO;
import de.fzk.iai.ilcd.service.model.enums.CompletenessValue;
import de.fzk.iai.ilcd.service.model.enums.LCIMethodApproachesValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.delegate.DataSetRestServiceBD;
import de.iai.ilcd.delegate.ValidationResult;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.process.Exchange;
import de.iai.ilcd.model.process.LciaResult;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.contentdeclaration.ContentDeclaration;
import de.iai.ilcd.model.registry.CommonRegistrationData;
import de.iai.ilcd.model.registry.DataSetRegistrationData;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.rest.DataSetDeregistrationService;
import de.iai.ilcd.rest.DataSetRegistrationService;
import de.iai.ilcd.rest.RegistryService;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.service.glad.GLADRegistrationService;
import de.iai.ilcd.util.CategoryTranslator;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.sort.LCIAIndicatorCustomComparator;
import de.iai.ilcd.util.sort.LCIAIndicatorNaturalComparator;
import de.iai.ilcd.util.sort.LCIIndicatorCustomComparator;
import de.iai.ilcd.util.sort.LCIIndicatorNaturalComparator;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.xml.zip.ZipArchiveBuilder;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.commons.view.util.Messages;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.TreeNode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Backing bean for process detail view
 */
@ManagedBean
@ViewScoped
public class ProcessHandler extends AbstractDataSetHandler<IProcessVO, Process, ProcessDao, ProcessDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6907352892423743765L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(ProcessHandler.class);
    private static final String EN15804A2Reference = "c0016b33-8cf7-415c-ac6e-deba0d21440d";
    private final DataSetRegistrationService dataSetRegistrationService;
    private final GLADRegistrationService gladRegistrationService;
    private final DataSetDeregistrationService dataSetDeregistrationService;
    private final RegistryService registryService;
    /**
     * List with input product flows
     */
    private List<Flow> inputProducts;
    /**
     * List with co product flows
     */
    private List<Flow> coProducts;
    private String reason;
    private DataSetRegistrationData selectedDataSetRegistrationData;
    /**
     * Flag to indicate if EPD type
     */
    private boolean epd;
    private Double referenceProductFlowPropMeanAmount = null;
    @Inject
    private DataExportController dataExportController;
    @ManagedProperty(value = "#{contentDeclarationHandler}")
    private ContentDeclarationHandler cdHandler;
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocksHandler;
    /**
     * We store here the Uuids of additional environmental impact indicators.
     * Unlike core indicators, the additional ones need to be shown with a disclaimer.
     * For more information check the official norm EN15804+A2.
     */
    private final String[] additionalType1EIIndicatorsUuids = new String[]{"b5c632be-def3-11e6-bf01-fe55135034f3"};
    private final String[] additionalType2EIIndicatorsUuids = new String[]{"b5c602c6-def3-11e6-bf01-fe55135034f3", "ee1082d1-b0f7-43ca-a1f0-21e2a4a74511",
            "2299222a-bbd8-474f-9d4f-4dd1f18aea7c", "3af763a5-b7a1-48c9-9cee-1f223481fcef", "b2ad6890-c78d-11e6-9d9d-cec0c932ce01"};
    private LCIIndicatorCustomComparator lciComp = null;
    private LCIAIndicatorCustomComparator lciaComp = null;
    private LCIIndicatorNaturalComparator lciNatComp = null;
    private LCIAIndicatorNaturalComparator lciaNatComp = null;

    /**
     * Initialize the handler
     */
    public ProcessHandler() {
        super(new ProcessDao(), DatasetTypes.PROCESSES.getValue(), ILCDTypes.PROCESS);
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.gladRegistrationService = ctx.getBean(GLADRegistrationService.class);
        this.dataSetRegistrationService = ctx.getBean(DataSetRegistrationService.class);
        this.dataSetDeregistrationService = ctx.getBean(DataSetDeregistrationService.class);
        this.registryService = ctx.getBean(RegistryService.class);
    }

    /**
     * Enables Autowiring inside a JSF managed bean.
     * <p>
     * Remove it when everything is CDI managed.
     *
     * @see <a href="https://stackoverflow.com/a/18388289/260229"></a>
     */

    @PostConstruct
    private void init() {
        FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
                .getAutowireCapableBeanFactory().autowireBean(this);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return process
     */
    public IProcessVO getProcess() {
        return this.getDataSet();
    }

    /**
     * We need this as workaround because the Service API delivers a set but we
     * need a List in the template
     *
     * @return the list of approaches
     */
    public List<LCIMethodApproachesValue> getApproaches() {
        List<LCIMethodApproachesValue> approaches = new ArrayList<>();

        if (this.getDataSet() != null) {
            approaches = new ArrayList<>(
                    this.getDataSet().getLCIMethodInformation().getApproaches());
        }

        return approaches;
    }

    /**
     * We need this too as workaround to geth the ComplianceSystems as List
     *
     * @return
     */
    public List<IComplianceSystem> getComplianceSystems() {
        List<IComplianceSystem> complianceSystems = new ArrayList<>();

        if (this.getDataSet() != null) {
            complianceSystems = new ArrayList<>(this.getDataSet().getComplianceSystems());
        }

        return complianceSystems;
    }

    /**
     * Get the input product flows
     *
     * @return input product flows
     */
    public List<Flow> getInputProducts() {
        return this.inputProducts;
    }

    /**
     * Get the co-product flows
     *
     * @return co-product flows
     */
    public List<Flow> getCoProducts() {
        return this.coProducts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void datasetLoaded(Process p) {
        // Load input products
        List<Exchange> tmp = p.getReferenceExchanges(ExchangeDirectionValues.INPUT);
        this.inputProducts = new ArrayList<>();
        for (Exchange e : tmp) {
            if (e.getFlowWithSoftReference() != null && TypeOfFlowValue.PRODUCT_FLOW.equals(e.getFlowWithSoftReference().getType())) {
                this.inputProducts.add(e.getFlowWithSoftReference());
            }
        }

        // Load co-products
        tmp = p.getReferenceExchanges(ExchangeDirectionValues.OUTPUT);
        this.coProducts = new ArrayList<>();
        // named loop (due to continue from inner loop)
        coProdLoop:
        for (Exchange e : tmp) {
            if (e.getFlowWithSoftReference() != null && TypeOfFlowValue.PRODUCT_FLOW.equals(e.getFlowWithSoftReference().getType())) {
                // check if this exchange flow is contained in the reference
                // exchange list
                Long id = e.getFlowWithSoftReference().getId();
                for (Exchange refEx : p.getReferenceExchanges()) {
                    if (refEx.getFlowWithSoftReference() != null && id.equals(refEx.getFlowWithSoftReference().getId())) {
                        // trigger next loop from !! outer !! loop
                        continue coProdLoop;
                    }
                }
                // flow was not in reference exchange list
                this.coProducts.add(e.getFlowWithSoftReference());
            }
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(this.coProducts.size() + " co-products found");

        // set EPD flag
        this.epd = TypeOfProcessValue.EPD.equals(p.getType());

        try {
            this.setReferenceProductFlowPropMeanAmount(p.getReferenceExchanges().get(0).getFlowWithSoftReference().getReferencePropertyDescription().getMeanValue());
        } catch (NullPointerException | IndexOutOfBoundsException e2) {
            if (LOGGER.isDebugEnabled())
                LOGGER.error("no mean amount for reference product found");
        }

        // set content declaration
        ContentDeclaration cd = ContentDeclaration.read(this.getXmlDataset().getProcessInformation().getDataSetInformation().getOther());
        ((Process) this.getProcess()).setContentDeclaration(cd);
        this.cdHandler.setContentDeclaration(cd);
        ContentDeclaration cd2 = ContentDeclaration.read(this.getXmlDataset().getProcessInformation().getDataSetInformation().getOther());
        ((Process) this.getProcess()).setContentDeclaration(cd2);
        this.cdHandler.setContentDeclaration(cd2);
    }

    public String deregisterSelected() {
        try {
            if (SecurityUtil.hasAdminAreaAccessRight()) {
                this.dataSetDeregistrationService.deregisterDatasets(
                        Collections.singletonList(this.selectedDataSetRegistrationData), this.reason,
                        this.selectedDataSetRegistrationData.getRegistry());
            }
        } catch (RestWSUnknownException e) {
            FacesMessage message = Messages.getMessage("resources.lang",
                    "admin.deregisterDataSets.restWSUnknownException", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (AuthenticationException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "authenticationException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        return null;
    }

    public List<CommonRegistrationData> getRegistrations() {
        List<CommonRegistrationData> registrationData = new ArrayList<CommonRegistrationData>();
        registrationData.addAll(this.dataSetRegistrationService.getListOfRegistrations((Process) this.getDataSet()));
        registrationData.addAll(this.gladRegistrationService.getListOfRegistrations((Process) this.getDataSet()));
        return registrationData;
    }

    public DataSetRegistrationData getSelectedDataSetRegistrationData() {
        return this.selectedDataSetRegistrationData;
    }

    public void setSelectedDataSetRegistrationData(DataSetRegistrationData selectedDataSetRegistrationData) {
        this.selectedDataSetRegistrationData = selectedDataSetRegistrationData;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ValidationResult getDatasetValidationResult() {
        if (this.getRegistryUUID() != null && !this.getRegistryUUID().trim().isEmpty()) {
            Registry reg = this.registryService.findByUUID(this.getRegistryUUID());
            DataSet ds = this.getDataSetFromProcess();
            return DataSetRestServiceBD.getInstance(reg).verify(ds);
        } else {
            return ValidationResult.CANT_VALIDATE_NOT_REGISTERED;
        }
    }

	/*public List<Process> getPrecedingVersions() {
		Set<GlobalReference> ref = ((Process) getDataSet()).getPrecedingDataSetVersions();
		List<Process> lp = new ArrayList<Process>();
		for (GlobalReference g : ref) {
			Process p = this.getDaoInstance().getByUuidAndVersion(g.getUuid().getUuid(), g.getVersion());
			if (p != null)
				lp.add(p);
		}
		if (!lp.isEmpty())
			return lp;
		return null;
	}*/

    private DataSet getDataSetFromProcess() {
        Process p = (Process) this.getDataSet();
        DataSet ds = new DataSet();
        ds.setUuid(p.getUuid().getUuid());
        ds.setVersion(p.getVersion().getVersionString());
        ds.setHash(p.getXmlFile().getContentHash());
        return ds;
    }

    /**
     * Determines if process is of type EPD
     *
     * @return <code>true</code> if process is of type EPD, <code>false</code>
     * otherwise
     */
    public boolean isEPD() {
        return this.epd;
    }

    /**
     * Determines if data set is the most recent version
     *
     * @return <code>true</code> if data set is the most recent version, else <code>false</code>
     */
    public boolean isMostRecentVersion() {
        Process p = (Process) this.getDataSet();
        for (Process po : this.getOtherVersions()) {
            if ((p.getVersion().compareTo(po.getVersion()) == -1))
                return false;
        }
        return true;
    }

    /**
     * Returns the most recent version or null if this already is the most recent version.
     */
    public Process getNewerVersion() {
        if (isMostRecentVersion())
            return null;
        Process mostRecentDataSet = (Process) this.getDataSet();
        for (Process po : this.getOtherVersions()) {
            if (mostRecentDataSet.getVersion().compareTo(po.getVersion()) == -1)
                mostRecentDataSet = po;
        }
        return mostRecentDataSet;
    }

    public Process getSupersedingVersion() {
        if (this.getDataSet() == null)
            return null;
        return this.getDaoInstance().getSupersedingDataSetVersion(((Process) this.getDataSet()).getUuidAsString(), this.availableStocksHandler.getVisibleStocksMeta());
    }

    /**
     * @see SodaUtil#replace(String, String, String)
     */
    public String replace(String text, String searchString, String replacement) {
        return SodaUtil.replace(text, searchString, replacement);
    }

    /**
     * @see SodaUtil#groupHint(String, java.util.ResourceBundle)
     */
    public String groupHint(String group) {
        return SodaUtil.groupHint(group, this.getI18n());
    }

    /**
     * Helper method returns a String containing slash separated inline list of
     * class types as a workaround
     *
     * @param clazz class type as list
     * @return String containing slash separated inline list of class types
     */
    public String getClassTypeListInline(List<ClassType> clazz, String catSystem, String language) {
        boolean translate = (ConfigurationService.INSTANCE.isTranslateClassification()
                && !StringUtils.equalsIgnoreCase(language, ConfigurationService.INSTANCE.getDefaultLanguage()));

        CategoryTranslator t = null;

        if (translate)
            try {
                t = new CategoryTranslator(DataSetType.PROCESS, catSystem);
            } catch (Exception e) {
                LOGGER.warn("Could not instantiate CategoryTranslator", e);
                translate = false;
            }

        if (CollectionUtils.isNotEmpty(clazz)) {
            StringBuilder classListSb = new StringBuilder();
            for (Iterator<ClassType> classIterator = clazz.iterator(); classIterator.hasNext(); ) {
                ClassType classType = classIterator.next();
                if (!translate)
                    classListSb.append(classType.getValue());
                else
                    classListSb.append(t.translateTo(classType.getClassId(), language));

                if (classIterator.hasNext()) {
                    classListSb.append(" / ");
                }
            }
            return classListSb.toString();
        }
        return null;
    }

    // this controls which sections of the accordion panel are active (open) by default
    public String returnActiveIndexString() {
        String configuredActiveSections = ConfigurationService.INSTANCE.getDisplayConfig().getShowProcessesDetailActiveSections();
        if (StringUtils.isNotBlank(configuredActiveSections))
            return configuredActiveSections;
        else if (this.isEPD()) {
            return "0,3";
        } else {
            return "0,1,2,3,4";
        }
    }

    public int sortExchanges(Object o1, Object o2) {
        if (ConfigurationService.INSTANCE.getDisplayConfig().getSortExchanges().equals("custom")) {
            return getLciComp().compare((Exchange) o1, (Exchange) o2);
        } else if (ConfigurationService.INSTANCE.getDisplayConfig().getSortExchanges().equals("natural")) {
            return getLciNatComp().compare((Exchange) o1, (Exchange) o2);
        } else {
            return 0;
        }
    }

    public int sortLciaResults(Object o1, Object o2) {
        if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("custom")) {
            return getLciaComp().compare((LciaResult) o1, (LciaResult) o2);
        } else if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("natural")) {
            return getLciaNatComp().compare((LciaResult) o1, (LciaResult) o2);
        } else {
            return 0;
        }
    }

    public LCIIndicatorCustomComparator getLciComp() {
        if (lciComp == null)
            lciComp = new LCIIndicatorCustomComparator();
        return lciComp;
    }

    public LCIAIndicatorCustomComparator getLciaComp() {
        if (lciaComp == null)
            lciaComp = new LCIAIndicatorCustomComparator();
        return lciaComp;
    }

    public LCIIndicatorNaturalComparator getLciNatComp() {
        if (lciNatComp == null)
            lciNatComp = new LCIIndicatorNaturalComparator();
        return lciNatComp;
    }

    public LCIAIndicatorNaturalComparator getLciaNatComp() {
        if (lciaNatComp == null)
            lciaNatComp = new LCIAIndicatorNaturalComparator();
        return lciaNatComp;
    }

    public Double getReferenceProductFlowPropMeanAmount() {
        return referenceProductFlowPropMeanAmount;
    }

    public void setReferenceProductFlowPropMeanAmount(Double referenceProductFlowPropMeanAmount) {
        this.referenceProductFlowPropMeanAmount = referenceProductFlowPropMeanAmount;
    }

    public String getLCIAResultUnit(GlobalReference g, String lang) {
        try {
            LCIAMethodDao dao = new LCIAMethodDao();
            LCIAMethod m = dao.getByUuid(g.getRefObjectId());
            if (m == null)
                return null;
            else
                return m.getReferenceQuantity().getShortDescription().getValueWithFallback(lang);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    // EF extension originatingProcess
    public OriginatingProcess getOriginatingProcess(BigInteger exchangeId) {
        LOGGER.trace("enter getOriginatingProcess");
        try {
            de.fzk.iai.ilcd.api.app.process.Exchange e = (de.fzk.iai.ilcd.api.app.process.Exchange) ((Exchanges) this.getXmlDataset().getExchanges()).getExchangeById(exchangeId);
            OriginatingProcess op = e.getOriginatingProcess();
            if (LOGGER.isTraceEnabled() && op != null && op.getRefObjectId() != null)
                LOGGER.trace("returning " + op.getRefObjectId());
            return e.getOriginatingProcess();
        } catch (NullPointerException e) {
            LOGGER.trace("something was null, returning null");
            return null;
        }
    }


    @Deprecated //in favor of download through a REST endpoint
    public void downloadDependencies(Process p) {

        Path dir = Paths.get(ConfigurationService.INSTANCE.getZipFileDirectory());

        // Overwrite the same file to avoid post cleaning
        ZipArchiveBuilder zipArchiveBuilder = new ZipArchiveBuilder(dir.resolve("ProcessHandlerTMPdep.zip"), dir);

        dataExportController.writeDependencies(p, zipArchiveBuilder);
        zipArchiveBuilder.close();

        String filename = p.getUuidAsString() + "_dependencies.zip";

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseHeader("Content-Type", "application/zip");

        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + '\"');
        try {
            externalContext.getResponseOutputStream()
                    .write(Files.readAllBytes(zipArchiveBuilder.getZipArchiveLocation()));
        } catch (IOException e) {
            LOGGER.error("Failed to download the dependencies of the given process");
        }
        facesContext.responseComplete();

        LOGGER.info("The given process and it's dependencies been exported successfully.");
    }

    public List<LciaResult> getCoreLciaResults() {
        Process thisProcess = (Process) this.getDataSet();
        List<LciaResult> results = new ArrayList<>(thisProcess.getLciaResults());

        if (this.isSeparationOfEIIndicators()) {
            List<LciaResult> standardResults = new ArrayList<>();
            for (LciaResult lciaResult : results) {
                if (!isAdditionalResult(lciaResult)) {
                    standardResults.add(lciaResult);
                }
            }
            results = standardResults;
        }

        if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("custom")) {
            results.sort(new LCIAIndicatorCustomComparator());
        } else if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("natural")) {
            results.sort(new LCIAIndicatorNaturalComparator());
        }

        return results;
    }

    public List<LciaResult> getAdditionalLciaResults() {
        Process thisProcess = (Process) this.getDataSet();
        List<LciaResult> results = new ArrayList<>(thisProcess.getLciaResults());
        List<LciaResult> additionalResults = new ArrayList<>();

        for (LciaResult lciaResult : results) {
            if (isAdditionalResult(lciaResult)) {
                additionalResults.add(lciaResult);
            }
        }

        if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("custom")) {
            additionalResults.sort(new LCIAIndicatorCustomComparator());
        } else if (ConfigurationService.INSTANCE.getDisplayConfig().getSortIndicators().equals("natural")) {
            additionalResults.sort(new LCIAIndicatorNaturalComparator());
        }

        return additionalResults;
    }

    public int getDisclaimerType(LciaResult lciaResult) {
        String id = lciaResult.getMethodReference().getRefObjectId();

        for (String type1Uuid : this.getType1ImpactIndicatorsUuids()) {
            if (id.equalsIgnoreCase(type1Uuid))
                return 1;
        }
        for (String type2Uuid : this.getType2ImpactIndicatorsUuids()) {
            if (id.equalsIgnoreCase(type2Uuid))
                return 2;
        }
        return 0;
    }

    private Boolean isAdditionalResult(LciaResult lciaResult) {
        String referencedUuid = lciaResult.getMethodReference().getRefObjectId();
        boolean additionalsFlag = false;

        for (String additionalsUuid : this.getAdditionalEIIndicatorUuids()) {
            if (referencedUuid.equalsIgnoreCase(additionalsUuid)) {
                additionalsFlag = true;
                break;
            }
        }
        return additionalsFlag;
    }

    private List<String> getType1ImpactIndicatorsUuids() {
        return Arrays.asList(this.additionalType1EIIndicatorsUuids);
    }

    private List<String> getType2ImpactIndicatorsUuids() {
        return Arrays.asList(this.additionalType2EIIndicatorsUuids);
    }

    private List<String> getAdditionalEIIndicatorUuids() {
        List<String> result = new ArrayList<>();
        result.addAll(this.getType1ImpactIndicatorsUuids());
        result.addAll(this.getType2ImpactIndicatorsUuids());
        return result;
    }

    public List<Process> getOtherVersionsWithoutMostRecentVersion() {
        List<Process> result = new ArrayList<>();
        result.addAll(this.getOtherVersions());

        if (!this.isMostRecentVersion())
            result.remove(this.getNewerVersion());
        return result;
    }

    public TreeNode getContentDeclarationRoot(String lang) {
        return this.cdHandler.getRootNode(lang);
    }

    public ContentDeclarationHandler getCdHandler() {
        return cdHandler;
    }

    public void setCdHandler(ContentDeclarationHandler cdHandler) {
        this.cdHandler = cdHandler;
    }

    public AvailableStockHandler getAvailableStocksHandler() {
        return availableStocksHandler;
    }

    public void setAvailableStocksHandler(AvailableStockHandler availableStocksHandler) {
        this.availableStocksHandler = availableStocksHandler;
    }

    public boolean displaysContentDeclaration() {
        if (!this.isEPD())
            return false;
        return this.cdHandler.hasData();
    }

    public void expandAllContentDecl() {
        this.cdHandler.expandAll();
    }

    public void collapseAllContentDecl() {
        this.cdHandler.collapseAll();
    }

    public boolean showCompletenessProductModel() {
        CompletenessValue enumValue = CompletenessValue.NO_STATEMENT;
        try {
            enumValue = this.getProcess().getCompletenessProductModel();
        } catch (NullPointerException npe) {
            // leave it NO_STATEMENT
        }
        return enumValue.equals(CompletenessValue.NO_STATEMENT) && this.isEPD() ? false : true;
    }

    public boolean isSeparationOfEIIndicators() {
        return this.isEN15804A2Compliant();
    }

    public boolean isEN15804A2Compliant() {
        try {
            IProcessVO dataSet = this.getProcess();
            Set<IComplianceSystem> css = dataSet.getComplianceSystems();
            for (IComplianceSystem cs : css) {
                String id = cs.getReference().getRefObjectId();
                if (StringUtils.equalsIgnoreCase(id, EN15804A2Reference))
                    return true;
            }
        } catch (NullPointerException npe) {
            return false;
        }
        return false;
    }
}
