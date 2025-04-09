package de.iai.ilcd.service.task.glad;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.rest.util.InvalidGLADUrlException;
import de.iai.ilcd.service.glad.GLADRegistrationDataDao;
import de.iai.ilcd.service.glad.GLADRegistrationDataDaoImpl;
import de.iai.ilcd.service.glad.GLADRegistrationServiceImpl;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractLoggingTask;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegistrationTask<T extends DataSet> extends AbstractLoggingTask<Collection<T>, RegistrationDeregistrationRunLog> {

    public static final int LARGE_WORKLOAD = 30;

    public static final String TASK_RESULT_DETAILS_KEY = "registrationResults";

    public RegistrationTask(Collection<T> data, User user) {
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
        List<DataSetRegistrationResult> dsRegistrationResults = new ArrayList<>();

        for (DataSet ds : data) {
            isContinue(); // Make sure thread has not received any interrupts.

            try {
                DataSetRegistrationResult registrationResult = GLADRegistrationServiceImpl.doRegister(ds, registrationDataDao);
                getRunLog().add(ds, registrationResult);
                dsRegistrationResults.add(registrationResult);

            } catch (NodeIllegalStatusException | AuthenticationException | RestWSUnknownException |
                     InvalidGLADUrlException e) {
                throw new TaskRunException(this.getClass(), e);
            }

            publishLog();
        }

        result.putData(TASK_RESULT_DETAILS_KEY, dsRegistrationResults);
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
