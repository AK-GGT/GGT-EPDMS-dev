package de.iai.ilcd.service.queue;

import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.abstractTask.AbstractTask;
import de.iai.ilcd.service.task.abstractTask.TaskCallable;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Callable;

/**
 * Interface defining the processing options that are needed in order to run tasks. They resemble the processing
 * strategies a task can call for.
 */
public interface TaskingHub {

    default TaskResult run(AbstractTask<?> task) throws Throwable {
        return task.runIn(this);
    }

    TaskResult runImmediately(TaskCallable<?> callable) throws TaskRunException;

    @Async
    void runAsynchronously(TaskCallable<?> callable);

    @Async
    void runAsynchronously(Runnable runnable);

    @Async
    void runAsynchronously(Callable<?> callable);

    void runInQueue(TaskCallable<?> callable);

    void runInQueue(Runnable runnable);

    void runInQueue(Callable<?> callable);

}
