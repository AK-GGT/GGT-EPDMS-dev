package de.iai.ilcd.service.task.abstractTask;

import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.model.dao.JobMetaDataDao;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.util.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Wrapper for suppliers that wraps all uncaught exceptions in a <code>TaskRunException</code>.
 * This behaves similiar to <code>java.util.concurrent.Callable</code> with the sole difference
 * that <code>java.util.concurrent.Callable</code> throws an 'unspecific'
 * <code>Exception</code>.
 *
 * @param <S> The task class
 */
public class TaskCallable<S extends AbstractTask<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskCallable.class);

    private final Callable<TaskResult> callable;

    private final String jobUuid;

    private final Class<?> taskClass;

    private final boolean isTrackingJobMetaData;

    TaskCallable(Class<?> taskClass, String jobUuid, Callable<TaskResult> callable, boolean isTrackingJobMetaData) {
        this.taskClass = taskClass;
        this.jobUuid = jobUuid;
        this.callable = callable;
        this.isTrackingJobMetaData = isTrackingJobMetaData;

        LOGGER.trace("New TaskCallable initialised: " + this);
    }

    public TaskResult call() throws TaskRunException {
        LOGGER.trace(this + " has been called.");
        persistTaskStatus(jobUuid, TaskStatus.RUNNING);

        // Run the task
        try {
            return callable.call();

        } catch (Exception e) {
            persistTaskStatus(jobUuid, TaskStatus.ERROR);

            if (e instanceof TaskRunException)
                throw (TaskRunException) e;
            else
                throw new TaskRunException(taskClass, e);
        }
    }

    public String getJobUuid() {
        return jobUuid;
    }

    private void persistTaskStatus(String jobUuid, TaskStatus taskStatus) {
        if (jobUuid != null) {
            JobMetaDataDao jmdDao = new JobMetaDataDao();

            LOGGER.trace("Task status to be persisted is " + taskStatus);
            JobState jobState = JobState.fromTaskStatus(taskStatus);
            try {
                JobMetaData jmd = jmdDao.getJobMetaData(jobUuid);
                jmd.setJobState(jobState);
                jmdDao.merge(jmd);

            } catch (Exception e) {
                LOGGER.error("Something went wrong while attempting to update the jobstate for "
                        + jobUuid + " to state " + jobState.name(), e);

                List<JobMetaData> jobMetaDataList = jmdDao.getAll();
                LOGGER.trace("JobMetaData entity count: " + jobMetaDataList.size());
                for (JobMetaData jmd : jobMetaDataList)
                    LOGGER.trace(jmd.getJobId());
            }
        }
    }

    public Class<?> getTaskClass() {
        return taskClass;
    }

    public boolean isTrackingJobMetaData() {
        return isTrackingJobMetaData;
    }

    @Override
    public String toString() {
        return "TaskCallable{"
                + "callable=" + callable
                + ", jobUuid='" + jobUuid + '\''
                + ", taskClass=" + taskClass.getName()
                + "} at " + super.toString();
    }
}
