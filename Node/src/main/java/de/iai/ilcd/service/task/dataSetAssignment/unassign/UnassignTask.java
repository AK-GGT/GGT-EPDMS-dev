package de.iai.ilcd.service.task.dataSetAssignment.unassign;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignTask;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignTaskDataBundle;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignTaskType;
import de.iai.ilcd.service.util.DataSetsTypes;
import de.iai.ilcd.service.util.JobType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UnassignTask extends AssignUnassignTask {

    public UnassignTask(@Nonnull AssignUnassignTaskDataBundle data, @Nonnull User signee) {
        super(data, signee);
    }

    public static UnassignTask fromData(@Nonnull Collection<DataSet> dsCollection,
                                        @Nonnull DataStock stock,
                                        @Nonnull DependenciesMode depMode,
                                        @Nonnull User signee) {
        DataSetReference[] refArr = dsCollection.stream().map(DataSetReference::new).toArray(DataSetReference[]::new);
        AssignUnassignTaskDataBundle dataBundle = new AssignUnassignTaskDataBundle(refArr, depMode,
                new DataStockMetaData(stock));
        return new UnassignTask(dataBundle, signee);
    }

    public static UnassignTask fromReferences(@Nonnull DataStock stock,
                                              @Nonnull Collection<GlobalReference> globalReferences,
                                              @Nonnull Collection<AbstractDataStock> stocksToFetchFrom,
                                              @Nonnull DependenciesMode depMode,
                                              @Nonnull User signee) {
        DataStockMetaData[] stocksToFetchFromMetaArr = stocksToFetchFrom.stream().map(DataStockMetaData::new)
                .toArray(DataStockMetaData[]::new);
        AssignUnassignTaskDataBundle dataBundle = new AssignUnassignTaskDataBundle(new DataStockMetaData(stock),
                globalReferences, stocksToFetchFromMetaArr, depMode);
        return new UnassignTask(dataBundle, signee);
    }

    /////////////////////////
    // Convenience methods //
    /////////////////////////

    public static UnassignTask fromTypes(@Nonnull DataStock stock,
                                         @Nonnull Collection<DataSetsTypes> dsTypes,
                                         @Nonnull Collection<AbstractDataStock> stocksToFetchFrom,
                                         @Nonnull DependenciesMode depMode,
                                         @Nonnull User signee) {
        DataStockMetaData[] stocksToFetchFromMetaArr = stocksToFetchFrom.stream().map(DataStockMetaData::new)
                .toArray(DataStockMetaData[]::new);
        Set<DataSetsTypes> dsTypesArr = new HashSet<>(dsTypes);
        AssignUnassignTaskDataBundle dataBundle = new AssignUnassignTaskDataBundle(dsTypesArr, stocksToFetchFromMetaArr,
                depMode, new DataStockMetaData(stock));
        return new UnassignTask(dataBundle, signee);
    }

    @Override
    protected AssignUnassignTaskType getAssignmentType() {
        return AssignUnassignTaskType.DETACH;
    }

    @Override
    protected JobType determineJobType(AssignUnassignTaskDataBundle data) {
        return JobType.DETACH;
    }

}
