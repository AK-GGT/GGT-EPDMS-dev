package de.iai.ilcd.service.task.dummy;


import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractLoggingTask;
import de.iai.ilcd.service.util.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepTask extends AbstractLoggingTask<Integer, SleepTaskDummyRunLog> {

    private static final int sleepUnit = 1000; // milliseconds

    private static final int acceptableInMainThread = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(SleepTask.class);

    public SleepTask(Integer data, User user) {
        super(data, user);
    }

    @Override
    protected SleepTaskDummyRunLog initTaskLog() {
        return new SleepTaskDummyRunLog();
    }

    @Override
    protected String getDescription(Integer data) {
        return "How wonderful: " + data + " seconds for napping!";
    }

    @Override
    protected TaskResult process(Integer data) throws TaskRunException {
        TaskResult result = new TaskResult();
        int seconds = data != null ? data : 0;

        // sleeping
        System.out.print(System.getProperty("line.separator"));
        for (int i = 0; i < seconds; i++) {

            try {
                Thread.sleep(sleepUnit);
                System.out.print(".");
                success("."); // will appear in the runlog.

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                LOGGER.error("Sleep unit " + (i + 1) + " was interrupted :(", e);
                error("Sleep unit " + (i + 1) + " was interrupted :("); // for the runlog

                throw new TaskRunException(this.getClass(), e);
            }

            publishLog();

        }

        System.out.print(System.getProperty("line.separator"));
        result.setStatus(TaskStatus.COMPLETE);

        return result;
    }

    @Override
    protected JobType determineJobType(Integer data) {
        return JobType.OTHER;
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(Integer data) {
        int seconds = data == null ? 0 : data;
        if (seconds <= acceptableInMainThread)
            return TaskProcessingStrategy.IMMEDIATELY;
        else if (seconds <= acceptableInMainThread + 5)
            return TaskProcessingStrategy.BACKGROUND_THREAD;
        else
            return TaskProcessingStrategy.BACKGROUND_QUEUE;
    }

}

