package de.iai.ilcd.service.task.abstractTask;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.abstractTask.runLogImplementations.DataSetReferencingRunLog;
import de.iai.ilcd.util.result.DataSetResult;
import de.iai.ilcd.util.result.ResultType;

import java.util.Set;

/**
 * Extension of AbstractLogging task that uses a specific TaskRunLog implementation: DataSetReferencingRunLog. This
 * run log is convenient when processing data sets.
 *
 * @param <T> the data type (typically a custom type bundling several objects)
 */
public abstract class AbstractDataSetLoggingTask<T> extends AbstractLoggingTask<T, DataSetReferencingRunLog> {

    public AbstractDataSetLoggingTask(T data, User signee) {
        super(data, signee);
    }

    @Override
    protected DataSetReferencingRunLog initTaskLog() {
        // We will ignore success and info entries
        return new DataSetReferencingRunLog(LogLevel.SUCCESS, LogLevel.INFO);
    }

    public void log(DataSetResult dataSetResult, boolean verbose) {
        getRunLog().log(dataSetResult, verbose);
        publishLog();
    }

    public void log(Set<DataSetResult> results) {
        for (DataSetResult result : results)
            getRunLog().log(result);

        publishLog();
    }

    public void log(DataSetResult dataSetResult) {
        getRunLog().log(dataSetResult);
        publishLog();
    }

    public void log(DataSet dataSet, ResultType resultType) {
        getRunLog().log(dataSet, resultType);
        publishLog();
    }

    public void log(DataSetReference dataSetReference, ResultType resultType) {
        getRunLog().log(dataSetReference, resultType);
        publishLog();
    }

    public void log(Set<DataSetReference> dataSetReferences, ResultType resultType) {
        for (DataSetReference dataSetReference : dataSetReferences)
            getRunLog().log(dataSetReference, resultType);

        publishLog();
    }

    public void logError(DataSetReference dataSetReference) {
        getRunLog().error(dataSetReference);
        publishLog();
    }

    public void logError(Set<DataSetReference> dataSetReferences) {
        for (DataSetReference dataSetReference : dataSetReferences)
            getRunLog().error(dataSetReference);

        publishLog();
    }

    public void logError(DataSet dataSet) {
        getRunLog().error(dataSet);
        publishLog();
    }

    public void logWarning(DataSetReference dataSetReference) {
        getRunLog().warning(dataSetReference);
        publishLog();
    }

    public void logWarning(Set<DataSetReference> dataSetReferences) {
        for (DataSetReference dataSetReference : dataSetReferences)
            getRunLog().warning(dataSetReference);

        publishLog();
    }

    public void logWarning(DataSet dataSet) {
        getRunLog().warning(dataSet);
        publishLog();
    }

    public void logInfo(DataSetReference dataSetReference) {
        getRunLog().info(dataSetReference);
        publishLog();
    }

    public void logInfo(Set<DataSetReference> dataSetReferences) {
        for (DataSetReference dataSetReference : dataSetReferences)
            getRunLog().info(dataSetReference);

        publishLog();
    }

    public void logInfo(DataSet dataSet) {
        getRunLog().info(dataSet);
        publishLog();
    }

    public void logSuccess(DataSetReference dataSetReference) {
        getRunLog().success(dataSetReference);
        publishLog();
    }

    public void logSuccess(Set<DataSetReference> dataSetReferences) {
        for (DataSetReference dataSetReference : dataSetReferences)
            getRunLog().success(dataSetReference);

        publishLog();
    }

    public void logSuccess(DataSet dataSet) {
        getRunLog().success(dataSet);
        publishLog();
    }

}
