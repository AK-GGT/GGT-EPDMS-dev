package de.iai.ilcd.service.task.abstractTask;


import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.model.dao.JobMetaDataDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.util.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of abstract task that includes an implementation of a TaskRunLog and provides easy access to key methods
 * of the run-log.
 *
 * @param <T> the data type (typically a custom type bundling several objects)
 * @param <S> the run log implementation
 */
public abstract class AbstractLoggingTask<T, S extends TaskRunLog> extends AbstractTask<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoggingTask.class);

    private final S runLog;

    public AbstractLoggingTask(T data, User signee) {
        super(data, signee);
        this.runLog = initTaskLog();
    }

    /////////////////////////
    // Mandatory Overrides //
    /////////////////////////

    /**
     * This is used to initialise the task log. Override this method to use a custom implementation.
     * It is called automatically when the Task is initialised.
     */
    protected abstract S initTaskLog();

    /////////////////////////////////////////
    // The basic logging task construction //
    /////////////////////////////////////////

    protected S getRunLog() {
        return runLog;
    }

    protected void publishLog() {
        publishLog(false);
    }

    protected void publishLog(boolean publishLogString) {
        if (this.getJobMetaDataUuid() != null) {
            String logString = publishLogString ? runLog.getLogString() : "Waiting for task to complete...";
            JobMetaDataDao jmdDao = new JobMetaDataDao();

            try {
                // log warnings are mapped to infos in jmd, log infos are counted as success in jmd
                jmdDao.mergeSuccessInfoErrorLogRunTime(this.getJobMetaDataUuid(), runLog.getErrorsCount(),
                        runLog.getWarningsCount(), runLog.getSuccessesCount() + runLog.getInfosCount(), logString);

            } catch (MergeException e) {
                // When the merge fails, our log is still fine, so we may let this go...
                e.printStackTrace();
            }
        }
    }

    protected void error(String logLine) {
        runLog.error(logLine);
        publishLog();
    }

    protected void info(String logLine) {
        runLog.info(logLine);
        publishLog();
    }

    protected void success(String logLine) {
        runLog.success(logLine);
        publishLog();
    }

    protected void warn(String logLine) {
        runLog.warning(logLine);
        publishLog();
    }

    protected void appendToLogAppendix(String logLine) {
        runLog.append(logLine);
        publishLog();
    }

    protected void appendToLogPreface(String logLine) {
        runLog.append(logLine);
        publishLog();
    }

    protected void setLogTitle(String logTitle) {
        runLog.setTitle(logTitle);
        publishLog();
    }

    @Override
    protected void preProcess(T data, User signee) {
        runLog.setTitle(getDescription(data));
    }

    @Override
    protected TaskResult postProcessFinally(T data, User user, TaskResult result) {
        publishLog(true);
        result.setRunLog(getRunLog());
        System.out.println(runLog.getLogString());
        return result;
    }

    @Override
    protected void onInterrupt(T data) {
        JobMetaDataDao jmdDao = new JobMetaDataDao();

        JobMetaData jmd = jmdDao.getJobMetaData(getJobMetaDataUuid());
        jmd.setJobState(JobState.CANCELED);
        appendToLogPreface("------- CANCELED -------");
        jmd.setLog(getRunLog().getLogString());

        try {
            jmdDao.merge(jmd);
        } catch (MergeException e) {
            LOGGER.error("Task has been interrupted. Setting the jobs state to 'canceled' has failed.", e);
        }
    }
}
