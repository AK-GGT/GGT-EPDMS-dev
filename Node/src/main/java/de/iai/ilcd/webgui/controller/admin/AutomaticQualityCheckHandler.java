package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataStockRestriction;
import de.iai.ilcd.model.common.QualityCheckEvent;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.service.AutomaticQualityCheckService;
import de.iai.ilcd.service.exception.NoCategoryException;
import de.iai.ilcd.util.SodaResourceBundle;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ui.StockSelectionHandler;
import de.iai.ilcd.webgui.controller.url.AbstractDataSetURLGenerator;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import de.iai.ilcd.webgui.util.Consts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * The view class for the Quantitative Quality Assurance.
 *
 * @author sarai
 */
@ManagedBean(name = "automaticQualityCheckHandler")
@ViewScoped
public class AutomaticQualityCheckHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = 7998073754372131794L;
    private static Logger logger = LogManager.getLogger(AutomaticQualityCheckHandler.class);
    public SodaResourceBundle bundle = new SodaResourceBundle();
    DataStock stock;
    String stockName;
    boolean showAll = false;
    private List<Process> selectedProcesses = new ArrayList<Process>();
    private AutomaticQualityCheckService service = new AutomaticQualityCheckService();
    private IDataStockMetaData selectedStock;
    private List<DataStock> selectedStocks = new ArrayList<DataStock>();
    @ManagedProperty(value = "#{stockListHandler}")
    private StockListHandler stockListHandler = new StockListHandler();
    private Integer threshold = ConfigurationService.INSTANCE.getQqaThreshold();
    private Integer warnThreshold = ConfigurationService.INSTANCE.getQqaWarnThreshold();
    private List<QualityCheckEvent> events;
    private List<String> categories = new ArrayList<String>();
    private List<String> modules = new ArrayList<String>();
    private Map<String, String> indicators = new HashMap<String, String>();
    private List<String> referenceCounts = new ArrayList<String>();
    /**
     * Selected lists for selected filter items
     */
    private List<String> selectedCategories = new ArrayList<String>();
    private List<String> selectedModules = new ArrayList<String>();
    private List<String> selectedIndicators = new ArrayList<String>();
    private List<String> selectedReferenceCounts = new ArrayList<String>();
    @ManagedProperty("#{url}")
    private URLGeneratorBean urlGenerator;
    @ManagedProperty(value = "#{stockSelection}")
    private StockSelectionHandler stockSelectionHandler;

    /**
     * Sets up the View.
     */
    @SuppressWarnings("unchecked")
    public AutomaticQualityCheckHandler() {
        logger.trace("setting up quality checker");
        this.selectedProcesses = (List<Process>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_DATASETS);

        logger.trace("set up quality checker.");
    }

    /**
     * {@inheritDoc}
     */
    @PostConstruct
    public void init() {
        logger.trace("Begin to initialize quality checker.");
        events = new ArrayList<QualityCheckEvent>();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        if (logger.isDebugEnabled()) {
            logger.debug("validationContext " + (String) request.getAttribute("validationContext"));
            logger.debug("stockId " + (String) request.getParameter("stockId"));
            logger.debug("stockName " + (String) request.getParameter("stockName"));
        }

        this.stockName = stockSelectionHandler.getCurrentStockName();
        this.stockListHandler.setDataStockRestriction(DataStockRestriction.LOGICAL);

        logger.trace("initialized quality checker.");
    }

    public String goToDataset(String uuid, String version) throws FormatException {
        return goToDataset(uuid, version, false);
    }

    /**
     * Goes to data set with given UUID and version.
     *
     * @param uuid     The UUID of data set to go to
     * @param version  The version of data set to go to
     * @param overview A flag indicating whether only the data set overview shall be shown or the complete data set
     * @return The link to the corresponding data set as String
     */
    public String goToDataset(String uuid, String version, boolean overview) {
        AbstractDataSetURLGenerator<? extends DataSet> generator = this.urlGenerator.getProcess();

        if (overview)
            return generator.getDetail(uuid, version, this.stockName);
        else
            return generator.getResourceDetailHtmlWithStock(uuid, version, this.stockName);
    }

    /**
     * The sort method for deviation message comparison.
     *
     * @param obj1 The first message to compare
     * @param obj2 The second message to compare
     * @return -1 if first object is smaller than the second, 0 if both have equal deviation and 1 if first object has greater deviation than second
     */
    public long sortByDeviation(Object obj1, Object obj2) {
        String dev1 = null;
        String dev2 = null;
        if (obj1 instanceof String) {
            dev1 = (String) obj1;
        }

        if (obj2 instanceof String) {
            dev2 = (String) obj2;
        }
        String[] val1 = dev1.split("%");
        String[] val2 = dev2.split("%");

        long value1 = Math.abs(Long.parseLong(val1[0]));
        long value2 = Math.abs(Long.parseLong(val2[0]));

        if (value1 < value2) {
            return -1;
        } else if (value1 == value2) {
            return 0;
        } else {
            return 1;
        }
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getWarnThreshold() {
        return warnThreshold;
    }

    public void setWarnThreshold(Integer warnThreshold) {
        this.warnThreshold = warnThreshold;
    }

    public IDataStockMetaData getSelectedStock() {
        return selectedStock;
    }

    public void setSelectedStock(IDataStockMetaData selectedStock) {
        this.selectedStock = selectedStock;
    }

    public List<QualityCheckEvent> getEvents() {
        return events;
    }

    public void setEvents(List<QualityCheckEvent> events) {
        this.events = events;
    }

    public URLGeneratorBean getUrlGenerator() {
        return urlGenerator;
    }

    public void setUrlGenerator(URLGeneratorBean urlGenerator) {
        this.urlGenerator = urlGenerator;
    }

    public StockSelectionHandler getStockSelectionHandler() {
        return stockSelectionHandler;
    }

    public void setStockSelectionHandler(StockSelectionHandler stockSelectionHander) {
        this.stockSelectionHandler = stockSelectionHander;
    }

    public StockListHandler getStockListHandler() {
        return stockListHandler;
    }

    public void setStockListHandler(StockListHandler stockListHandler) {
        this.stockListHandler = stockListHandler;
    }

    public void clearEvents() {
        events.clear();
    }

    public List<Process> getSelectedProcesses() {
        return selectedProcesses;
    }

    public void setSelectedProcesses(List<Process> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
    }

    public List<DataStock> getSelectedStocks() {
        return selectedStocks;
    }

    public void setSelectedStocks(List<DataStock> selectedStocks) {
        this.selectedStocks = selectedStocks;
    }

    public boolean getShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    /**
     * Makes the automatic quality check for all processes selected previously.
     */
    public void check() {
        events = new ArrayList<QualityCheckEvent>();
        if (logger.isDebugEnabled()) {
            logger.debug("showAll is set: " + showAll);
        }

        try {
            for (Process process : selectedProcesses) {
                events.addAll(service.checkProcess(process, selectedStocks, Double.valueOf(threshold), showAll));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("events has " + events.size() + " entries");
            }

            categories = service.getCategories(events);
            Collections.sort(categories);
            modules = service.getModules(events);
            Collections.sort(modules);
            indicators = new TreeMap<String, String>(service.getIndicators(events));
            if (logger.isTraceEnabled()) {
                logger.trace("List of indicators has size: " + indicators.size());
            }
            referenceCounts = service.getReferenceCounts(events);
            Collections.sort(referenceCounts);
        } catch (NoCategoryException nce) {
            this.addI18NFacesMessage("facesMsg.automaticQualityCheck.NoCategory", FacesMessage.SEVERITY_ERROR);
        }

        boolean statsIsNull = false;
        for (QualityCheckEvent event : events) {
            if (event.getMinValue() == null || event.getMaxValue() == null || event.getAvgValue() == null || event.getReferenceCount() == null) {
                statsIsNull = true;
            }
        }

        if (statsIsNull) {
            this.addI18NFacesMessage("facesMsg.automaticQualityCheck.emptyStats", FacesMessage.SEVERITY_INFO);
        }

    }

    /**
     * Clears all filters of the result event table.
     */
    public void clearAllFilters() {
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("validation:qqaAcordionPanel:logTable");
        logger.debug("Starting to reset filters.");
        if (!dataTable.getFilterBy().isEmpty()) {
            dataTable.reset();

            //resetting checkBoxes for filters
            selectedCategories.clear();
            selectedModules.clear();
            selectedIndicators.clear();
            selectedReferenceCounts.clear();

            PrimeFaces.current().executeScript("validation:qqaAcordionPanel:logTable");
            logger.debug("reset filters.");
        }
    }

    public boolean isNoStockSelected() {
        return (selectedStocks == null) || (selectedStocks.size() == 0);
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getModules() {
        return modules;
    }

    public Map<String, String> getIndicators() {
        return indicators;
    }

    public List<String> getreferenceCounts() {
        return referenceCounts;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(List<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    public List<String> getSelectedModules() {
        return selectedModules;
    }

    public void setSelectedModules(List<String> selectedModules) {
        this.selectedModules = selectedModules;
    }

    public List<String> getSelectedIndicators() {
        return selectedIndicators;
    }

    public void setSelectedIndicators(List<String> selectedIndicators) {
        this.selectedIndicators = selectedIndicators;
    }

    public List<String> getSelectedReferenceCounts() {
        return selectedReferenceCounts;
    }

    public void setSelectedReferenceCounts(List<String> selectedReferenceCounts) {
        this.selectedReferenceCounts = selectedReferenceCounts;
    }

    /**
     * Gets the rounded value of a given floating point value.
     *
     * @param value The value that has to be rounded
     * @return The rounded representation of the value
     */
    public Double getRoundedValue(String value) {
        BigDecimal valuebd = new BigDecimal(value).setScale(7, RoundingMode.HALF_EVEN);
        Double valueRounded = valuebd.doubleValue();
        return valueRounded;
    }

    /**
     * Extracts the acronym of a given indicator name.
     *
     * @param indicatorName The name of indicator whose acronym has to be extracted from
     * @return The acronym of indicator name
     */
    public String getAcronym(String indicatorName) {
        return service.getAcronym(indicatorName);
    }
}
