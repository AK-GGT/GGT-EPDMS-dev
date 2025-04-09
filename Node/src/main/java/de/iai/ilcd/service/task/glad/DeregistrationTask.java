package de.iai.ilcd.service.task.glad;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.glad.GLADRegistrationDataDao;
import de.iai.ilcd.service.glad.GLADRegistrationDataDaoImpl;
import de.iai.ilcd.service.glad.GLADRegistrationServiceImpl;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractLoggingTask;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;

import java.util.Collection;

public class DeregistrationTask<T extends DataSet> extends AbstractLoggingTask<Collection<T>, RegistrationDeregistrationRunLog> {

    public static final int LARGE_WORKLOAD = 30;

    public static final String TASK_RESULT_DETAILS_KEY = "deregistrationResults";

    public DeregistrationTask(Collection<T> data, User user) {
        super(data, user);
    }

    @Override
    protected RegistrationDeregistrationRunLog initTaskLog() {
        return new RegistrationDeregistrationRunLog();
    }

    @Override
    protected String getDescription(Collection<T> data) {
        return "Registering " + data.size() + " data sets to GLAD.";
    }

    @Override
    protected TaskResult process(Collection<T> data) throws TaskRunException {
        GLADRegistrationDataDao registrationDataDao = new GLADRegistrationDataDaoImpl();
        TaskResult result = new TaskResult();

        for (DataSet ds : data) {
            isContinue(); // Check whether thread has not received any interrupts

            try {
                GLADRegistrationServiceImpl.doDeregister(ds, registrationDataDao);
                getRunLog().incrementSuccessesCount();

            } catch (RestWSUnknownException rue) {
                rue.printStackTrace();
                getRunLog().incrementErrorsCount();

            } catch (AuthenticationException ae) {
                throw new TaskRunException(this.getClass(), ae);
            }

            publishLog();
        }

        result.setStatus(TaskStatus.COMPLETE);
        return result;
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(Collection<T> data) {
        if (data.size() <= LARGE_WORKLOAD)
            return TaskProcessingStrategy.IMMEDIATELY;
        else
            return TaskProcessingStrategy.BACKGROUND_THREAD;
    }

}
