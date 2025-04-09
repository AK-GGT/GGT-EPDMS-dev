package de.iai.ilcd.service.task.abstractTask;

/**
 * The most basic methods a run log should always provide.
 */
public interface TaskRunLog {

    void error(String logLine);

    void warning(String logLine);

    void success(String logLine);

    void info(String logLine);

    void prepend(String s);

    void setTitle(String title);

    void append(String s);

    Long getErrorsCount();

    Long getWarningsCount();

    Long getInfosCount();

    Long getSuccessesCount();

    String getLogString();

    boolean isEmpty();

}
