package de.iai.ilcd.service.task.abstractTask.runLogImplementations;

/**
 * Extension of DefaultRunLog class, that adds the functionality to
 * increment counters without leaving a log line.<br/>
 * Typically, the log is being written/kept somewhere else and then assigned.
 */
public class LegacyRunLog extends DefaultRunLog {

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
        successCount++;
    }
}
