package de.iai.ilcd.util.result;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DataSetResult {

    DataSetReference dataSetReference;

    ResultType resultType;

    List<DataSetResult> dependencyResults = new ArrayList<>();

    public DataSetResult() {
        super();
    }

    public DataSetResult(DataSet dataSet, ResultType resultType) {
        this.dataSetReference = new DataSetReference(dataSet);
        this.resultType = resultType;
    }

    public DataSetResult(DataSetReference dataSetReference, ResultType resultType) {
        this.dataSetReference = dataSetReference;
        this.resultType = resultType;
    }

    public void addDependencyResult(DataSetResult result) {
        mergeType(result);
        dependencyResults.add(result);
    }

    public void addDependencyResult(DataSet dependency, ResultType resultType) {
        mergeType(resultType);
        addDependencyResult(new DataSetResult(dependency, resultType));
    }

    public void addDependencyResult(DataSetReference dependencyReference, ResultType resultType) {
        mergeType(resultType);
        addDependencyResult(new DataSetResult(dependencyReference, resultType));
    }

    public void addAllDependencyResults(Collection<DataSetResult> dependencyResults) {
        mergeType(dependencyResults);
        this.dependencyResults.addAll(dependencyResults);
    }

    public DataSetReference getDataSetReference() {
        return dataSetReference;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public List<DataSetResult> getDependencyResults() {
        return dependencyResults;
    }

    private void mergeType(Collection<DataSetResult> dependencyResults) {
        if (!ResultType.ERROR.equals(this.resultType)
                && !ResultType.PARTIALLY_SUCCESSFUL.equals(this.resultType)) {
            for (DataSetResult r : dependencyResults)
                if (mergeType(r))
                    break;
        }
    }

    private boolean mergeType(DataSetResult dependencyResult) {
        return mergeType(dependencyResult.getResultType());
    }

    private boolean mergeType(ResultType type) {
        boolean adjustmentMade = false;

        if (!ResultType.ERROR.equals(this.resultType)
                && !ResultType.PARTIALLY_SUCCESSFUL.equals(this.resultType)) {
            if (ResultType.ERROR.equals(type)
                    || ResultType.PARTIALLY_SUCCESSFUL.equals(type)) {

                this.resultType = ResultType.PARTIALLY_SUCCESSFUL;
                adjustmentMade = true;

            }
        }

        return adjustmentMade;
    }

    public boolean hasEmptyRootReference() {
        return dataSetReference == null;
    }

    public boolean hasEmptyRootResultType() {
        return resultType == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSetResult that = (DataSetResult) o;
        return dataSetReference.equals(that.dataSetReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSetReference);
    }
}
