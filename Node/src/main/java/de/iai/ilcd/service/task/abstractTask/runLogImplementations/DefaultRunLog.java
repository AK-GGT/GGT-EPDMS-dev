package de.iai.ilcd.service.task.abstractTask.runLogImplementations;

import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.abstractTask.TaskRunLog;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rather simplistic implementation of TaskRunLog. For the convenience of the reader it groups errors, warnings,
 * infos and successes.
 */
public class DefaultRunLog implements TaskRunLog {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    protected long errorsCount, warningsCount, successCount, infosCount;
    protected Set<LogLevel> ignoreList = new HashSet<>();
    String title;
    private EnumMap<LogLevel, String> leveledLogStrings = new EnumMap<>(LogLevel.class);

    public DefaultRunLog() {
        super();
        successCount = 0;
        warningsCount = 0;
        infosCount = 0;
        errorsCount = 0;
    }

    public DefaultRunLog(LogLevel... logLevelsToIgnore) {
        this();
        setIgnoreList(Arrays.asList(logLevelsToIgnore));
    }

    @Override
    public void append(String s) {
        append(s, LogLevel.APPENDIX);
    }

    public void append(String logLine, LogLevel logLevel) {
        if (logLevel != null && logLine != null && !logLine.trim().isEmpty()) {
            String prior = leveledLogStrings.get(logLevel);
            prior = (prior == null) ? "" : prior; // convert null to empty string.
            leveledLogStrings.put(logLevel, prior + LINE_SEPARATOR + logLine);
        }
    }

    protected void appendAndCount(String logLine, LogLevel logLevel) {
        append(logLine, logLevel);

        if (LogLevel.ERROR.equals(logLevel))
            errorsCount += 1;
        else if (LogLevel.WARNING.equals(logLevel))
            warningsCount += 1;
        else if (LogLevel.INFO.equals(logLevel))
            infosCount += 1;
        else if (LogLevel.SUCCESS.equals(logLevel))
            successCount += 1;
    }

    @Override
    public void error(String logLine) {
        appendAndCount(logLine, LogLevel.ERROR);
    }

    @Override
    public void warning(String logLine) {
        appendAndCount(logLine, LogLevel.WARNING);
    }

    @Override
    public void success(String logLine) {
        appendAndCount(logLine, LogLevel.SUCCESS);
    }

    @Override
    public void info(String logLine) {
        appendAndCount(logLine, LogLevel.INFO);
    }

    @Override
    public void prepend(String s) {
        append(s, LogLevel.PREFACE);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLogString() {
        return buildLogString(this.leveledLogStrings);
    }

    @Override
    public boolean isEmpty() {
        return (getLogString().equals(buildLogString(null)) || getLogString().trim().isEmpty())
                && getErrorsCount() == 0
                && getWarningsCount() == 0
                && getInfosCount() == 0
                && getSuccessesCount() == 0;
    }

    private String buildLogString(EnumMap<LogLevel, String> leveledLogStrings) {
        leveledLogStrings = (leveledLogStrings != null) ? leveledLogStrings : new EnumMap<>(LogLevel.class);
        StringBuilder sb = new StringBuilder();

        sb.append("----------------------------------------------------------------------------")
                .append(LINE_SEPARATOR)
                .append(getTitleHeader());

        // ignore log levels that are in the ignore list
        Set<LogLevel> printableLogLevels = Arrays.stream(LogLevel.values())
                .filter(ll -> !ignoreList.contains(ll))
                .collect(Collectors.toSet());
        for (LogLevel logLevel : printableLogLevels) {
            String levelLogString = leveledLogStrings.get(logLevel);
            boolean isEmpty = levelLogString == null || levelLogString.trim().isEmpty();

            if ((logLevel.equals(LogLevel.PREFACE) || logLevel.equals(LogLevel.APPENDIX)) && isEmpty)
                continue;

            // for the standard levels we are explicit about having no entries
            sb.append(createLevelHeader(logLevel));
            if (isEmpty)
                sb.append("None...");
            else
                sb.append(levelLogString);

            sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        sb.append("----------------------------------------------------------------------------");
        return sb.toString();
    }

    private String createLevelHeader(LogLevel level) {
        return LINE_SEPARATOR +
                "---------" +
                level +
                "---------" +
                LINE_SEPARATOR;
    }

    private String getTitleHeader() {
        boolean titleInitialised = title != null && !title.trim().isEmpty();
        String initialisedTitle = titleInitialised ? title : "Log";
        return "### " + initialisedTitle + " ###" + LINE_SEPARATOR;
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
        return successCount;
    }

    public void setIgnoreList(Collection<LogLevel> logLevelsToIgnore) {
        this.ignoreList = new HashSet<>(logLevelsToIgnore);
    }
}
