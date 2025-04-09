package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractDataSetLoggingTask;
import de.iai.ilcd.service.task.abstractTask.runLogImplementations.DataSetReferencingRunLog;
import de.iai.ilcd.util.DependenciesUtil;
import de.iai.ilcd.webgui.controller.admin.export.DBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AssignUnassignTask extends AbstractDataSetLoggingTask<AssignUnassignTaskDataBundle> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AssignUnassignTask.class);

    public AssignUnassignTask(AssignUnassignTaskDataBundle data, User signee) {
        super(data, signee);
    }

    @Override
    protected DataSetReferencingRunLog initTaskLog() {
        return new AssignmentTaskRunLog(LogLevel.SUCCESS, LogLevel.INFO);
    }

    /////////////////////////
    // Mandatory Overrides //
    /////////////////////////

    protected abstract AssignUnassignTaskType getAssignmentType();

    /////////////////////////////////
    // The basic task construction //
    /////////////////////////////////

    @Override
    protected String getInitialDescription(AssignUnassignTaskDataBundle data) {

        return getAssignmentType().getVerb() + " ??? " +
                " data sets " +
                getAssignmentType().getPreposition() +
                " data stock " +
                data.getAffectedStockMeta().getName() +
                " (" + data.getAffectedStockMeta().getUuidAsString() + ")" +
                " with dependency mode " +
                data.getDependenciesMode().name() + ".";
    }

    @Override
    protected String getDescription(AssignUnassignTaskDataBundle data) {

        return getAssignmentType().getVerb() + " " +
                data.getDataSetReferences().length +
                " data sets " +
                getAssignmentType().getPreposition() +
                " data stock " +
                data.getAffectedStockMeta().getName() +
                " (" + data.getAffectedStockMeta().getUuidAsString() + ")" +
                " with dependency mode " +
                data.getDependenciesMode().name() + ".";
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(AssignUnassignTaskDataBundle data) {
        return TaskProcessingStrategy.BACKGROUND_QUEUE;
    }

    @Override
    protected TaskResult process(AssignUnassignTaskDataBundle data) throws TaskRunException {
        DependenciesMode depMode = data.getDependenciesMode();
        DataStockMetaData targetMeta = data.getAffectedStockMeta();
        DataSetReference[] refs = data.getDataSetReferences();

        // We use a custom batch-method, that utilises JdbcTemplate
        // and we have a BatchProcessor that handles batching and triggering of said method.
        JdbcTemplate jdbcTemplate = DBConfig.getJdbcTemplate();
        AssignUnassignBatchMethod batchMethod = getAssignmentType().getBatchMethod(jdbcTemplate, targetMeta);
        BatchProcessor batchProcessor = new BatchProcessor(batchMethod, this::log);

        // All that's left to do is supplying the batch processor with data sets
        // it will handle processing according to the AssignUnassigTaskType.method
        for (DataSetReference ref : refs) {
            isContinue(); // make sure the thread has not received an interrupt.

            if (!DependenciesMode.NONE.equals(depMode)) {
                EntityManager em = PersistenceUtil.getEntityManager();
                DataSet ds = em.find(ref.getDataSetClass(), ref.getId());
                Set<DataSetReference> deps = DependenciesUtil
                        .getDependencyCluster(ds, depMode, true,
                                batchProcessor.getKnownReferences().stream()
                                        .map(DataSetReference::getUuidAsString)
                                        .collect(Collectors.toSet()))
                        .stream()
                        .map(DataSetReference::new)
                        .collect(Collectors.toSet());
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Fetched " + deps.size() + " dependencies.");
                batchProcessor.addDependencyCluster(ref, deps);
            } else {
                batchProcessor.addDependencyCluster(ref, Set.of());
            }
        }

        batchProcessor.consumeEverything();

        // The last step is to set the stock modified. We do so in a transaction, but only if the stock is
        // not yet set modified.
        if (getRunLog().getSuccessesCount() > 0) {
            EntityManager em = PersistenceUtil.getEntityManager();
            DataStock stock = (DataStock) em.find(targetMeta.getDataStockClass(), targetMeta.getId());
            if (!stock.isSetModified()) {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                stock.setModified(true);
                tx.commit();
            }
            em.detach(stock);
        }

        return new TaskResult(TaskStatus.COMPLETE);
    }

}
