package de.iai.ilcd.service.queue;

import de.iai.ilcd.model.dao.JobMetaDataDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.TaskCallable;
import de.iai.ilcd.service.util.JobState;

import java.util.function.Consumer;

/**
 * Enum resembling the processing strategies a task can call for.<br/>
 * <br/>
 * <p>
 * <p>
 * Each value knows/defines whether job metadata should be tracked (i.e. initialised, persisted in the db
 * and frequently updated, which is typically not desirable for immediate execution, where results can be
 * immediately accessed and processed).<br/>
 * <p>
 * Furthermore, each value knows/defines an exception strategy (typically, again, immediate execution
 * allows dealing with exceptions, so for them to be thrown may be preferable there).<br/>
 * <p>
 * The essential part, however, is the Functional that defines how to plug a task into
 * a tasking hub -- the actual processing strategy. <i>Note: </i> As a functional having a tasking hub as parameter
 * this is completely independent of implementation details, it merely holds (together with the {@link #apply} method)
 * the proper protocol for the hypothetical: 'Given a tasking hub and a task, how would we obtain a result.'<br/>
 * <br/><br/>
 *
 *
 * <i> How is the processing strategy (i.e. </i>'plugging a task into a hub'<i>) modeled as a bi-function? </i><br/>
 * <br/>
 * Given a <b><i>task</i></b> and a <b><i>hub</i></b> ├─── [*] ───► obtain a <b><i>result</i></b><br/>
 * [*] = [processing strategy] <br/>
 * = [get a method from the hub that accepts a task and call it on the task]
 */
public enum TaskProcessingStrategy {

    IMMEDIATELY(false, ExceptionStrategy.THROW_CAUSE, TaskingHub::runImmediately),

    BACKGROUND_THREAD(true, ExceptionStrategy.CATCH, (hub, callable) -> schedule(hub::runAsynchronously, callable)),

    BACKGROUND_QUEUE(true, ExceptionStrategy.CATCH, (hub, callable) -> schedule(hub::runInQueue, callable));

    private final boolean trackingJobMetaData;

    private final ExceptionStrategy exceptionStrategy;

    private final ThrowingBiFunction<TaskingHub, TaskCallable<?>, TaskResult> processingStrategy;

    TaskProcessingStrategy(boolean trackingJobMetaData, ExceptionStrategy exceptionStrategy,
                           ThrowingBiFunction<TaskingHub, TaskCallable<?>, TaskResult> processingStrategy) {
        this.trackingJobMetaData = trackingJobMetaData;
        this.exceptionStrategy = exceptionStrategy;
        this.processingStrategy = processingStrategy;
    }

    /**
     * If the task is tracking job meta data, we set the state to 'scheduled' and we hand the callable over
     * to the consumer (who may refrain from running the job at once).
     *
     * @param consumer the functional that runs the callable
     * @param callable the callable to be called
     * @return the task result
     */
    private static TaskResult schedule(Consumer<TaskCallable<?>> consumer, TaskCallable<?> callable) {
        TaskStatus taskStatus = TaskStatus.SCHEDULED;

        if (callable.isTrackingJobMetaData()) {
            JobMetaDataDao jmdDao = new JobMetaDataDao();
            try {
                jmdDao.mergeJobState(callable.getJobUuid(), JobState.fromTaskStatus(taskStatus));
            } catch (MergeException e) {
                e.printStackTrace();
            }
        }

        consumer.accept(callable);
        return new TaskResult(taskStatus);
    }

    /**
     * We transform the processing strategy functional by applying the exception appropriate exception strategy
     * and then apply the resulting functional.
     *
     * @param taskingHub the tasking hub instance in which the callable should be run.
     * @param callable   the task callable
     * @return the task result
     * @throws Throwable depending on the exception strategy, exceptions may be thrown
     */
    public TaskResult apply(TaskingHub taskingHub, TaskCallable<?> callable) throws Throwable {

        try {
            return applyExceptionStrategy(processingStrategy, callable).applyThrowing(taskingHub, callable);

        } catch (Exception e) {
            if (isTrackingJobMetaData())
                persistErrorStatus(callable);

            throw e;
        }

    }

    /**
     * We wrap calling the functional on the callable into a try catch.
     *
     * @param throwingBiFunction the functional
     * @param taskCallable       the callable
     * @return the throwing bi-function now throwing according to our exception strategy
     */
    private ThrowingBiFunction<TaskingHub, TaskCallable<?>, TaskResult> applyExceptionStrategy(ThrowingBiFunction<TaskingHub, TaskCallable<?>, TaskResult> throwingBiFunction,
                                                                                               TaskCallable<?> taskCallable) {
        return (hub, callable) -> {
            try {
                return throwingBiFunction.applyThrowing(hub, callable);

            } catch (Throwable throwable) {

                if (ExceptionStrategy.THROW_CAUSE.equals(exceptionStrategy))
                    throw throwable instanceof TaskRunException || throwable instanceof TaskProcessingException ?
                            throwable.getCause() : throwable;

                else if (ExceptionStrategy.THROW_STANDARDISED_EXCEPTION.equals(exceptionStrategy))
                    throw !(throwable instanceof TaskRunException || throwable instanceof TaskProcessingException) ?
                            new TaskProcessingException(taskCallable.getTaskClass(), throwable) : throwable;

                else if (ExceptionStrategy.CATCH.equals(exceptionStrategy)) {
                    TaskResult result = new TaskResult(TaskStatus.ERROR);
                    result.setThrowable(throwable);
                    return result;

                } else {
                    // Let's apply ExceptionStrategy.CATCH as fallback
                    TaskResult result = new TaskResult(TaskStatus.ERROR);
                    result.setThrowable(throwable);
                    return result;

                }
            }
        };

    }

    private void persistErrorStatus(TaskCallable<?> taskCallable) {
        JobMetaDataDao jmdDao = new JobMetaDataDao();
        String jobUuid = taskCallable.getJobUuid();

        try {
            jmdDao.mergeJobState(jobUuid, JobState.fromTaskStatus(TaskStatus.ERROR));
        } catch (MergeException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isTrackingJobMetaData() {
        return trackingJobMetaData;
    }

}
