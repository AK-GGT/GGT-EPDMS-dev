package de.iai.ilcd.webgui.controller.admin;

import com.okworx.ilcd.validation.*;
import com.okworx.ilcd.validation.common.DatasetType;
import com.okworx.ilcd.validation.events.IValidationEvent;
import com.okworx.ilcd.validation.reference.DatasetReference;
import com.okworx.ilcd.validation.reference.IDatasetReference;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.ExportTag;
import de.iai.ilcd.model.datastock.ExportType;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.util.SodaResourceBundle;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.admin.export.DataExportController;
import de.iai.ilcd.webgui.controller.ui.StockSelectionHandler;
import de.iai.ilcd.webgui.controller.url.AbstractDataSetURLGenerator;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import de.iai.ilcd.webgui.util.Consts;
import de.iai.ilcd.webgui.util.ValidationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.jsf.FacesContextUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

@ManagedBean(name = "validationHandler")
@ViewScoped
public class ValidationHandler extends AbstractHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7152522840736563200L;

    public static Logger logger = LogManager.getLogger(ValidationHandler.class);
    private final List<DataSet> datasets;
    public SodaResourceBundle bundle = new SodaResourceBundle();
    private List<String> validationOptions;
    private ArchiveValidator archiveValidator;
    private List<AbstractDatasetsValidator> validators;
    private boolean profileDisabled = false;
    private boolean defaultValue = false;
    private boolean noDataToValidate = true;
    private List<String> selectedOptions;
    private boolean success = true;
    private List<IValidationEvent> events;
    private List<String> documentViewHeader = new ArrayList<>();
    private String stockId = null;
    private String stockName = null;
    private String activeIndex = "0";
    private int numberOfDatasets = 0;
    private ValidationContext validationContext = null;
    private Boolean positiveValidationFlag = false;
    @ManagedProperty(value = "#{stockSelection}")
    private StockSelectionHandler stockSelectionHander;

    @ManagedProperty(value = "#{statusProgress}")
    private StatusProgressBarEventListener statusProgress;

    @ManagedProperty(value = "#{profileHandler}")
    private ProfileHandler profileHandler;

    @ManagedProperty(value = "#{importHandler}")
    private ImportHandler importHandler;

    @ManagedProperty("#{url}")
    private URLGeneratorBean urlGenerator;

    @Autowired
    private DataExportController exportController;

    @SuppressWarnings("unchecked")
    public ValidationHandler() {
        this.datasets = (List<DataSet>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_DATASETS);
    }


    /**
     * Enables Autowiring inside a JSF managed bean.
     * <p>
     * Remove it when everything is CDI managed.
     *
     * @see "https://stackoverflow.com/a/18388289/260229"
     */

    @PostConstruct
    public void init() {
        FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance())
                .getAutowireCapableBeanFactory().autowireBean(this);

        statusProgress.setProgress(0);
        validationOptions = new ArrayList<>();
        selectedOptions = new ArrayList<>();
        events = new ArrayList<>();

        validationOptions.add(bundle.getString("admin.validator.aspect.archive.structure"));
        validationOptions.add(bundle.getString("admin.validator.aspect.categories"));
        validationOptions.add(bundle.getString("admin.validator.aspect.links"));
        validationOptions.add(bundle.getString("admin.validator.aspect.orphaned.items"));
        validationOptions.add(bundle.getString("admin.validator.aspect.reference.flows"));
        validationOptions.add(bundle.getString("admin.validator.aspect.xml.schemas"));
        validationOptions.add(bundle.getString("admin.validator.aspect.xslt.stylesheet"));

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        if (logger.isDebugEnabled()) {
            logger.debug("validationContext " + request.getAttribute("validationContext"));
            logger.debug("stockId " + request.getParameter("stockId"));
            logger.debug("stockName " + request.getParameter("stockName"));
            logger.debug("profilehandler null? " + (profileHandler == null));
        }

        this.stockId = request.getParameter("stockId");
        this.stockName = request.getParameter("stockName");

        String vContext = (String) request.getAttribute("validationContext");
        if (vContext == null)
            vContext = ValidationContext.IMPORT.getValue();

        this.validationContext = ValidationContext.fromValue(vContext);

        if (this.validationContext.equals(ValidationContext.DATASETS))
            this.stockName = stockSelectionHander.getCurrentStockName();

        if (this.validationContext.equals(ValidationContext.STOCK) || this.validationContext.equals(ValidationContext.DATASETS)) {
            activeIndex = "0,1";
            this.validationOptions.remove(bundle.getString("admin.validator.aspect.archive.structure"));
        } else
            activeIndex = "";

        // trigger setting of aspect presets
        this.handleProfileSelection(null);
    }

    public void updateListOptions() {

        if (defaultValue) {
            this.clearListSelectedOptions();
            selectedOptions.add(bundle.getString("admin.aspect.default"));
            profileHandler.setSelectedProfile(profileHandler.getDefaultProfile());
            profileDisabled = true;
        } else {
            profileDisabled = false;
        }

    }

    public void clearListSelectedOptions() {
        selectedOptions.clear();
    }

    public List<String> getValidationOptions() {
        return validationOptions;
    }

    public void setValidationOptions(List<String> validationOptions) {
        this.validationOptions = validationOptions;
    }

    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    public void setProfileHandler(ProfileHandler profileHandler) {
        this.profileHandler = profileHandler;
    }

    public List<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<String> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public boolean isProfileDisabled() {
        return profileDisabled;
    }

    public void setProfileDisabled(boolean profileDisabled) {
        this.profileDisabled = profileDisabled;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ImportHandler getImportHandler() {
        return importHandler;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public ArchiveValidator getArchiveValidator() {
        return archiveValidator;
    }

    public void setArchiveValidator(ArchiveValidator archiveValidator) {
        this.archiveValidator = archiveValidator;
    }

    public List<AbstractDatasetsValidator> getValidators() {
        return validators;
    }

    public void setValidators(List<AbstractDatasetsValidator> validators) {
        this.validators = validators;
    }

    public StatusProgressBarEventListener getStatusProgress() {
        return statusProgress;
    }

    public void setStatusProgress(StatusProgressBarEventListener statusProgress) {
        this.statusProgress = statusProgress;
    }

    public String getStockName() {
        if (this.stockName != null && this.stockName.trim().isEmpty())
            return stockName;
        else
            return "Validation";
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getActiveIndex() {
        return activeIndex;

    }

    public void setActiveIndex(String activeIndex) {
        this.activeIndex = activeIndex;
    }

    public boolean isNoDataToValidate(Long stockId) {
        // TODO: fix performance bottleneck
        noDataToValidate = false;
//		CommonDataStockDao cdao = new CommonDataStockDao();
//		if(!cdao.isDataStockToValidate(stockId))
//			noDataToValidate = true;

        return noDataToValidate;
    }

    public List<IValidationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<IValidationEvent> events) {
        this.events = events;
    }

    public boolean isNoDataToValidate() {
        return noDataToValidate;
    }

    public void setNoDataToValidate(boolean noDataToValidate) {
        this.noDataToValidate = noDataToValidate;
    }

    public String getDatastockValidationView() {
        return "/admin/stocks/validateStock.xhtml?includeViewParams=true";
    }

    /**
     * general method for validation
     */
    public void doValidate() {
        clearEvents();
        generateDocumentViewHeader();
        this.success = true;
        statusProgress.setProgress(0);

        try {
            validators = new ArrayList<>();

            if (this.validationContext.equals(ValidationContext.STOCK)) {
                validateStock();
            } else if (this.validationContext.equals(ValidationContext.DATASETS)) {
                validateDatasets();
            } else if (this.validationContext.equals(ValidationContext.IMPORT)) {
                validateImport();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        statusProgress.setSuccess(success);

        if (success) {
            statusProgress.setMessageSummary(bundle.getString("admin.validation.success"));
            statusProgress.setMessageDetail("");
        } else {
            statusProgress.setMessageSummary(bundle.getString("admin.validation.failure"));
            statusProgress.setMessageDetail(this.events.size() + " " + bundle.getString("admin.validation.events"));
        }
    }

    private void validateImport() throws InterruptedException {
        logger.debug("import validation");
        List<File> files = new ArrayList<>();
        for (UploadedFileInformation fileInf : this.importHandler.getUploadedFiles()) {
            files.add(fileInf.getFile());
        }

        for (File file : files) {
            logger.info("File name : " + file.getName());
            if (file.isAbsolute())
                validateArchive(file);
        }

        validateFiles(files);
    }

    private void validateDatasets() throws InterruptedException {
        logger.debug("datasets validation");

        if (this.datasets == null)
            return;

        // as currently the validation lib only reads from file system,
        // we export the datasets as a ZIP file
        List<File> files = new ArrayList<>();
        List<DataSet> allDatasets = new ArrayList<>(this.datasets);

        // add dependencies
        ProcessDao dao = new ProcessDao();
        for (DataSet ds : this.datasets) {
            Set<DataSet> dependencies = dao.getDependencies(ds, DependenciesMode.ALL_FROM_DATASTOCK);
            allDatasets.addAll(dependencies);
        }

        this.numberOfDatasets = allDatasets.size();

        // export as temporary file and validate
        boolean exportWithDependencies = (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.links")));

        File tmpFile = this.exportDataSets(allDatasets, exportWithDependencies);
        files.add(tmpFile);
        this.validateFiles(files);

        // delete the temporary file
        new File(tmpFile.getAbsolutePath()).delete();
    }

    private void validateStock() throws InterruptedException {
        logger.debug("stock validation");
        List<File> files = new ArrayList<>();
        files.add(this.getFileToValidate());
        this.validateFiles(files);
    }

    /**
     * @param files to validate
     * @throws InterruptedException
     */
    public void validateFiles(List<File> files) throws InterruptedException {
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.categories")))
            validators.add(new CategoryValidator());
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.links"))) {
            LinkValidator v = new LinkValidator();
            v.setParameter(LinkValidator.PARAM_IGNORE_REFERENCE_OBJECTS, true);
            v.setParameter(LinkValidator.PARAM_IGNORE_REFS_TO_LCIAMETHODS, true);
            v.setParameter(LinkValidator.PARAM_IGNORE_PRECEDINGDATASETVERSION, true);
            validators.add(v);
        }
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.orphaned.items"))) {
            OrphansValidator v = new OrphansValidator();
            v.setParameter(OrphansValidator.PARAM_IGNORE_REFERENCE_OBJECTS, true);
            validators.add(v);
        }
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.reference.flows")))
            validators.add(new ReferenceFlowValidator());
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.xml.schemas"))) {
            SchemaValidator v = new SchemaValidator();
            v.setParameter(SchemaValidator.PARAM_IGNORE_REFERENCE_OBJECTS, true);
            validators.add(v);
        }
        if (this.getSelectedOptions().contains(bundle.getString("admin.validator.aspect.xslt.stylesheet"))) {
            XSLTStylesheetValidator v = new XSLTStylesheetValidator();
            v.setParameter(XSLTStylesheetValidator.PARAM_IGNORE_REFERENCE_OBJECTS, true);
            validators.add(v);
        }
        if (this.getSelectedOptions().contains(bundle.getString("admin.aspect.default")))
            validators.add(new ILCDFormatValidator());
        int i = 0;
        for (AbstractDatasetsValidator validator : validators) {
            i++;
            if (profileHandler.getSelectedProfile() != null) {
                validator.setProfile(profileHandler.getSelectedProfile());
                if (logger.isDebugEnabled())
                    logger.debug("Setting profile " + profileHandler.getSelectedProfile().getName());
            }
            validator.setUpdateEventListener(statusProgress);
            validator.setReportSuccesses(this.positiveValidationFlag);

            for (File file : files) {
                if (logger.isDebugEnabled()) {
                    logger.debug("adding file " + file.getAbsolutePath() + " to validation queue");
                    if (logger.isTraceEnabled())
                        logger.trace("file exists: " + file.exists());
                }
                validator.setObjectsToValidate(file);
                boolean fileValidated = false;
                boolean result = true;
                try {
                    result = validator.validate();
                    fileValidated = true;
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage());
                }
                if (fileValidated) {
                    processResult(validator, result);
                }
            }
        }
        logger.debug("validation executed using " + i + " validators");
    }

    private void processResult(AbstractDatasetsValidator validator, boolean resultOk) {
        if (this.positiveValidationFlag) {
            processResultPositively(validator, resultOk);
        } else {
            processResultNegatively(validator, resultOk);
        }
    }

    private void processResultNegatively(AbstractDatasetsValidator validator, boolean resultOk) {
        if (!resultOk) {
            events.addAll(validator.getEventsList().getEvents());
            success = false;
        }
    }

    private void processResultPositively(AbstractDatasetsValidator validator, boolean resultOk) {
        events.addAll(validator.getEventsList().getEvents());
        if (!resultOk)
            success = false;
    }


    /**
     * @param file or archive to be validated
     */
    private void validateArchive(File file) {
        archiveValidator = new ArchiveValidator();
        archiveValidator.setUpdateEventListener(statusProgress);

        if (this.getSelectedOptions().contains("Archive Structure") && (this.getSelectedOptions().size() == 1)) {
            statusProgress.setProgress(100);
            archiveValidator.setArchiveToValidate(file);
            boolean result = archiveValidator.validate();
            if (!result) {
                events.addAll(archiveValidator.getEventsList().getEvents());
            }
        }
    }

    /**
     * @return the files to validate for a stock
     */
    public File getFileToValidate() {
        String fileName = null;
        File file = null;

        if (org.apache.commons.lang3.StringUtils.isEmpty(this.stockId)) {
            logger.debug("no stock id provided, returning null");
            return null;
        }

        CommonDataStockDao cdao = new CommonDataStockDao();
        AbstractDataStock ads = cdao.getDataStockById(this.stockId);
        ExportTag tag = ads.getExportTag(ExportType.ZIP, exportController.getExportMode());

        if (logger.isDebugEnabled())
            logger.debug("getting file for data stock " + ads.getName());

        if (tag.isModified()) {
            logger.debug("cached file is outdated, generating new file");
            file = exportDataStock(ads).toFile();
        } else {
            logger.debug("serving cached file ");
            fileName = tag.getFile();
            file = new File(fileName);
            if (!file.exists()) {
                logger.debug("cached file not found, generating new one");
                file = exportDataStock(ads).toFile();
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("returning " + file.getAbsolutePath());

        return file;

    }

    private File exportDataSets(List<DataSet> datasets, boolean dependencies) {
        if (logger.isDebugEnabled())
            logger.debug("generating new export ZIP for " + datasets.size() + " datasets");

        exportController.setExportMode(ExportMode.ALL);
        exportController.setDependencies(dependencies);
        try {
            return exportController.export(datasets).toFile();
        } catch (IOException e) {
            if (logger.isDebugEnabled())
                logger.error("could not export datasets ", e);
            return null;
        }
    }

    private Path exportDataStock(AbstractDataStock ads) {
        if (logger.isDebugEnabled())
            logger.debug("generating new export ZIP for " + ads.getName());

        exportController.setExportMode(ExportMode.ALL);
        try {
            return exportController.export(ads, ExportType.ZIP);
        } catch (IOException e) {
            if (logger.isDebugEnabled())
                logger.error("could not export data stock " + ads.getName(), e);
            return null;
        }
    }

    public String clearFilesList() {
        this.statusProgress.cancel();
        this.clearListSelectedOptions();
        logger.info("clearing List");
        this.importHandler.getUploadedFiles().clear();
        return "/admin/importUpload.xhtml?faces-redirect=true";

    }

    public void clearEvents() {
        this.events.clear();
        statusProgress.setProgress(0);
    }

    public List<String> getDocumentViewHeader() {
        return this.documentViewHeader;
    }

    public void setDocumentViewHeader(List<String> documentViewHeader) {
        this.documentViewHeader = documentViewHeader;
    }

    public void generateDocumentViewHeader() {
        this.documentViewHeader.clear();

        StringBuffer selectedAspects = new StringBuffer();
        for (int i = 0; i < this.getSelectedOptions().size(); i++) {
            selectedAspects = selectedAspects.append(this.getSelectedOptions().get(i));
            if (i < this.getSelectedOptions().size() - 1)
                selectedAspects.append("; ");
        }

        String documentViewProfile = this.profileHandler.getSelectedProfile().getName() + " v" + this.profileHandler.getSelectedProfile().getVersion();
        String soda4lcaVersion = ConfigurationService.INSTANCE.getVersionTag();

        this.documentViewHeader.add(selectedAspects.toString());
        this.documentViewHeader.add(documentViewProfile);
        this.documentViewHeader.add(soda4lcaVersion);
    }

    public String goToDataset(String uuid, String version, DatasetType type) throws FormatException {
        return goToDataset(uuid, version, type, false);
    }

    public String goToDataset(String uuid, String version, DatasetType type, boolean overview) throws FormatException {
        AbstractDataSetURLGenerator<? extends DataSet> generator = null;

        switch (type) {
            case PROCESS:
                generator = this.urlGenerator.getProcess();
                break;
            case FLOW: {
                // as we do not know what sort of flow it is, we need to look this up
                // TODO: embed this in FlowURLGenerator
                DataSetVersion dsVersion = null;
                try {
                    dsVersion = DataSetVersion.parse(version);
                } catch (FormatException e) {
                    //TODO show error page
                    throw e;
                }
                ProductFlowDao pfDao = new ProductFlowDao();
                ProductFlow productFlow = pfDao.getByUuidAndVersion(uuid, dsVersion);
                if (productFlow != null) {
                    generator = this.urlGenerator.getPflow();
                    break;
                }

                ElementaryFlowDao efDao = new ElementaryFlowDao();
                ElementaryFlow elFlow = efDao.getByUuidAndVersion(uuid, dsVersion);
                if (elFlow != null) {
                    generator = this.urlGenerator.getEflow();
                    break;
                }
                break;
            }
            case CONTACT:
                generator = this.urlGenerator.getContact();
                break;
            case SOURCE:
                generator = this.urlGenerator.getSource();
                break;
            case FLOWPROPERTY:
                generator = this.urlGenerator.getFlowProperty();
                break;
            case UNITGROUP:
                generator = this.urlGenerator.getUnitGroup();
                break;
            case LCIAMETHOD:
                generator = this.urlGenerator.getLciaMethod();
                break;
            default:
                return "../error.xhtml";
        }

        if (overview)
            return generator.getDetail(uuid, version, this.stockName);
        else
            return generator.getResourceDetailHtmlWithStock(uuid, version, this.stockName);
    }

    public List<IDatasetReference> getRefsToProcessesForFlow(String flowUuid) {

        List<Process> processes = getProcessesForFlow(flowUuid);

        List<IDatasetReference> result = new ArrayList<>();

        for (Process process : processes) {
            IDatasetReference ref = new DatasetReference(process.getUuidAsString(), process.getVersion().getVersionString(), DatasetType.PROCESS);
            ref.setName(process.getBaseName().getDefaultValue());
            result.add(ref);
        }

        return result;
    }

    public String getRefsAsString(List<IDatasetReference> references) {
        StringBuilder sb = new StringBuilder();

        for (IDatasetReference ref : references) {
            if (sb.length() != 0) {
                sb.append("; ");
            }
            sb.append(ref.getName());
            sb.append(" (");
            sb.append(ref.getUuid());
            sb.append(")");
        }

        return sb.toString();
    }

    private List<Process> getProcessesForFlow(String flowUuid) {
        ProcessDao dao = new ProcessDao();
        return dao.getProcessesForExchangeFlow(flowUuid, null, 0, 10);
    }

    public URLGeneratorBean getUrlGenerator() {
        return urlGenerator;
    }

    public void setUrlGenerator(URLGeneratorBean urlGenerator) {
        this.urlGenerator = urlGenerator;
    }

    public StockSelectionHandler getStockSelectionHander() {
        return stockSelectionHander;
    }

    public void setStockSelectionHander(StockSelectionHandler stockSelectionHander) {
        this.stockSelectionHander = stockSelectionHander;
    }

    public ValidationContext getValidationContext() {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext) {
        this.validationContext = validationContext;
    }

    public int getNumberOfDatasets() {
        return numberOfDatasets;
    }

    public void setNumberOfDatasets(int numberOfDatasets) {
        this.numberOfDatasets = numberOfDatasets;
    }

    public void handleProfileSelection(AjaxBehaviorEvent event) {
        // TODO: do this only if load aspect presets are enabled
        if (profileHandler.getSelectedProfile() != null && profileHandler.getSelectedProfile().getActiveAspects() != null) {
            logger.debug("applying aspect presets");
            this.clearListSelectedOptions();
            StringTokenizer st = new StringTokenizer(profileHandler.getSelectedProfile().getActiveAspects(), ",");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                logger.debug("setting " + s);
                this.selectedOptions.add(translateAspect(s));
            }
        }
    }

    private String translateAspect(String aspectName) {
        switch (aspectName) {
            case "Links":
                return bundle.getString("admin.validator.aspect.links");
            case "Orphaned Items":
                return bundle.getString("admin.validator.aspect.orphaned.items");
            case "XML Schema Validity":
                return bundle.getString("admin.validator.aspect.xml.schemas");
            case "Profile Specific Rules":
                return bundle.getString("admin.validator.aspect.xslt.stylesheet");
            case "Custom Validity":
                return bundle.getString("admin.validator.aspect.xslt.stylesheet");
            case "Reference Flows":
                return bundle.getString("admin.validator.aspect.reference.flows");
            case "Categories":
                return bundle.getString("admin.validator.aspect.categories");
        }
        return null;
    }


    public Boolean getPositiveValidationFlag() {
        return positiveValidationFlag;
    }


    public void setPositiveValidationFlag(Boolean positiveValidationFlag) {
        this.positiveValidationFlag = positiveValidationFlag;
    }
}