package de.iai.ilcd.service.task.glad;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.Uuid;
import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.abstractTask.TaskRunLog;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistrationDeregistrationRunLog implements TaskRunLog {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");
    private EnumMap<LogLevel, List<DataSetRegistrationResult>> logLevelMapper = new EnumMap<>(
            LogLevel.class);
    private EnumMap<DataSetRegistrationResult, List<Uuid>> preSortedLogs = new EnumMap<>(
            DataSetRegistrationResult.class);
    private EnumMap<LogLevel, String> unspecificLog = new EnumMap<>(LogLevel.class);
    private long errorsCount, warningsCount, infosCount, successesCount = 0;

    private String title;

    /**
     * Define which result gets mapped to which log level and initialises the
     * pre-sorted logs.
     */
    RegistrationDeregistrationRunLog() {
        List<DataSetRegistrationResult> unmappedResults = new LinkedList<>(
                Arrays.asList(DataSetRegistrationResult.values()));
        for (DataSetRegistrationResult key : unmappedResults) {
            preSortedLogs.put(key, new ArrayList<>());
        }

        // Errors
        List<DataSetRegistrationResult> codes = Stream.of(DataSetRegistrationResult.ERROR)
                .collect(Collectors.toList());
        logLevelMapper.put(LogLevel.ERROR, codes);
        unmappedResults.removeAll(codes);

        // Successes
        codes = Stream.of(DataSetRegistrationResult.ACCEPTED_PENDING).collect(Collectors.toList());
        logLevelMapper.put(LogLevel.SUCCESS, codes);
        unmappedResults.removeAll(codes);

        // Info
        logLevelMapper.put(LogLevel.INFO, unmappedResults);
    }

    void add(DataSet dataSet, DataSetRegistrationResult statusResult) {
        List<Uuid> entries = preSortedLogs.get(statusResult);
        entries.add(dataSet.getUuid());
        preSortedLogs.put(statusResult, entries);

        // TODO: wire to log level!
        if (statusResult.equals(DataSetRegistrationResult.ERROR))
            incrementErrorsCount();
        else if (statusResult.equals(DataSetRegistrationResult.ACCEPTED_PENDING))
            incrementSuccessesCount();
        else
            incrementInfosCount();
    }

    private String printLevelLog(LogLevel level) {
        StringBuilder sb = new StringBuilder();
        sb.append(addHeader(level));

        List<String> preLogs = logLevelMapper.get(level).stream().map(this::printPreSortedLog)
                .collect(Collectors.toList());
        for (String s : preLogs)
            sb.append(s);

        String unspecificLogString = unspecificLog.get(level);
        if (unspecificLogString != null && !unspecificLogString.trim().isEmpty()) {
            sb.append(sb.length() > 0 ? LINE_SEPARATOR : "");
            sb.append(unspecificLog.get(level));
        }

        sb.append(LINE_SEPARATOR);
        String levelLog = sb.toString();
        return StringUtils.equals(levelLog, addHeader(level) + LINE_SEPARATOR) ? "" : levelLog;
    }

    private String printPreSortedLog(DataSetRegistrationResult key) {
        StringBuilder sb = new StringBuilder();
        List<String> ids = preSortedLogs.get(key).stream().map(uuid -> key + ": " + uuid + LINE_SEPARATOR)
                .collect(Collectors.toList());
        for (String s : ids)
            sb.append(s);
        return sb.toString();
    }

    private String addHeader(LogLevel level) {
        return LINE_SEPARATOR +
                "---------" +
                level +
                "---------" +
                LINE_SEPARATOR;
    }

    @Override
    public Long getErrorsCount() {
        return errorsCount;
    }

    @Override
    public Long getWarningsCount() {
        return warningsCount;
    }

    @Override
    public Long getInfosCount() {
        return infosCount;
    }

    @Override
    public Long getSuccessesCount() {
        return successesCount;
    }

    public void incrementErrorsCount() {
        errorsCount++;
    }

    public void incrementInfosCount() {
        infosCount++;
    }

    public void incrementWarningsCount() {
        warningsCount++;
    }

    public void incrementSuccessesCount() {
        successesCount++;
    }

    @Override
    public void error(String logLine) {
        incrementSuccessesCount();
        append(logLine, LogLevel.ERROR);
    }

    @Override
    public void info(String logLine) {
        incrementSuccessesCount();
        append(logLine, LogLevel.INFO);
    }

    @Override
    public void warning(String logLine) {
        incrementWarningsCount();
        append(logLine, LogLevel.WARNING);
    }

    @Override
    public void success(String logLine) {
        incrementSuccessesCount();
        append(logLine, LogLevel.SUCCESS);
    }

    @Override
    public void prepend(String s) {
        append(s, LogLevel.PREFACE);
    }

    @Override
    public void setTitle(String s) {
        this.title = s;
    }

    private String getTitleHeader() {
        boolean titleInitialised = title != null && !title.trim().isEmpty();
        String initialisedTitle = titleInitialised ? title : "Log";
        return "### " + initialisedTitle + " ###" + LINE_SEPARATOR;
    }

    @Override
    public void append(String s) {
        append(s, LogLevel.INFO);
    }

    public void append(String logLine, LogLevel logLevel) {
        if (logLevel != null && logLine != null && !logLine.trim().isEmpty()) {
            String prior = unspecificLog.get(logLevel);
            prior = (prior == null) ? "" : prior; // convert null to empty string.
            unspecificLog.put(logLevel, prior + System.getProperty("line.separator") + logLine);
        }
    }

    public String getLogString() {
        StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
        sb.append(getTitleHeader());

        for (LogLevel key : logLevelMapper.keySet()) {
            sb.append(printLevelLog(key));
        }
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return errorsCount == 0 && infosCount == 0 && successesCount == 0;
    }
}
