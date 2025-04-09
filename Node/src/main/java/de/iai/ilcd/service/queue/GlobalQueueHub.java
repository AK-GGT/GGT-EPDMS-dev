package de.iai.ilcd.service.queue;

import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.TaskCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Callable;

/**
 * Service to execute runnables, callables and tasks. In addition to the option to simple async execution
 * there's the option to utilize a global (injected) queue.
 */
@Component("globalQueueHub")
public class GlobalQueueHub implements TaskingHub {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalQueueHub.class);

    private ThreadPoolTaskExecutor queue;

    @Autowired
    public GlobalQueueHub(@Qualifier("globalQueue") ThreadPoolTaskExecutor globalQueue) {
        this.queue = globalQueue;
        LOGGER.info("Queue initialised " + queue);
    }

    @PreDestroy
    private void shutdown() {
        LOGGER.info("Shutting down " + queue);
        queue.shutdown();
        LOGGER.info("Shutdown of " + queue + " complete.");
    }

    @Override
    public TaskResult runImmediately(TaskCallable<?> callable) throws TaskRunException {
        try {
            return callable.call();

        } catch (TaskRunException tre) {

            LOGGER.error(null, tre);
            return new TaskResult(TaskStatus.ERROR, tre);
        }
    }

    @Async
    public void runAsynchronously(Runnable runnable) {
        runnable.run();
    }

    @Async
    public void runAsynchronously(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error(null, e);
        }
    }

    @Override
    public void runAsynchronously(TaskCallable<?> callable) {
        try {
            callable.call();

        } catch (TaskRunException tre) {
            LOGGER.error(null, tre);
        }
    }

    public void runInQueue(Runnable runnable) {
        queue.execute(runnable);
    }

    public void runInQueue(Callable<?> callable) {
        queue.execute(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                LOGGER.error(null, e);
            }
        });
    }

    @Override
    public void runInQueue(TaskCallable<?> callable) {
        queue.execute(() -> {
            try {
                callable.call();

            } catch (TaskRunException tre) {
                LOGGER.error(null, tre);
            }
        });
    }
}
