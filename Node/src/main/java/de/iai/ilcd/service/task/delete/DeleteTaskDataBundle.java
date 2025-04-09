package de.iai.ilcd.service.task.delete;

import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.dao.DependenciesMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DeleteTaskDataBundle {

    private static final Logger LOGGER = LogManager.getLogger(DeleteTaskDataBundle.class);

    private EnumMap<DataSetDaoType, Set<DeleteReference>> dataSetRefsByDaoType = new EnumMap<>(DataSetDaoType.class);

    private DependenciesMode depMode;

    public DeleteTaskDataBundle(@Nonnull Collection<DataSetReference> refs, @Nonnull DependenciesMode depMode) {
        super();
        if (refs.size() == 0)
            throw new IllegalArgumentException("data set collection can't be empty.");

        for (DataSetDaoType dt : DataSetDaoType.values())
            dataSetRefsByDaoType.put(dt, new HashSet<>());

        for (DataSetReference ref : refs) {
            dataSetRefsByDaoType.get(ref.getDaoType()).add(new DeleteReference(ref, false));
        }

        this.depMode = depMode;
    }

    public void addAllDependencies(@Nonnull DataSetDaoType key, Collection<DataSetReference> dependencyRefs) {
        if (dependencyRefs == null || dependencyRefs.isEmpty()) {
            LOGGER.debug("No dependencies added to data bundle, provided collection was " +
                    (dependencyRefs == null ? "null" : "empty"));
            return;
        }

        dataSetRefsByDaoType.get(key).addAll(dependencyRefs.stream()
                .filter(Objects::nonNull)
                .map(ref -> new DeleteReference(ref, true))
                .collect(Collectors.toList()));
    }

    public EnumMap<DataSetDaoType, Set<DeleteReference>> getDataSetRefsByDaoType() {
        return dataSetRefsByDaoType;
    }

    public DependenciesMode getDependenciesMode() {
        return depMode;
    }

    public Set<String> getAllUuids() {
        return dataSetRefsByDaoType.values().stream()
                .flatMap(set -> set.stream())
                .map(ref -> ref.getDsRef().getUuidAsString())
                .collect(Collectors.toSet());
    }
}
