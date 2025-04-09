package de.iai.ilcd.service.task.abstractTask;

import de.iai.ilcd.model.common.JobMetaData;
import de.iai.ilcd.model.dao.JobMetaDataDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.PersistException;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.queue.TaskProcessingException;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.queue.TaskingHub;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.util.JobState;
import de.iai.ilcd.service.util.JobType;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Base class for tasks. Essentially, this class knows how to, given data and an implementation of the
 * method {@link #process(Object)}, generate a callable object that can be handed over to tasking hubs
 * for execution. It also includes several overrideable methods to customise processing and keep the
 * implementation of the task well-structured. Furthermore, the logic for job meta data tracking lies
 * in this class.
 *
 * @param <T> the data type (typically a custom type bundling several objects)
 */
public abstract class AbstractTask<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    private final T data;

    private final User signee;

    private final TaskProcessingStrategy processingStrategy;
    private final JobMetaDataDao jmdDao;
    private String jobMetaDataUuid = null; // Will be initialised when needed.

    public AbstractTask(T data, User signee) {
        this.signee = signee;
        this.data = data;
        jmdDao = new JobMetaDataDao();
        processingStrategy = determineProcessingStrategy(data);
    }

    /////////////////////////
    // Mandatory Overrides //
    /////////////////////////

    /**
     * The string generated in this method yields the jobDescription, if the task is tracked as a job. This method will
     * be called automatically if a job description is needed.
     *
     * @param data The description can be based on the provided data, e.g. size of a list.
     * @return the job description
     */
    protected abstract String getDescription(T data);

    /**
     * Determines processing strategy. This method will be called automatically when the task is processed.
     *
     * @param data Picking a strategy can be based on the provided data set, e.g. size of a list.
     * @return a suitable processing strategy
     */
    protected abstract TaskProcessingStrategy determineProcessingStrategy(T data);

    /**
     * The provided data is going to be processed with this method. This method will be called automatically when the
     * task is run.
     *
     * @param data the provided data
     * @return A wrapper for the TaskStatus (e.g. SCHEDULED) and potential results (if task is run immediately)
     * @throws TaskRunException exception thrown during task runtime.
     */
    protected abstract TaskResult process(T data) throws TaskRunException;

    ////////////////////////
    // Optional Overrides //
    ////////////////////////

    /**
     * This is the description being displayed until the task is running according to the processing strategy.
     * When overriding this to add detail be warned: This is called from the main-thread, after
     * {@link #preProcessImmediately(Object, User)}.
     *
     * @return description displayed until the proper description is generated.
     */
    protected String getInitialDescription(T data) {
        return "Waiting or being pre-processed...";
    }

    /**
     * The JobMetaData instance will call this method to determine the jobType.
     *
     * @param data the data the job is going to process
     * @return the job type that will be kept in the job meta data object
     */
    protected JobType determineJobType(T data) {
        return null;
    }

    /**
     * Asserts that the user has the necessary permissions, to perform/order the task. It is the first method that is
     * called (i.e. before the Task is processed, still in main thread). Override this method if (additional)
     * permission checks are needed or e.g. the procedure should fail faster than by the safety checks already in
     * place.<br/><br/>
     * <p> Example:<br/>
     * Say T is a list of data sets and the tasks exports all of them. It should fail <i>quickly</i>, if one of the
     * necessary permissions is missing:
     * <pre>
     * {@literal @}Override
     * protected void assertCan(T data) {
     *
     *         Collection&lt;IDataStockMetaData&gt; involvedStocks = new HashSet<>();
     *
     *         for (DataSet dataSet : dataSets) {
     *             involvedStocks.add(dataSet.getRootDataStock());
     *             involvedStocks.addAll(dataSet.getContainingDataStocks());
     *         }
     *         for (IDataStockMetaData stock : involvedStocks){
     *             SecurityUtil.assertCan(stock, ProtectionType.EXPORT);
     *         }
     *
     * }
     * </pre>
     * </p>
     *
     * @param data   permissions can be based on the provided data.
     * @param signee the user who signed off the task
     */
    protected void assertCanImmediately(T data, User signee) throws AuthorizationException {

    }

    /**
     * This method gets executed after the process method, even if a throwable has been thrown
     * E.g. publishing log data, so that the published log holds the latest state.<br/><br/>
     * <p>
     * Note: If this method is overwritten, oftentimes {@link #onInterrupt} needs to be overwritten,
     * too.
     *
     * @param data   the data-bundle
     * @param user   the signee
     * @param result the result
     * @return the result
     */
    protected TaskResult postProcessFinally(T data, User user, TaskResult result) {
        return result;
    }

    /**
     * This method gets executed on the main thread before the task is run/processed, whatever the processing strategy
     * is, (i.e. directly after authorization checks).<br/>
     * Warning: If this method runs resource-intensive code, UI may freeze.
     * As an alternative, consider using {@link #preProcess(Object, User)}.
     *
     * @param data   the task data
     * @param signee the user who ordered the task done
     */
    protected void preProcessImmediately(T data, User signee) {

    }

    /**
     * This method gets executed right before processing the task during run time of the task, i.e. in accordance
     * with the processing strategy.<br/>
     * Note: If the processing strategy is off main thread, resource-intensive code won't slow down UI, if executed
     * in this method. If, however, it must be run on the main thread nonetheless, consider using
     * {@link #preProcessImmediately(Object, User)}
     *
     * @param data   the task data
     * @param signee the user who ordered the task done
     */
    protected void preProcess(T data, User signee) {

    }

    /**
     * Override this method if you need to track custom properties in the job metadata entity. E.g. job type is not
     * added by default, hence if the type should be <code>JobType.PUSH</code> one could override this method to:
     * <pre><code>
     *    protected JobMetaData completeJmd(JobMetaData jmd) {
     *         jmd.setJobType(JobType.PUSH);
     *         return jmd;
     *     }
     * </code>
     * </pre>
     *
     * @param jmd The jmd used by the abstract task
     * @return The completed jmd
     */
    protected JobMetaData completeJmd(JobMetaData jmd) {
        return jmd;
    }

    /**
     * This method get's called after the {@link #isContinue()} method has learned that our thread
     * has actually recieved an interrupt.
     *
     * @param data the data-bundle
     */
    protected void onInterrupt(T data) {
        JobMetaData jmd = jmdDao.getJobMetaData(getJobMetaDataUuid());
        jmd.setJobState(JobState.CANCELED);
        mergeJobMetaData(jmd);
    }

    /////////////////////////////////
    // The basic task construction //
    /////////////////////////////////

    /**
     * Triggers processing and running the task.
     *
     * @param taskingHub the tasking hub instance
     * @return A wrapper for the TaskStatus (e.g. SCHEDULED) and potential results (if task is run immediately)
     * @throws TaskProcessingException exception thrown during processing / scheduling
     * @throws TaskRunException        exception thrown during task runtime.
     */
    public TaskResult runIn(TaskingHub taskingHub) throws Throwable {
        // permissions
        try {
            assertCanImmediately(data, signee);

        } catch (AuthorizationException ae) {
            LOGGER.warn("Unauthorised according to 'assertCanImmediately' implementation!", ae);
            return new TaskResult(TaskStatus.UNAUTHORISED, ae);

        } catch (Throwable t) {
            LOGGER.error("Error occured in implementation of 'assertCan' method!", t);
            return new TaskResult(TaskStatus.ERROR, t);

        }

        // preprocessing
        try {
            preProcessImmediately(data, signee);

        } catch (Throwable t) {
            LOGGER.error("Error occured in implementation of 'preProcessImmediately' method!", t);
            return new TaskResult(TaskStatus.ERROR, t);

        }

        // scheduling/running
        return processingStrategy.apply(taskingHub, generateCallable(processingStrategy.isTrackingJobMetaData()));
    }

    /**
     * A wrapper for the provided implementation of the process method, that handles job metadata if necessary.
     *
     * @param data                  the provided data
     * @param isTrackingJobMetaData flag determined by the processing strategy. E.g. could be true if the task is
     *                              scheduled and false if it is run immediately
     * @return A wrapper for the TaskStatus (e.g. SCHEDULED) and potential results (if task is run immediately)
     * @throws TaskRunException exception thrown during task runtime.
     */
    private TaskResult process(T data, boolean isTrackingJobMetaData) throws TaskRunException {
        TaskResult taskResult;

        if (isTrackingJobMetaData) {
            Instant startingInstant = Instant.now();

            try {
                preProcess(data, signee);
                updateTitle();
                taskResult = process(data);

            } catch (Throwable t) {
                // Log error meta data
                JobMetaData jmd = jmdDao.getJobMetaData(jobMetaDataUuid);
                jmd.setJobCompletionTime(Date.from(Instant.now()));
                jmd.setJobRunTime(Duration.between(startingInstant, jmd.getJobCompletionTime().toInstant()).toMillis());
                jmd.setJobState(JobState.fromTaskStatus(TaskStatus.ERROR));

                LOGGER.trace("Attempting to merge Error status.");
                mergeJobMetaData(jmd);

                throw t;

            }

            // log meta data
            JobMetaData jmd = jmdDao.getJobMetaData(jobMetaDataUuid);
            jmd.setJobCompletionTime(Date.from(Instant.now()));
            jmd.setJobRunTime(Duration.between(startingInstant, jmd.getJobCompletionTime().toInstant()).toMillis());
            jmd.setJobState(JobState.fromTaskStatus(taskResult != null ? taskResult.getStatus() : TaskStatus.COMPLETE));

            LOGGER.trace("Attempting to merge post process status.");
            mergeJobMetaData(jmd);
            LOGGER.trace("Successfully merge post process status.");

        } else {
            preProcess(data, signee);
            taskResult = process(data);

        }

        return taskResult;
    }

    /**
     * As generating the description may be time-consuming (e.g. counting all process data sets
     * in the db), we do this within the processing method. This way the code is executed
     * according to the processing strategy (e.g. asynchronously).
     */
    private void updateTitle() {
        LOGGER.trace("Updating job description.");
        JobMetaData jmd = jmdDao.getJobMetaData(getJobMetaDataUuid());
        jmd.setJobName(getDescription(data));
        mergeJobMetaData(jmd);
    }

    /**
     * Uses the provided implementation of the method '<code>process</code>'
     * to generate a <code>Callable</code> that
     * throws a <code>TaskRunException</code>.
     *
     * @return the customised callable.
     */
    private TaskCallable<? extends AbstractTask<T>> generateCallable(boolean isTrackingJobMetaData) {
        LOGGER.trace("Generating a callable for " + this.getClass().getName());
        LOGGER.trace("isTrackingJobMetaData? " + isTrackingJobMetaData + "!");

        if (isTrackingJobMetaData) {
            LOGGER.trace("Creating new JobMetaData entity.");
            this.jobMetaDataUuid = UUID.randomUUID().toString();
            JobMetaData jmd = new JobMetaData();
            jmd.setJobFireTime(Date.from(Instant.now()));
            jmd.setJobId(jobMetaDataUuid);
            jmd.setUser(signee);
            jmd.setJobName(getInitialDescription(data));
            jmd.setJobType(determineJobType(data));
            jmd.setJobState(JobState.fromTaskStatus(TaskStatus.PROCESSING));

            LOGGER.trace("Attempting to persist jmd.");
            persistJobMetaData(jmd);

        }

        Callable<TaskResult> callable;
        callable = () -> {
            TaskResult result;
            try {
                result = process(data, isTrackingJobMetaData);
            } catch (Throwable t) {
                result = new TaskResult(TaskStatus.ERROR);
                result.setThrowable(t);
                safePostProcessFinally(data, signee, result);
                throw t;
            }
            result = safePostProcessFinally(data, signee, result);
            return result;
        };

        return new TaskCallable<>(this.getClass(), getJobMetaDataUuid(), callable, isTrackingJobMetaData);
    }

    /**
     * Wrapper for the post process finally to make sure exceptions are handled gracefully.
     *
     * @param data   the data we started out with
     * @param signee the user who instantiated the job
     * @param result the task result
     * @return the task result (potentially now holding an error status and an exception)
     */
    private TaskResult safePostProcessFinally(T data, User signee, TaskResult result) {
        try {
            result = postProcessFinally(data, signee, result);
        } catch (Throwable t) {
            LOGGER.error("Error occured in implementation of 'postProcessFinally' method!", t);
            if (result != null) {
                result.setStatus(TaskStatus.ERROR);
                result.setThrowable(t);
            } else {
                result = new TaskResult(TaskStatus.ERROR);
                result.setThrowable(t);
            }
        }

        return result;
    }

    public String getJobMetaDataUuid() {
        return this.jobMetaDataUuid;
    }

    /**
     * Merges the provided job meta data with the one in the db
     *
     * @param jmd the job meta data with updated state
     */
    private void mergeJobMetaData(JobMetaData jmd) {
        jmd = completeJmd(jmd);

        try {
            jmdDao.merge(jmd);

        } catch (MergeException me1) {
            // try again a bit later
            try {
                Thread.sleep(1000);
                jmdDao.merge(jmd);
            } catch (InterruptedException ie) {
                // Ignore

            } catch (MergeException me2) {
                LOGGER.error("There has been a problem, persisting/merging JobMetaData for class "
                        + this.getClass().getName() + ". JobMetaData UUID=" + jmd.getJobId(), me2);

            }
        }
    }

    /**
     * Persists a brand-new job meta data object
     *
     * @param jmd job meta data object
     */
    private void persistJobMetaData(JobMetaData jmd) {
        JobMetaDataDao jmdDao = new JobMetaDataDao();

        jmd = completeJmd(jmd);
        try {
            jmdDao.persist(jmd);
            LOGGER.trace("JobMetaData (" + jmd.getJobId() + ") has successfully been persisted.");

            // Let's also try to return a managed entity
            try {
                jmdDao.getJobMetaData(jmd.getJobId());

            } catch (Exception e) {
                LOGGER.warn("Job meta data may not be up to date.", e);
            }

        } catch (PersistException e) {
            LOGGER.error("Could not persist job meta data!", e);
        }
    }

    /**
     * Getter for the user that instantiated the task.
     *
     * @return signee
     */
    protected User getSignee() {
        return signee;
    }

    /**
     * Check whether the thread has received an interrupt. If it has,
     * we execute {@link #onInterrupt} and throw a TaskRunException.
     *
     * @return true if the thread has not been interrupted.
     * @throws TaskRunException if the thread has been interrupted.
     */
    protected boolean isContinue() throws TaskRunException {
        if (Thread.currentThread().isInterrupted()) {
            LOGGER.info("Thread has been interrupted.");
            onInterrupt(data);
            throw new TaskRunException("Interrupted.");
        }

        // else
        return true;
    }

}
