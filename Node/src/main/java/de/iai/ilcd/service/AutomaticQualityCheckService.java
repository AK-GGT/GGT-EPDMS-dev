package de.iai.ilcd.service;

import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.common.QualityCheckEvent;
import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.dao.LciaResultClClassStatsDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.*;
import de.iai.ilcd.service.exception.NoCategoryException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service class for the quantitative quality assurance.
 *
 * @author sarai
 */
public class AutomaticQualityCheckService {

    private static Logger logger = LogManager.getLogger(AutomaticQualityCheckService.class);

    List<ClClass> categories;

    /**
     * Calculates the deviation from the given process amount to the given maximum amount in stats.
     *
     * @param processAmount The amount of given process whose deviation has to be calculated.
     * @param maxOfStats    The maximum amount given in stats
     * @return The deviation from process amount to maximum amount given in stats
     */
    public static Double calculateMaxDeviation(Double processAmount, Double maxOfStats) {
        if (maxOfStats == null)
            return null;
        double maxDeviation;
        maxDeviation = (processAmount - maxOfStats) * 100.0 / (Math.abs(maxOfStats));
        return maxDeviation;
    }

    /**
     * Calculates the deviation from given process amount to given minimum amount in stats.
     *
     * @param processAmount the amount of process whose deviation has to be calculated
     * @param minOfStats    The minimum amount given in stats
     * @return The deviation from process amount to minimum amount given in stats
     */
    public static Double calculateMinDeviation(Double processAmount, Double minOfStats) {
        if (minOfStats == null)
            return null;
        double minDeviation;
        minDeviation = (minOfStats - processAmount) * 100.0 / (Math.abs(minOfStats));
        return minDeviation;
    }

    /**
     * Checks the process's LCIA result against the whole category of selected stocks.
     * A deviation of a process's LCIA result from category is only be noted if the deviation is greater than the given threshold.
     *
     * @param process   The process to check
     * @param stocks    The reference stock to check against
     * @param threshold The deviation threshold
     * @return A list of notes containing information about the affected process, its LCIA result that deviates more than threshold from reference category
     */
    public List<QualityCheckEvent> checkProcess(Process process, List<DataStock> stocks, Double threshold, boolean showAll) throws NoCategoryException {
        List<QualityCheckEvent> eventList = new ArrayList<QualityCheckEvent>();
        ClClass category = null;
        List<ClClass> clClasses = process.getClassification().getClasses();
        category = clClasses.get(clClasses.size() - 1);
        if (category == null || category.getClId() == null) {
            throw new NoCategoryException();
        }
        List<Long> positions = new ArrayList<Long>();
        for (DataStock stock : stocks) {
            positions.add(stock.getId());
        }
        positions.sort(null);

        List<LciaResult> lciaResults = process.getLciaResults();
        List<Exchange> referenceExchanges = process.getReferenceExchanges();

        eventList = doProcessCheck(referenceExchanges, lciaResults, positions, category, process, threshold, showAll);

        return eventList;
    }

    /**
     * Creates a new event that will be shown in eventTable.
     *
     * @param lciaResult      The indicator of the process
     * @param reference       The reference value to measure with
     * @param process         The process for which an event has to be created
     * @param lciaResultStats The statistics of LCIA results that the process is compared against with
     * @param category        The category of process
     * @param deviation       Teh deviation of process to extremum of LCIA result stats
     * @param message         The message that will be displayed in table
     * @param amount          The amount of process
     * @return An event containing all relevant information to be correctly shown in event table
     */
    private QualityCheckEvent createEvent(LciaResult lciaResult, String reference, Process process, LciaResultClClassStats lciaResultStats, ClClass category,
                                          Double deviation, String message, Amount amount) {
        FlowPropertyDao flowPropDao = new FlowPropertyDao();
        FlowProperty flowProp = flowPropDao.getByUuid(lciaResultStats.getReferenceFlowpropertyUuid());
        QualityCheckEvent event = new QualityCheckEvent();
        event.setIndicatorName(lciaResult.getMethodReference().getShortDescription().getDefaultValue());
        event.setIndicatorUuid(lciaResult.getMethodReference().getUuid().getUuid());
        event.setReferenceValue(reference);
        event.setModule(amount.getModule());
        event.setProcessName(process.getName().getDefaultValue());
        event.setProcessUuid(process.getUuid().getUuid());
        event.setVersion(process.getDataSetVersion());
        event.setReferenceUnit("1.0 " + lciaResultStats.getRefUnit() + " " + flowProp.getName().getDefaultValue());
        event.setCategory(category.getClId() + " " + process.getClassification().getClassHierarchyAsString());
        event.setMessage(message);
        event.setAbsValue(lciaResult.getAmounts().get(0).getValue());
        event.setMinValue(Double.valueOf(lciaResultStats.getMin()));
        event.setMaxValue(Double.valueOf(lciaResultStats.getMax()));
        event.setAvgValue(Double.valueOf(lciaResultStats.getMean()));
        event.setDeviation(Math.round(deviation));
        event.setReferenceCount(lciaResultStats.getReferenceCount().toString());
        return event;
    }

    /**
     * Extracts the acronym of a given indicator name.
     *
     * @param indicatorName The name of indicator whose acronym has to be extracted from
     * @return The acronym of indicator name
     */
    public String getAcronym(String indicatorName) {
        if (indicatorName.contains("(")) {
            String[] descriptions = indicatorName.split("[(]");
            if (descriptions.length >= 2) {
                String acronym = descriptions[1];
                acronym = acronym.substring(0, acronym.length() - 1);
                return acronym;
            }
        }
        return indicatorName;
    }

    /**
     * Creates a list of events that occurred when checking each amount of process against reference statistics.
     *
     * @param referenceExchanges The reference flows used to compute reference value of each event
     * @param lciaResults        The lciaResults to compare to statistics
     * @param positions          The positions of stocks in stock list/ the stock IDs
     * @param category           The category to compare against
     * @param process            The process that is to checked
     * @param threshold          The threshold for deviation
     * @param showAll            A flag indicating whether all comparisons should be included into evenet list
     * @return A list of events that have either a higher deviation than threshold or all events (depends on showAll flag)
     */
    private List<QualityCheckEvent> doProcessCheck(List<Exchange> referenceExchanges, List<LciaResult> lciaResults,
                                                   List<Long> positions, ClClass category, Process process, Double threshold, boolean showAll) {
        List<QualityCheckEvent> eventList = new ArrayList<QualityCheckEvent>();

        for (Exchange referenceFlow : referenceExchanges) {
            String referenceFlowPropertyUuid = referenceFlow.getFlowWithSoftReference().getReferenceFlowProperty()
                    .getFlowPropertyRef().getUuid().getUuid();
            StringBuilder reference = new StringBuilder();
            reference.append(referenceFlow.getMeanValue()).append(" ").append(referenceFlow.getUnit()).append(" ")
                    .append(referenceFlow.getFlowPropertyName().getDefaultValue());
            if (logger.isDebugEnabled()) {
                logger.debug("going over all references; reference unit: " + reference.toString());
            }

            goOverAllLciaResults(eventList, lciaResults, process, category, positions,
                    referenceFlowPropertyUuid, reference.toString(), threshold, showAll);
        }
        return eventList;
    }

    /**
     * Goes over all LCIA results and add all events that to given event list.
     *
     * @param eventList                 The event list to be extended
     * @param lciaResults               The list of LCIA results to go over
     * @param process                   the process that has to be checked
     * @param category                  The category of process
     * @param positions                 The positions/ IDs of the reference data stocks in database
     * @param referenceFlowPropertyUuid The UUID of the reference flow property
     * @param reference                 The name of the reference
     * @param threshold                 The threshold to check against
     * @param showAll                   A flag indicating whether all generated events shall be added to list
     * @return A list containing both old and newly generated events
     */
    private void goOverAllLciaResults(List<QualityCheckEvent> eventList,
                                      List<LciaResult> lciaResults, Process process, ClClass category, List<Long> positions,
                                      String referenceFlowPropertyUuid, String reference, Double threshold, Boolean showAll) {

        for (LciaResult lciaResult : lciaResults) {
            logger.debug("going over all LCIA results of process.");
            String methodUuid = lciaResult.getMethodReference().getUuid().getUuid();
            if (logger.isDebugEnabled()) {
                logger.debug("UUID of LCIA Method corresponding to current LCIA result: " + methodUuid);
            }
            goOverAllAmounts(eventList, lciaResult, process, category, positions, methodUuid,
                    referenceFlowPropertyUuid, reference, threshold, showAll);
        }
    }

    /**
     * Goes over all amounts an adds all events that occurred in each amount to given event list.
     *
     * @param eventList                 The event list to be extended
     * @param lciaResult                The LCIA result corresponding to process
     * @param process                   The process that has to be checked
     * @param category                  The category of corresponding process
     * @param positions                 The positions/IDs of the reference data stocks in database
     * @param methodUuid                The UUID of given LCIA result
     * @param referenceFlowPropertyUuid The UUID of the reference flow property corresponding to process
     * @param reference                 The name of reference
     * @param threshold                 The threshold to check against
     * @param showAll                   A flag indicating whether all generated events shall be added to list
     * @return A list containing both old and newly generated events
     */
    private void goOverAllAmounts(List<QualityCheckEvent> eventList, LciaResult lciaResult,
                                  Process process, ClClass category, List<Long> positions, String methodUuid,
                                  String referenceFlowPropertyUuid, String reference, Double threshold, Boolean showAll) {

        for (Amount amount : lciaResult.getAmounts()) {
            logger.debug("Going over all amounts of current LCIA result");
            String module = amount.getModule();
            if (logger.isDebugEnabled()) {
                logger.debug("Module of current amount: " + module);
            }
            LciaResultClClassStatsDao dao = new LciaResultClClassStatsDao();
            LciaResultClClassStats lciaResultStats = dao.get(category.getClId(), methodUuid, module, positions,
                    referenceFlowPropertyUuid);

            if (logger.isDebugEnabled() && lciaResultStats != null) {
                logger.debug("process: " + process.getName().getDefaultValue() + ", amount: " + amount.getModule());
                logger.debug("LCIA Result name: " + lciaResult.getMethodReference().getShortDescription().getValue());
                logger.debug("min: " + lciaResultStats.getMin() + ", max: " + lciaResultStats.getMax() + " Avg: "
                        + lciaResultStats.getMean());
                logger.debug("Process stat: " + amount.getValue());
            }

            Double processAmount = lciaResult.getAmounts().get(0).getValue();
            Double maxOfStats = (lciaResultStats != null ? lciaResultStats.getMax() : null);
            Double minOfStats = (lciaResultStats != null ? lciaResultStats.getMin() : null);
            Double maxDeviation = calculateMaxDeviation(processAmount, maxOfStats);
            Double minDeviation = calculateMinDeviation(processAmount, minOfStats);

            logger.debug("calculating deviation from max of stats and from min of stats");
            checkAgainstMaxOfStats(eventList, lciaResult, process, lciaResultStats, category, threshold,
                    reference.toString(), amount, maxOfStats, maxDeviation);

            checkAgainstMinOfStats(eventList, lciaResult, process, lciaResultStats, category, threshold,
                    reference.toString(), amount, minOfStats, minDeviation);

            checkWhetherToShowAllEvents(eventList, lciaResult, lciaResultStats, showAll, process, minDeviation,
                    maxDeviation, threshold, reference.toString(), category, amount);

        }
    }

    /**
     * Checks Whether an event has to be generated due to the fact that the given amount is under the maximum of stats plus threshold.
     *
     * @param eventList       The list to extend
     * @param lciaResult      The LCIA result corresponding to process
     * @param process         The process that has to be checked
     * @param lciaResultStats The LCIA result corresponding to process
     * @param category        The category of corresponding process
     * @param threshold       The threshold to check against
     * @param reference       The threshold to check against
     * @param amount          The current amount of process that is to be checked against min of stats
     * @param maxOfStats      The maximum amount that is recorded in stats
     * @param maxDeviation    The deviation of current amount to maximum amount recorded in stats
     * @return A list containing both old and newly generated events
     */
    private void checkAgainstMaxOfStats(List<QualityCheckEvent> eventList, LciaResult lciaResult,
                                        Process process, LciaResultClClassStats lciaResultStats, ClClass category, Double threshold,
                                        String reference, Amount amount, Double maxOfStats, Double maxDeviation) {

        if (maxOfStats != null) {
            if (maxDeviation > threshold) {
                logger.debug("Indicator amount is deviating more from max than allowed.");
                String message = "+" + Math.round(maxDeviation) + "% (from max)";
                QualityCheckEvent event = createEvent(lciaResult, reference, process, lciaResultStats, category,
                        maxDeviation, message, amount);
                eventList.add(event);
            }
        }
    }

    /**
     * Checks Whether an event has to be generated due to the fact that the given amount is under the minimum of stats plus threshold.
     *
     * @param eventList       The list to extend
     * @param lciaResult      The LCIA result corresponding to process
     * @param process         The process that has to be checked
     * @param lciaResultStats The statistics for given LCIA result, category and reference data stocks
     * @param category        The category of corresponding process
     * @param threshold       The threshold to check against
     * @param reference       The name of reference
     * @param amount          The current amount of process that is to be checked against min of stats
     * @param minOfStats      The minimum amount that is recorded in stats
     * @param minDeviation    The deviation of current amount to minimum amount recorded in stats
     * @return A list containing both old and newly generated events
     */
    private void checkAgainstMinOfStats(List<QualityCheckEvent> eventList, LciaResult lciaResult,
                                        Process process, LciaResultClClassStats lciaResultStats, ClClass category, Double threshold,
                                        String reference, Amount amount, Double minOfStats, Double minDeviation) {

        if (minOfStats != null) {
            if (minDeviation > threshold) {
                logger.debug("Indicator amount is deviating more from min than allowed.");
                String message = "-" + Math.round(minDeviation) + "% (from min)";
                QualityCheckEvent event = createEvent(lciaResult, reference.toString(), process, lciaResultStats, category, minDeviation, message, amount);
                eventList.add(event);
            }
        }
    }

    /**
     * Checks Whether an event has to be generated due to the fact that all events shall be shown in event list.
     *
     * @param eventList       The list to extend
     * @param lciaResult      The LCIA result corresponding to process
     * @param lciaResultStats
     * @param showAll         A flag indicating whether all generated events shall be added to list
     * @param process         The process that has to be checked
     * @param minDeviation    The deviation of current amount to minimum amount recorded in stats
     * @param maxDeviation    The deviation of current amount to maximum amount recorded in stats
     * @param threshold       The threshold to check against
     * @param reference       The name of reference
     * @param category        The category of corresponding process
     * @param amount          The current amount of process that is to be checked against min of stats
     * @return A list containing both old and newly generated events
     */
    private void checkWhetherToShowAllEvents(List<QualityCheckEvent> eventList,
                                             LciaResult lciaResult, LciaResultClClassStats lciaResultStats, Boolean showAll, Process process,
                                             Double minDeviation, Double maxDeviation, Double threshold, String reference, ClClass category,
                                             Amount amount) {

        if (showAll && (minDeviation == null || (minDeviation <= threshold))
                && (maxDeviation == null || (maxDeviation <= threshold))) {
            QualityCheckEvent event = createEvent(lciaResult, reference, process, lciaResultStats, category,
                    (minDeviation != null ? minDeviation : null), "", amount);
            eventList.add(event);
        }

    }

    /**
     * Gets all categories out of given events
     *
     * @param events The events whose categories are wanted
     * @return The categories that are part of the events
     */
    public List<String> getCategories(List<QualityCheckEvent> events) {
        if (events == null)
            return null;
        List<String> categories = new ArrayList<String>();

        for (QualityCheckEvent event : events) {
            String category = event.getCategory();
            if (!categories.contains(category))
                categories.add(category);
        }
        return categories;
    }

    /**
     * Gets all modules out of the events.
     *
     * @param events The events to extract the modules
     * @return The modules that are contained in the events
     */
    public List<String> getModules(List<QualityCheckEvent> events) {
        if (events == null)
            return null;
        List<String> modules = new ArrayList<String>();

        for (QualityCheckEvent event : events) {
            String module = event.getModule();
            if (!modules.contains(module))
                modules.add(module);
        }
        return modules;
    }

    /**
     * Extracts all indicators out of given events.
     *
     * @param events The events to extract the indicators
     * @return The indicators that are extracted out of events
     */
    public Map<String, String> getIndicators(List<QualityCheckEvent> events) {
        if (events == null)
            return null;
        Map<String, String> indicators = new HashMap<String, String>();

        for (QualityCheckEvent event : events) {
            String indicatorName = event.getIndicatorName();
            String indicatorUuid = event.getIndicatorUuid();
            if (logger.isDebugEnabled()) {
                logger.debug("indicator name: " + indicatorName + ", UUID: " + indicatorUuid);
            }
            if (!indicators.containsValue(indicatorUuid)) {
                indicators.put(indicatorName, indicatorUuid);
                logger.debug("indicator added to map");
            }
        }
        return indicators;
    }

    /**
     * Extracts the reference counts out of the given events.
     *
     * @param events The events whose reference counts will be extracted from
     * @return The reference counts that are extracted out of events
     */
    public List<String> getReferenceCounts(List<QualityCheckEvent> events) {
        if (events == null)
            return null;
        List<String> referenceCounts = new ArrayList<String>();

        for (QualityCheckEvent event : events) {
            String referenceCount = event.getReferenceCount();
            if (!referenceCounts.contains(referenceCount))
                referenceCounts.add(referenceCount);
        }
        return referenceCounts;
    }


}
