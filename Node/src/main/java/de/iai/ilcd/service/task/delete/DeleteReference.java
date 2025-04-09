package de.iai.ilcd.service.task.delete;

import de.iai.ilcd.model.common.DataSetReference;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DeleteReference {

    private DataSetReference dsRef;

    private Boolean isDependency;

    DeleteReference(@Nonnull DataSetReference dsRef, boolean isDependency) {
        this.dsRef = dsRef;
        this.isDependency = isDependency;
    }

    Boolean isDependency() {
        return isDependency;
    }

    DataSetReference getDsRef() {
        return dsRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DeleteReference other = (DeleteReference) o;
        return dsRef.equals(other.dsRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dsRef);
    }
}
