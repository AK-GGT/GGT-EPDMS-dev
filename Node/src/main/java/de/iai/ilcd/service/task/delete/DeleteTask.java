package de.iai.ilcd.service.task.delete;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractDataSetLoggingTask;
import de.iai.ilcd.service.util.JobType;
import de.iai.ilcd.util.DependenciesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DeleteTask extends AbstractDataSetLoggingTask<DeleteTaskDataBundle> {

    private static final Logger LOGGER = LogManager.getLogger(DeleteTask.class);

    private final int primaryRefsCount;

    private Integer totalRefsCount = null;

    public DeleteTask(@Nonnull DeleteTaskDataBundle data, @Nonnull User signee) {
        super(data, signee);
        primaryRefsCount = countDataSetReferences(data);
    }

    public static DeleteTask from(@Nonnull Collection<DataSet> dataSetCollection, @Nonnull DependenciesMode depMode,
                                  @Nonnull User signee) {
        Collection<DataSetReference> refCollection = dataSetCollection.stream()
                .map(DataSetReference::new)
                .collect(Collectors.toList());
        DeleteTaskDataBundle dataBundle = new DeleteTaskDataBundle(refCollection, depMode);
        return new DeleteTask(dataBundle, signee);
    }

    @Override
    protected String getDescription(DeleteTaskDataBundle data) {
        return "Deleting " + primaryRefsCount +
                " data sets with dependencies mode '" +
                data.getDependenciesMode() +
                (totalRefsCount != null ? "' (total count: " + totalRefsCount + ")" : "");
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(DeleteTaskDataBundle data) {
        return TaskProcessingStrategy.BACKGROUND_QUEUE;
    }

    @Override
    protected void preProcessImmediately(DeleteTaskDataBundle data, User signee) {
        super.preProcessImmediately(data, signee);
        for (Map.Entry<DataSetDaoType, Set<DeleteReference>> e : data.getDataSetRefsByDaoType().entrySet()) {
            Collection<DataSetReference> dsRefCollection = e.getValue().stream().map(DeleteReference::getDsRef).collect(Collectors.toList());
            e.getKey().getDao().flagForDeletion(dsRefCollection);
        }
    }

    @Override
    protected TaskResult process(DeleteTaskDataBundle data) throws TaskRunException {
        Map<DataSetDaoType, Set<DataSetReference>> dependencyRefsByDaoType = collectAllDependencies(data);
        for (Map.Entry<DataSetDaoType, Set<DataSetReference>> e : dependencyRefsByDaoType.entrySet()) {
            if (e.getKey() == null)
                throw new IllegalStateException("Data set dao type can't be null.");
            e.getKey().getDao().flagForDeletion(e.getValue());
            data.addAllDependencies(e.getKey(), e.getValue());
        }

        totalRefsCount = countDataSetReferences(data);
        getRunLog().setTitle(getDescription(data)); // update run log title

        Comparator<DataSetDaoType> comparator = DataSetDaoType.getComparatorForProcessingOrder().reversed();
        Iterator<DataSetDaoType> keyIterator = DataSetDaoType.iterator(comparator);
        while (keyIterator.hasNext() && isContinue()) {
            DataSetDaoType key = keyIterator.next();
            DataSetDao<?, ?, ?> dao = key.getDao();

            data.getDataSetRefsByDaoType().get(key).forEach(ref -> {
                DataSetReference dsRef = ref.getDsRef();
                try {
                    if (!isContinue())
                        throw new InterruptedException();

                    dao.removeById(dsRef.getId());

                    if (ref.isDependency())
                        logInfo(dsRef);
                    else
                        logSuccess(dsRef);

                } catch (Exception e) {
                    if (e instanceof InterruptedException)
                        LOGGER.error("Failed to delete data set " + dsRef + " (interrupted)");
                    else
                        LOGGER.error("Failed to delete data set " + dsRef, e);

                    if (ref.isDependency())
                        logWarning(dsRef);
                    else
                        logError(dsRef);
                }
            });
        }

        return new TaskResult(TaskStatus.COMPLETE);
    }

    @Override
    protected TaskResult postProcessFinally(DeleteTaskDataBundle data, User user, TaskResult result) {
        result = super.postProcessFinally(data, user, result);

        Set<DataSetReference> refs;
        for (Map.Entry<DataSetDaoType, Set<DeleteReference>> e : data.getDataSetRefsByDaoType().entrySet()) {
            refs = e.getValue().stream()
                    .map(DeleteReference::getDsRef)
                    .collect(Collectors.toSet());
            refs.removeAll(getRunLog().getReferences(LogLevel.SUCCESS));
            refs.removeAll(getRunLog().getReferences(LogLevel.INFO));
            e.getKey().getDao().unflagForDeletion(refs);
        }

        return result;
    }

    ///////////
    // Utils //
    ///////////

    @Override
    protected JobType determineJobType(DeleteTaskDataBundle data) {
        return JobType.DELETE; // Legacy...
    }

    private Map<DataSetDaoType, Set<DataSetReference>> collectAllDependencies(DeleteTaskDataBundle data) {
        EnumMap<DataSetDaoType, Set<DataSetReference>> resultsByDaoType = new EnumMap<>(DataSetDaoType.class);
        for (DataSetDaoType dt : DataSetDaoType.values())
            resultsByDaoType.put(dt, new HashSet<>());

        Collection<DeleteReference> primaryRefsCopy = new ArrayList<>();
        data.getDataSetRefsByDaoType().values().forEach(primaryRefsCopy::addAll);

        Collection<DataSet> dsDependencies;
        for (DeleteReference ref : primaryRefsCopy) {
            dsDependencies = DependenciesUtil.getDependencyCluster(ref.getDsRef().getReferencedDataSet(),
                    data.getDependenciesMode(), true, data.getAllUuids());
            dsDependencies.forEach(ds ->
                    resultsByDaoType.get(DataSetDaoType.getFor(ds)).add(new DataSetReference(ds)));
        }
        return resultsByDaoType;
    }

    /////////////////////
    // For Convenience //
    /////////////////////

    private int countDataSetReferences(DeleteTaskDataBundle data) {
        return data.getDataSetRefsByDaoType().values().stream()
                .filter(Objects::nonNull)
                .mapToInt(Set::size)
                .sum();
    }
}
