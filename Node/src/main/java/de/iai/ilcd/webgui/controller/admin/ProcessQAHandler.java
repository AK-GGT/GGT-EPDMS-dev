package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.ClassificationDao;
import de.iai.ilcd.model.dao.LciaResultClClassStatsDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.*;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.util.SodaUtil;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.chart.*;

import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.Map.Entry;

/**
 * Handler for the processes for quality assurance.
 */
@ViewScoped
@ManagedBean(name = "pqaHandler")
public class ProcessQAHandler extends AbstractProcessQAHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8548483395299377253L;

    /**
     * List of processes
     */
    private final List<Process> processes = new ArrayList<Process>();

    /**
     * All chart models for {@link Process#getExchangesExcludingRefFlows()} with direction
     * {@link ExchangeDirectionValues#INPUT}
     */
    private final List<QAChartModel> allExchangesInCharts = new ArrayList<QAChartModel>();

    /**
     * All chart models for {@link Process#getExchangesExcludingRefFlows()} with direction
     * {@link ExchangeDirectionValues#OUTPUT}
     */
    private final List<QAChartModel> allExchangesOutCharts = new ArrayList<QAChartModel>();

    /**
     * All chart models for {@link Process#getLciaResults()}
     */
    private final List<QAChartModel> allLciaResultCharts = new ArrayList<QAChartModel>();

    /**
     * Flag to indicate if exchange charts shall be calculated
     */
    private boolean doExchangeCharts = false;

    /**
     * Flag to indicate if LCIAResult charts shall be calculated
     */
    private boolean doLciaResultCharts = true;

    /**
     * Statistics flag
     */
    private boolean statistics = true;

    /**
     * Category
     */
    private ClClass category;

    /**
     * {@inheritDoc}
     */
    @Override
    public void postConstruct() {
        try {
            // loaded this way due to the lack of support for multiple value view parameters of JSF/Mojarra
            String[] uuidsQueryParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("dataset");

            ProcessDao pDao = new ProcessDao();

            // uuidsQueryParams may be: uuid || uuid_version
            for (String uuidQueryParam : uuidsQueryParams) {
                String uuid = uuidQueryParam;
                DataSetVersion version = null;

                // if "_" found -> assume version afterwards
                int idxUnderscore = uuidQueryParam.indexOf('_');
                if (idxUnderscore >= 0) {
                    uuid = uuidQueryParam.substring(0, idxUnderscore);
                    version = DataSetVersion.parse(uuidQueryParam.substring(idxUnderscore + 1));
                }

                Process p = version != null ? pDao.getByUuidAndVersion(uuid, version) : pDao.getByUuid(uuid);
                if (p != null) {
                    SecurityUtil.assertCanRead(p);
                    this.processes.add(p);
                }
            }

            if (CollectionUtils.isNotEmpty(this.processes)) {
                QAGrouping groupBy = QAGrouping.MODULES;
                try {
                    // not to mix with f:viewParam, groupBy also loaded this way
                    String[] groupByStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("groupBy");
                    if (groupByStr != null && groupByStr.length > 0) {
                        groupBy = QAGrouping.valueOf(groupByStr[0].toUpperCase());
                    }

                    this.doExchangeCharts = this.getBoolRequestParam("exchanges", this.isDoExchangeCharts());
                    this.doLciaResultCharts = this.getBoolRequestParam("lciaResults", this.isDoLciaResultCharts());
                    this.statistics = this.getBoolRequestParam("statistics", this.statistics);
                } catch (Exception e) {
                    // nothing to worry, assume MODULES
                }

                // category
                try {
                    String[] clIdArr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("category");
                    if (clIdArr != null && clIdArr.length > 0) {
                        String clId = clIdArr[0];
                        ClassificationDao cDao = new ClassificationDao();
                        this.category = cDao.getByClId(clId);
                    }
                } catch (Exception e) {
                    // if anything goes wrong -> use ClClass from process
                }

                this.prepareForCompare(groupBy);
            }
        } catch (Exception e) {
            throw new FacesException(e);
        }
    }

    /**
     * Get a boolean value from request parameters
     *
     * @param name       name of the parameter
     * @param defaultVal default value
     * @return boolean value from request parameters
     */
    private boolean getBoolRequestParam(String name, boolean defaultVal) {
        try {
            String[] paramArr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get(name);
            if (paramArr != null && paramArr.length > 0) {
                return Boolean.parseBoolean(paramArr[0]);
            }
        } catch (Exception e) {
        }
        return defaultVal;
    }

    /**
     * Get the chart models for exchanges (direction: {@link ExchangeDirectionValues#INPUT}), only set if
     * {@link #isDoExchangeCharts()} returns <code>true</code>
     *
     * @return the list of models
     */
    public List<QAChartModel> getAllExchangesInCharts() {
        return this.allExchangesInCharts;
    }

    /**
     * Get the chart models for exchanges (direction: {@link ExchangeDirectionValues#OUTPUT}), only set if
     * {@link #isDoExchangeCharts()} returns <code>true</code>
     *
     * @return the list of models
     */
    public List<QAChartModel> getAllExchangesOutCharts() {
        return this.allExchangesOutCharts;
    }

    /**
     * Get the chart models for LCIA results, only set if {@link #isDoLciaResultCharts()} returns <code>true</code>
     *
     * @return the list of models
     */
    public List<QAChartModel> getAllLciaResultCharts() {
        return this.allLciaResultCharts;
    }

    /**
     * Get list of processes
     *
     * @return list of processes
     */
    public List<Process> getProcesses() {
        return this.processes;
    }

    /**
     * Get the exchanges charts active flag
     *
     * @return <code>true</code> if exchanges charts are active, <code>false</code> otherwise
     */
    public boolean isDoExchangeCharts() {
        return this.doExchangeCharts;
    }

    /**
     * Set the exchanges charts active flag
     *
     * @param doExchangeCharts flag to set
     */
    public void setDoExchangeCharts(boolean doExchangeCharts) {
        this.doExchangeCharts = doExchangeCharts;
    }

    /**
     * Get the LCIA result charts active flag
     *
     * @return <code>true</code> if LCIA result charts are active, <code>false</code> otherwise
     */
    public boolean isDoLciaResultCharts() {
        return this.doLciaResultCharts;
    }

    /**
     * Set the LCIA result charts active flag
     *
     * @param doLciaResultCharts flag to set
     */
    public void setDoLciaResultCharts(boolean doLciaResultCharts) {
        this.doLciaResultCharts = doLciaResultCharts;
    }


    /**
     * Prepare everything for the compare operation
     */
    private void prepareForCompare(QAGrouping groupBy) {
        // get the keys together for all indicators
        List<ProcessQAHelper> helpers = new ArrayList<ProcessQAHelper>();

        IndicatorUuidList indicatorsExInUuids = new IndicatorUuidList(ExchangeDirectionValues.INPUT);
        IndicatorUuidList indicatorsExOutUuids = new IndicatorUuidList(ExchangeDirectionValues.OUTPUT);
        IndicatorUuidList indicatorsLciaResultsUuids = new IndicatorUuidList(null);

        // create helpers for all processes (fills both UUID lists)
        for (Process p : this.processes) {
            helpers.add(new ProcessQAHelper(p, indicatorsExInUuids, indicatorsExOutUuids, indicatorsLciaResultsUuids));
        }

        // Generate the charts for input/output
        this.allExchangesInCharts.addAll(this.createChartsForIndicatorList(groupBy, helpers, indicatorsExInUuids));
        this.allExchangesOutCharts.addAll(this.createChartsForIndicatorList(groupBy, helpers, indicatorsExOutUuids));
        this.allLciaResultCharts.addAll(this.createChartsForIndicatorList(groupBy, helpers, indicatorsLciaResultsUuids));

        if (this.doLciaResultCharts && QAGrouping.MODULES.equals(groupBy) && this.statistics) {
            if (this.category == null) {
                this.category = SodaUtil.getHighestClClass(this.processes.get(0).getClassification());
            }
            if (this.category != null) {
                this.prepareLciaResultStatistics(this.category.getClId(), this.category.getName());
            }

        }
    }

    /**
     * Prepare LCIA Result Statistics
     *
     * @param clid      class id
     * @param className name of the class
     */
    private void prepareLciaResultStatistics(String clid, String className) {
        LciaResultClClassStatsDao dao = new LciaResultClClassStatsDao();

        for (QAChartModel m : this.allLciaResultCharts) {
            Set<Object> modules = m.getSeries().get(0).getData().keySet();

            Axis x2Axis = new CategoryAxis();
            x2Axis.setTickCount(-1);
            m.getAxes().put(AxisType.X2, x2Axis);


            int idx = 1;
            LineChartSeries mmmSeries = new LineChartSeries("Min/Mean/Max " + clid + " " + className);
            for (Object module : modules) {
                List<Long> stocks = new ArrayList<Long>();
                stocks.add(Long.valueOf(1));
                LciaResultClClassStats stats = dao.get(clid, m.getMethodUuid(), (String) module, stocks, m.getReferenceFlowpropertyUuid());

                mmmSeries.setXaxis(AxisType.X2);

                // left null value fill-up for gap
                mmmSeries.set(idx++, null);

                // actual values
                if (stats != null) {
                    mmmSeries.set(idx++, stats.getMin());
                    mmmSeries.set(idx++, stats.getMean());
                    mmmSeries.set(idx++, stats.getMax());
                }
                // no stats possible -> fill up with nulls for correct alignment in chart
                else {
                    mmmSeries.set(idx++, null);
                    mmmSeries.set(idx++, null);
                    mmmSeries.set(idx++, null);
                }
                // right null value fill-up for gap
                mmmSeries.set(idx++, null);
            }

            m.addSeries(mmmSeries);
            m.updateLegendCols();
        }
    }

    /**
     * Create charts for the indicator list
     *
     * @param grouping        grouping value
     * @param helpers         all process helpers
     * @param indicatorsUuids the indicator UUIDs
     */
    private List<QAChartModel> createChartsForIndicatorList(QAGrouping grouping, List<ProcessQAHelper> helpers, IndicatorUuidList indicatorsUuids) {
        List<QAChartModel> foo = new ArrayList<QAChartModel>();
        for (String indicatorUuid : indicatorsUuids) {

            // map with ChartSeries helpers to collect everything together according to provided grouping method
            Map<String, ChartSeriesValueHelper> chartSeriesMapHelper = new LinkedHashMap<String, ChartSeriesValueHelper>();


            // name and unit of indicator, will be filled by first process (must be the same across all processes)
            String indicatorName = null;
            String indicatorMethodUuid = null;
            String indicatorUnit = null;
            List<String> referenceFlowpropertiesUuids = new ArrayList<String>();

            List<Exchange> referenceExchanges = helpers.get(0).process.getReferenceExchanges();

            for (Exchange referenceExchange : referenceExchanges) {
                referenceFlowpropertiesUuids.add(referenceExchange.getFlowWithSoftReference().getReferenceFlowProperty().getReference().getUuid().getUuid());
            }

            // actual collection
            for (ProcessQAHelper h : helpers) {
                String procLabel = SodaUtil.getLStringValueWithFallback(this.getLang(), h.process.getBaseName());
                String procUuid = h.process.getUuidAsString();


//				System.out.println("LCIA result name: " + h.process.getLciaResults().get(0).getMethodReference().getShortDescription().getValue());
//				System.out.println("lcia result amount: " + h.process.getLciaResults().get(0).getAmounts().get(0).getValue());

                Map<String, IndicatorShell> byUuid = h.getIndicatorsByUuid(indicatorsUuids.getDirection());
                IndicatorShell indicator = byUuid.get(indicatorUuid);
                // only for first process, values are the same => use from first
                if (indicatorName == null) {
                    try {
                        indicatorName = SodaUtil.getLStringValueWithFallback(this.getLang(), indicator.getName());
                        indicatorUnit = SodaUtil.getLStringValueWithFallback(this.getLang(), indicator.getUnit());
                        indicatorMethodUuid = indicator.getMethodUuid();
                    } catch (NullPointerException e) {
                    }
                }

                for (Amount a : indicator.getAmounts()) {
                    grouping.handleChartSeries(chartSeriesMapHelper, a, procUuid, procLabel);
                }
            }
            // all charts collected, create the model for them
            QAChartModel model = new QAChartModel();
            model.setTitle(indicatorName);
            model.setMethodUuid(indicatorMethodUuid);
            model.setUnit(indicatorUnit);
            model.setDirection(indicatorsUuids.getDirection());

            // first: collect all series keys (e.g. modules) to know all possible keys throughout all processes
            // Example: proc#1 has A1-A3 but proc#2 has A1-A3 and A5
            List<String> seriesKeys = new ArrayList<String>();
            for (Entry<String, ChartSeriesValueHelper> chart : chartSeriesMapHelper.entrySet()) {
                Set<String> localKeys = chart.getValue().keySet();
                for (String localKey : localKeys) {
                    if (!seriesKeys.contains(localKey)) {
                        seriesKeys.add(localKey);
                    }
                }
            }
            Collections.sort(seriesKeys);

            // second: fill missing keys with NaN => required for correct rendering of primefaces chart
            // and fill the actual chart series (important to do after collection all series keys -> order
            // of value adding is important in ChartSeries class).
            for (Entry<String, ChartSeriesValueHelper> chart : chartSeriesMapHelper.entrySet()) {
                ChartSeriesValueHelper csvh = chart.getValue();

                ChartSeries cs = new ChartSeries();
                cs.setLabel(csvh.getLabel());
                for (String key : seriesKeys) {
                    if (!csvh.containsKey(key)) {
                        cs.set(key, Float.NaN);
                    } else {
                        cs.set(key, csvh.get(key));
                    }
                }
                model.addSeries(cs);
            }


            model.setCountPerSeries(seriesKeys.size());
            model.getAxis(AxisType.Y).setLabel(model.getUnit());
            model.updateLegendCols();
            for (String referenceFlowpropertyUuid : referenceFlowpropertiesUuids) {
                model.setReferenceFlowpropertyUuid(referenceFlowpropertyUuid);
                foo.add(model);
            }
        }
        return foo;
    }

    /**
     * Get the category the statistics are shown for
     *
     * @return category the statistics are shown for
     */
    public ClClass getCategory() {
        return this.category;
    }

    /**
     * Grouping options for QA
     */
    public enum QAGrouping {
        /**
         * Group by modules
         */
        MODULES {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleChartSeries(Map<String, ChartSeriesValueHelper> chartSeriesMapHelper, Amount a, String procUuid, String procLabel) {
                ChartSeriesValueHelper chartSeries = chartSeriesMapHelper.get(procUuid);
                if (chartSeries == null) {
                    chartSeries = new ChartSeriesValueHelper();
                    chartSeries.setLabel(procLabel);
                    chartSeriesMapHelper.put(procUuid, chartSeries);
                }
                // add the value for the process
                chartSeries.put(a.getModule(), a.getValue());
            }
        },
        /**
         * Group by processes
         */
        PROCESSES {
            /**
             * {@inheritDoc}
             */
            @Override
            public void handleChartSeries(Map<String, ChartSeriesValueHelper> chartSeriesMapHelper, Amount a, String procUuid, String procLabel) {
                ChartSeriesValueHelper chartSeries = chartSeriesMapHelper.get(a.getModule());
                if (chartSeries == null) {
                    chartSeries = new ChartSeriesValueHelper();
                    chartSeries.setLabel(a.getModule());
                    chartSeriesMapHelper.put(a.getModule(), chartSeries);
                }
                // add the value for the process
                chartSeries.put(procLabel, a.getValue());
            }

        };

        /**
         * Handle the chart series
         *
         * @param chartSeriesMapHelper map of chart series helper
         * @param a                    amount
         * @param procUuid             UUID of process
         * @param procLabel            label of process
         */
        public abstract void handleChartSeries(Map<String, ChartSeriesValueHelper> chartSeriesMapHelper, Amount a, String procUuid, String procLabel);

    }

    /**
     * Shell class for indicator data
     */
    public interface IndicatorShell {

        /**
         * Get the name of the indicator
         *
         * @return name of the indicator
         */
        public IMultiLangString getName();

        /**
         * Get the unit of the indicator
         *
         * @return unit of the indicator
         */
        public IMultiLangString getUnit();

        /**
         * Get the list of amounts
         *
         * @return list of amounts
         */
        public List<Amount> getAmounts();

        /**
         * Get the method UUID
         *
         * @return method UUID
         */
        public String getMethodUuid();
    }

    /**
     * Helper to store the values for the chart series to create an actual chart series from it after all values have
     * been collected and missing values for a series key (e.g. module) have been filled with <code>null</code>s.
     */
    private static class ChartSeriesValueHelper extends HashMap<String, Number> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -3849994429924704107L;

        /**
         * Label
         */
        private String label;

        /**
         * Get the label
         *
         * @return the label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Set the label
         *
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

    }

    /**
     * Helper class for the Process quality assurance which manages indicators and maps of UUID&rarr;indicators for
     * each {@link ExchangeDirectionValues direction}
     */
    private class ProcessQAHelper {

        /**
         * The process
         */
        public final Process process;

        /**
         * The indicators in a map by UUID (input)
         */
        private final Map<String, IndicatorShell> indicatorsInByUuid = new HashMap<String, IndicatorShell>();

        /**
         * The indicators in a map by UUID (output)
         */
        private final Map<String, IndicatorShell> indicatorsOutByUuid = new HashMap<String, IndicatorShell>();

        /**
         * The indicators in a map by UUID (output)
         */
        private final Map<String, IndicatorShell> lciaResultIndicatorsByUuid = new HashMap<String, IndicatorShell>();

        /**
         * Create helper
         *
         * @param process                    process
         * @param indicatorsExInUuids        list of indicator UUIDs for input exchanges
         * @param indicatorsExOutUuids       list of indicator UUIDs for output exchanges
         * @param indicatorsLciaResultsUuids list of indicator UUIDs for LCIA results
         */
        private ProcessQAHelper(Process process, List<String> indicatorsExInUuids, List<String> indicatorsExOutUuids, IndicatorUuidList indicatorsLciaResultsUuids) {
            super();
            this.process = process;
            if (ProcessQAHandler.this.doExchangeCharts) {
                List<Exchange> indicators = process.getExchangesExcludingRefFlows();
                for (Exchange indicator : indicators) {
                    if (ExchangeDirectionValues.INPUT.equals(indicator.getExchangeDirection())) {
                        this.addIndicator(indicator, indicatorsExInUuids, this.indicatorsInByUuid);
                    } else if (ExchangeDirectionValues.OUTPUT.equals(indicator.getExchangeDirection())) {
                        this.addIndicator(indicator, indicatorsExOutUuids, this.indicatorsOutByUuid);
                    }
                }
            }
            if (ProcessQAHandler.this.doLciaResultCharts) {
                List<LciaResult> results = process.getLciaResults();
                for (LciaResult indicator : results) {
                    this.addIndicator(indicator, indicatorsLciaResultsUuids, this.lciaResultIndicatorsByUuid);
                }
            }
        }

        /**
         * Get the indicators by UUID map for the specified exchange direction
         *
         * @param direction direction
         * @return map
         */
        public Map<String, IndicatorShell> getIndicatorsByUuid(ExchangeDirectionValues direction) {
            // direction given => exchanges
            if (ExchangeDirectionValues.INPUT.equals(direction)) {
                return this.indicatorsInByUuid;
            }
            if (ExchangeDirectionValues.OUTPUT.equals(direction)) {
                return this.indicatorsOutByUuid;
            }

            // no direction => lcia results
            if (direction == null) {
                return this.lciaResultIndicatorsByUuid;
            }

            return null;
        }

        /**
         * Add indicator to list of UUIDs and maps
         *
         * @param indicator        the indicator
         * @param indicatorsUuids  list with UUIDs
         * @param indicatorsByUuid map with UUID as key
         */
        private void addIndicator(LciaResult indicator, List<String> indicatorsUuids, Map<String, IndicatorShell> indicatorsByUuid) {
            String uuid = indicator.getMethodReference().getUuid().getUuid();
            if (!indicatorsUuids.contains(uuid)) {
                indicatorsUuids.add(uuid);
            }
            indicatorsByUuid.put(uuid, new LciaResultIndicatorShell(indicator));
        }

        /**
         * Add indicator to list of UUIDs and maps
         *
         * @param indicator        the indicator
         * @param indicatorsUuids  list with UUIDs
         * @param indicatorsByUuid map with UUID as key
         */
        private void addIndicator(Exchange indicator, List<String> indicatorsUuids, Map<String, IndicatorShell> indicatorsByUuid) {
            String uuid = indicator.getFlow().getUuidAsString();
            if (!indicatorsUuids.contains(uuid)) {
                indicatorsUuids.add(uuid);
            }
            indicatorsByUuid.put(uuid, new ExchangeIndicatorShell(indicator));
        }

    }

    /**
     * List of UUIDs with direction property
     */
    public class IndicatorUuidList extends ArrayList<String> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = 393098443094984231L;

        /**
         * The exchange direction
         */
        private final ExchangeDirectionValues direction;

        /**
         * Create the list
         *
         * @param direction exchange direction
         */
        public IndicatorUuidList(ExchangeDirectionValues direction) {
            super();
            this.direction = direction;
        }

        /**
         * Get the exchange direction
         *
         * @return exchange direction
         */
        public ExchangeDirectionValues getDirection() {
            return this.direction;
        }
    }

    /**
     * Chart model with title
     */
    public class QAChartModel extends BarChartModel {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = -3073147764486727452L;

        /**
         * UUID of method
         */
        private String methodUuid;

        /**
         * UUID of reference flowproperty
         */
        private String referenceFlowpropertyUuid;

        /**
         * Direction
         */
        private ExchangeDirectionValues direction;

        /**
         * The unit
         */
        private String unit;

        /**
         * Count per series
         */
        private int countPerSeries;

        /**
         * Create the quality assurance model
         */
        public QAChartModel() {
            this.setExtender("chart_style_extender"); // Name of JS extender function
            this.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
            this.setLegendPosition("n");
            this.setZoom(true);
        }

        /**
         * Get the direction
         *
         * @return direction
         */
        public ExchangeDirectionValues getDirection() {
            return this.direction;
        }

        /**
         * Set the direction
         *
         * @param direction direction to set
         */
        public void setDirection(ExchangeDirectionValues direction) {
            this.direction = direction;
        }

        /**
         * Get the unit
         *
         * @return unit
         */
        public String getUnit() {
            return this.unit;
        }

        /**
         * Set the unit
         *
         * @param unit unit to set
         */
        public void setUnit(String unit) {
            this.unit = unit;
        }

        /**
         * Get the count per series
         *
         * @return count per series
         */
        public int getCountPerSeries() {
            return this.countPerSeries;
        }

        /**
         * Set the count per series
         *
         * @param countPerSeries count per series to set
         */
        public void setCountPerSeries(int countPerSeries) {
            this.countPerSeries = countPerSeries;
        }

        /**
         * Get the UUID of the unit group
         *
         * @return UUID of the unit group
         */
        public String getMethodUuid() {
            return this.methodUuid;
        }

        /**
         * Set the UUID of the unit group
         *
         * @param methodUuid UUID of the method to set
         */
        public void setMethodUuid(String methodUuid) {
            this.methodUuid = methodUuid;
        }

        public String getReferenceFlowpropertyUuid() {
            return this.referenceFlowpropertyUuid;
        }

        public void setReferenceFlowpropertyUuid(String referenceFlowpropertyUuid) {
            this.referenceFlowpropertyUuid = referenceFlowpropertyUuid;
        }

        /**
         * Get the series count
         *
         * @return series count
         */
        public int getSeriesCount() {
            // safety first, even if current model impl does not allow null value for series list
            return this.getSeries() != null ? this.getSeries().size() : 0;
        }

        /**
         * Update legend column count
         */
        public void updateLegendCols() {
            this.setLegendCols(this.getSeriesCount() > 2 ? 2 : 1);
        }

    }

    /**
     * Indicator shell for exchanges
     */
    public class ExchangeIndicatorShell implements IndicatorShell {

        /**
         * The exchange
         */
        private final Exchange exchange;

        /**
         * Create the shell
         *
         * @param exchange exchange
         */
        public ExchangeIndicatorShell(Exchange exchange) {
            super();
            this.exchange = exchange;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IMultiLangString getName() {
            return this.exchange.getFlowName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IMultiLangString getUnit() {
            return this.exchange.getUnitGroupReference().getShortDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMethodUuid() {
            return ((GlobalReference) this.exchange.getReference()).getUuid().getUuid();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Amount> getAmounts() {
            return this.exchange.getAmounts();
        }

    }

    /**
     * Indicator shell LCIA results
     */
    public class LciaResultIndicatorShell implements IndicatorShell {

        /**
         * The LCIA result
         */
        private final LciaResult lciaResult;

        /**
         * Create the shell
         *
         * @param lciaResult LCIA result
         */
        public LciaResultIndicatorShell(LciaResult lciaResult) {
            super();
            this.lciaResult = lciaResult;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IMultiLangString getName() {
            return this.lciaResult.getMethodReference().getShortDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IMultiLangString getUnit() {
            return this.lciaResult.getUnitGroupReference().getShortDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMethodUuid() {
            return this.lciaResult.getMethodReference().getUuid().getUuid();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Amount> getAmounts() {
            return this.lciaResult.getAmounts();
        }

    }

}
