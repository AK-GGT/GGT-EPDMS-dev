package de.iai.ilcd.service.task.abstractTask.runLogImplementations;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.util.result.DataSetResult;
import de.iai.ilcd.util.result.ResultType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of the default run log (grouping log lines by their state/log-level (error, warning, info, success)).
 * <p>
 * It allows adding DataSetResult objects or references together with a log-level. The references are stored instead
 * of being immediately mapped to string interpretation and appended. The benefit lies in lazy initialisation of
 * the log-string and one can extract references for further use (e.g. getting all error references).
 */
public class DataSetReferencingRunLog extends DefaultRunLog {

    public static final DuplicateStrategy FAVOR_FIRST = DuplicateStrategy.FAVOR_FIRST;
    public static final DuplicateStrategy FAVOR_LAST = DuplicateStrategy.FAVOR_LAST;
    private static final String DEPENDENCY_INDENT = "---------- DEP ---------- ";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    protected final EnumMap<LogLevel, ResultType> logLeveLInterpreter = new EnumMap<>(LogLevel.class);

    protected final EnumMap<ResultType, LogLevel> resultTypeToLogLevelMap = new EnumMap<>(ResultType.class);
    private final EnumMap<LogLevel, Set<DataSetPrintableReference>> references = new EnumMap<>(LogLevel.class);
    private DuplicateStrategy duplicateStrategy;
    private ResultType verbosenessOnSecondaryResults;

    public DataSetReferencingRunLog() {
        super();

        // Initialise LogLevel based fields
        for (LogLevel ll : LogLevel.values()) {
            references.put(ll, new HashSet<>());
        }

        duplicateStrategy = DuplicateStrategy.FAVOR_LAST;
        verbosenessOnSecondaryResults = ResultType.ERROR;

        determineLogLevelToResultTypeTranslations();
    }

    public DataSetReferencingRunLog(LogLevel... logLevelsToIgnore) {
        this();
        super.setIgnoreList(Arrays.asList(logLevelsToIgnore));
    }

    protected void determineLogLevelToResultTypeTranslations() {
        // How we interpret log levels
        logLeveLInterpreter.put(LogLevel.ERROR, ResultType.ERROR);
        logLeveLInterpreter.put(LogLevel.WARNING, ResultType.QUIET_ERROR);
        logLeveLInterpreter.put(LogLevel.SUCCESS, ResultType.SUCCESS);
        logLeveLInterpreter.put(LogLevel.INFO, ResultType.REDUNDANT);
        // else: null, no interpretation.

        // How we map result types to log levels
        resultTypeToLogLevelMap.put(ResultType.SUCCESS, LogLevel.SUCCESS);
        resultTypeToLogLevelMap.put(ResultType.ERROR, LogLevel.ERROR);
        resultTypeToLogLevelMap.put(ResultType.REDUNDANT, LogLevel.INFO);
        resultTypeToLogLevelMap.put(ResultType.PARTIALLY_SUCCESSFUL, LogLevel.WARNING);
        resultTypeToLogLevelMap.put(ResultType.QUIET_ERROR, LogLevel.WARNING);
    }

    /**
     * Stores a reference according to the given duplicate strategy.
     *
     * @param reference the reference
     * @param logLevel  the level to which the reference is filed
     * @return true if a reference was added or overridden.
     */
    private boolean storeReference(DataSetReference reference, LogLevel logLevel) {
        boolean addReference;
        DataSetPrintableReference printableRef = new DataSetPrintableReference(reference);

        // If duplicates are not allowed
        Set<DataSetPrintableReference> inboxPrior = null;
        boolean sameLevel = false;

        Set<DataSetPrintableReference> tmp;
        for (LogLevel ll : LogLevel.values()) {
            tmp = references.get(ll);
            if (tmp.contains(printableRef)) {
                inboxPrior = tmp;
                sameLevel = ll.equals(logLevel);
                break;
            }
        }

        addReference = (inboxPrior == null || !DuplicateStrategy.FAVOR_FIRST.equals(duplicateStrategy));
        if (addReference && !sameLevel) { // of course, we won't actually 'override' a reference -> && !sameLevel
            references.get(logLevel).add(printableRef);
            if (inboxPrior != null)
                inboxPrior.remove(printableRef);
        }

        return addReference;
    }

    private void logReference(DataSetReference dataSetReference, Collection<String> additionalLines, LogLevel logLevel) {
        storeReference(dataSetReference, logLevel);
    }

    public void log(DataSetResult dataSetResult, boolean verbose) {
        LogLevel logLevel = resultTypeToLogLevelMap.get(dataSetResult.getResultType());
        storeReference(dataSetResult.getDataSetReference(), logLevel);
    }

    public void log(DataSet dataSet, ResultType resultType) {
        log(new DataSetReference(dataSet), resultType);
    }

    public void log(DataSetReference dataSetReference, ResultType resultType) {
        LogLevel logLevel = resultTypeToLogLevelMap.get(resultType);
        storeReference(dataSetReference,
                logLevel);
    }

    public void log(DataSetResult dataSetResult) {
        log(dataSetResult, false);
    }

    public void error(DataSetReference dataSetReference) {
        logReference(dataSetReference, null, LogLevel.ERROR);
    }

    public void error(DataSet dataSet) {
        error(new DataSetReference(dataSet));
    }

    public void warning(DataSetReference dataSetReference) {
        logReference(dataSetReference, null, LogLevel.WARNING);
    }

    public void warning(DataSet dataSet) {
        success(new DataSetReference(dataSet));
    }

    public void success(DataSetReference dataSetReference) {
        logReference(dataSetReference, null, LogLevel.SUCCESS);
    }

    public void success(DataSet dataSet) {
        success(new DataSetReference(dataSet));
    }

    public void info(DataSetReference dataSetReference) {
        logReference(dataSetReference, null, LogLevel.INFO);
    }

    public void info(DataSet dataSet) {
        info(new DataSetReference(dataSet));
    }

    public Set<DataSetReference> getReferences(LogLevel logLevel) {
        return references.get(logLevel).stream()
                .map(DataSetPrintableReference::getReference)
                .collect(Collectors.toSet());
    }

    public Set<DataSetPrintableReference> getUnprintedReferences(LogLevel logLevel) {
        return references.get(logLevel).stream()
                .filter(DataSetPrintableReference::isUnprinted)
                .collect(Collectors.toSet());
    }

    public void setDuplicateStrategy(DuplicateStrategy duplicateStrategy) {
        this.duplicateStrategy = duplicateStrategy;
    }

    public void setVerbosenessOnSecondaryResults(ResultType resultTypeMostSevere) {
        this.verbosenessOnSecondaryResults = resultTypeMostSevere;
    }

    @Override
    public void error(String logLine) {
        append("## " + LogLevel.ERROR.name() + ": " + logLine);
    }

    @Override
    public void warning(String logLine) {
        append("# " + LogLevel.WARNING.name() + ": " + logLine);
    }

    @Override
    public void success(String logLine) {
        append(LogLevel.SUCCESS.name() + ": " + logLine);
    }

    @Override
    public void info(String logLine) {
        append(LogLevel.INFO.name() + ": " + logLine);
    }

    @Override
    public Long getErrorsCount() {
        return (long) references.get(LogLevel.ERROR).size();
    }

    @Override
    public Long getWarningsCount() {
        return (long) references.get(LogLevel.WARNING).size();
    }

    @Override
    public Long getInfosCount() {
        return (long) references.get(LogLevel.INFO).size();
    }

    @Override
    public Long getSuccessesCount() {
        return (long) references.get(LogLevel.SUCCESS).size();
    }

    @Override
    public String getLogString() {
        for (LogLevel ll : LogLevel.values()) {
            if (!super.ignoreList.contains(ll))
                for (DataSetPrintableReference pr : getUnprintedReferences(ll)) {
                    super.append(toString(pr.getReference(), ll), ll);
                    pr.setPrinted(true);
                }
        }

        return super.getLogString();
    }

    ///////////////////
    // PRIVATE UTILS //
    ///////////////////

    private String toString(DataSetResult dataSetResult, boolean verbose) {
        DataSetReference ref = dataSetResult.getDataSetReference();
        LogLevel logLevel = resultTypeToLogLevelMap.get(dataSetResult.getResultType());
        List<String> additionalInfo = toStringList(dataSetResult.getDependencyResults());

        return toString(ref, logLevel) +
                formatAdditionalLines(additionalInfo);
    }

    private String toString(DataSetReference dataSetReference, LogLevel logLevel) {
        ResultType logLevelInterpretation = logLeveLInterpreter.get(logLevel);
        if (logLevelInterpretation != null)
            return toStringPrefix(logLevelInterpretation) +
                    dataSetReference.toLogString() +
                    toStringSuffix(logLevelInterpretation);

        // else: standard implementation should be fine
        return dataSetReference.toLogString();
    }

    private String toString(DataSetReference dataSetReference, ResultType resultType) {
        return toStringPrefix(resultType) +
                dataSetReference.toLogString() +
                toStringSuffix(resultType);
    }

    private String toStringPrefix(ResultType resultType) {
        return resultType.name() +
                ": Operation on ";
    }

    private String toStringSuffix(ResultType resultType) {
        return " " +
                resultType.getValue() +
                ".";
    }

    /**
     * From each result we interpret the underlying data set together with the result type,
     * and collect these into a List.<br/>
     * Note: the results are filtered according to the verboseness on secondary results, which can be
     * set for the whole log instance. Default is <code>ResultType.ERROR</code>
     *
     * @param additionalResults List of secondary results
     * @return List of string interpretations of secondary results
     */
    private List<String> toStringList(List<DataSetResult> additionalResults) {
        // Here of additional references, e.g. dependency results, we only consider the 'first order'
        // results, i.e. dataSetResult.getDataSet() as opposed to dataSetResult.getDependencyResults.
        List<String> stringList = new ArrayList<>();
        Map<ResultType, List<DataSetResult>> groupedAdditionalResults =
                additionalResults.stream()
                        .filter(res -> verbosenessOnSecondaryResults.getSeverity() <= res.getResultType().getSeverity())
                        .collect(Collectors.groupingBy(DataSetResult::getResultType));
        for (Map.Entry<ResultType, List<DataSetResult>> e : groupedAdditionalResults.entrySet())
            stringList.addAll(e.getValue().stream()
                    .map(ref -> toString(ref.getDataSetReference(), e.getKey())).collect(Collectors.toSet()));

        return stringList;
    }

    private String formatAdditionalLines(Collection<String> additionalLines) {

        // Handling: no significant data
        if (additionalLines == null || additionalLines.size() == 0)
            return "";
        Set<String> cleanedSet = additionalLines.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toSet());
        if (cleanedSet.isEmpty())
            return "";

        // If there is data
        StringBuilder sb = new StringBuilder();
        for (String s : cleanedSet)
            sb.append(LINE_SEPARATOR)
                    .append(DEPENDENCY_INDENT)
                    .append(s);

        return sb.toString();
    }

    private enum DuplicateStrategy {
        FAVOR_FIRST,
        FAVOR_LAST
    }

    private class DataSetPrintableReference {

        private DataSetReference reference;

        private boolean isPrinted = false;

        public DataSetPrintableReference(DataSetReference reference) {
            this.reference = reference;
        }

        public DataSetReference getReference() {
            return reference;
        }

        public boolean isPrinted() {
            return isPrinted;
        }

        public void setPrinted(boolean printed) {
            isPrinted = printed;
        }

        public boolean isUnprinted() {
            return !isPrinted();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataSetPrintableReference that = (DataSetPrintableReference) o;
            return Objects.equals(reference, that.reference);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reference);
        }
    }

}
