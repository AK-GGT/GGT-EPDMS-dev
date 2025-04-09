package de.iai.ilcd.service.task;

import de.iai.ilcd.service.task.abstractTask.TaskRunLog;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The standardised result class returned by tasks when being run. It typically contains enough information
 * to generate a standardised faces message informing the Users of the result or state (e.g. scheduled). Therefore, it
 * includes a convenience method that, given an abstract handler, builds and sends the faces message.<br/><br/>
 * <p>
 * It also includes a run-log that can be printed into the console, a field to store an exception in and for the sake of
 * flexibility a general String-Object-Map where anything can be stored if need be. Typically, run-log, exception and
 * String-Object-Map are redundant when the task is scheduled.
 */
public class TaskResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskResult.class);

    private static final String STANDARD_MESSAGE_KEY_PREFIX = "facesMsg.taskResult.";

    private TaskStatus status;

    private Map<String, Object> dataMap = new HashMap<>();

    private Throwable throwable;

    private TaskRunLog runLog;

    public TaskResult() {
        super();
    }

    public TaskResult(TaskStatus status) {
        this.status = status;
    }

    public TaskResult(TaskStatus status, Throwable throwable) {
        this.status = status;
        this.throwable = throwable;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public TaskRunLog getRunLog() {
        return runLog;
    }

    public void setRunLog(TaskRunLog runLog) {
        this.runLog = runLog;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void putData(String key, Object value) {
        dataMap.put(key, value);
    }

    public void putAllData(Map<String, Object> dataMap) {
        this.dataMap.putAll(dataMap);
    }

    public List<?> getList(String key) {
        Object uncastList = dataMap.get(key);
        return uncastList == null ? new ArrayList<>() : (List<?>) uncastList;
    }

    /**
     * Adds a standardised message to the given handler.
     * If wished, an error-log will be printed to the console (if it exists).
     *
     * @param handler              the handler that accepts the message
     * @param printRunLogToConsole flag to decide whether the error log should appear in the console
     * @return self
     */
    public TaskResult message(AbstractHandler handler, boolean printRunLogToConsole) {
        // determine severity
        boolean isError = status.isError();
        FacesMessage.Severity severity = isError ?
                FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO;

        // add status based messge
        handler.addI18NFacesMessage(STANDARD_MESSAGE_KEY_PREFIX + status.getI18nKey(), severity);

        // add overview if success/error/info counts are available
        if (runLog != null && !runLog.isEmpty()) {
            handler.addI18NFacesMessage(STANDARD_MESSAGE_KEY_PREFIX + "overview", severity,
                    runLog.getSuccessesCount(), runLog.getInfosCount(), runLog.getErrorsCount());

            if (printRunLogToConsole) {
                String logAsString = runLog.getLogString();
                if (logAsString != null && !logAsString.trim().isEmpty())
                    LOGGER.info(runLog.getLogString());
            }

        }

        return this;

    }

    /**
     * Adds a standardised message to the given handler.
     * Prints error-log to console (if it exists).
     *
     * @param handler the handler that accepts the message
     * @return self
     */
    public TaskResult message(AbstractHandler handler) {
        return message(handler, true);
    }

}
