package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.model.common.DataSetReference;

import java.util.Objects;

class RootedDataSetReference {

    private final DataSetReference rootDataSetReference;

    private final Boolean isDependency;

    private final DataSetReference dataSetReference;

    RootedDataSetReference(DataSetReference rootDataSetReference, Boolean isDependency, DataSetReference dataSetReference) {
        if (rootDataSetReference == null)
            throw new IllegalArgumentException("Root data set reference can't be null");
        else if (dataSetReference == null)
            throw new IllegalArgumentException("Data set reference can't be null");

        this.rootDataSetReference = rootDataSetReference;
        this.isDependency = isDependency;
        this.dataSetReference = dataSetReference;
    }

    public DataSetReference getRootDataSetReference() {
        return rootDataSetReference;
    }

    public boolean isDependency() {
        return isDependency != null ? isDependency : rootDataSetReference.equals(dataSetReference);
    }

    public boolean isRoot() {
        return !isDependency();
    }

    public DataSetReference getDataSetReference() {
        return dataSetReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RootedDataSetReference other = (RootedDataSetReference) o;
        return rootDataSetReference.equals(other.rootDataSetReference) && dataSetReference.equals(other.dataSetReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootDataSetReference, dataSetReference);
    }
}
