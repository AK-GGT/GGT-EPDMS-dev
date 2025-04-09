package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.dao.GlobalReferenceDao;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.service.util.DataSetsTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class AssignUnassignTaskDataBundle {

    private static final Logger LOGGER = LogManager.getLogger(AssignUnassignTaskDataBundle.class);
    private final DependenciesMode depMode;
    private final DataStockMetaData affectedStockMeta;
    private InputType inputType;
    private DataSetReference[] dataSetReferences;
    private DataStockMetaData[] stocksToFetchFrom;
    private Set<DataSetsTypes> dataSetTypes;
    private Collection<GlobalReference> globalReferences;
    private boolean distinct = false;

    public AssignUnassignTaskDataBundle(@Nonnull final DataSetReference[] dataSetReferences,
                                        @Nonnull final DependenciesMode depMode,
                                        @Nonnull final DataStockMetaData affectedStockMeta) {
        inputType = InputType.DATA_SET_REFERENCES;
        this.dataSetReferences = dataSetReferences;
        this.depMode = depMode;
        this.affectedStockMeta = affectedStockMeta;
    }

    public AssignUnassignTaskDataBundle(@Nonnull final Set<DataSetsTypes> dataSetTypes,
                                        @Nonnull final DataStockMetaData[] stocksToFetchFrom,
                                        @Nonnull final DependenciesMode depMode,
                                        @Nonnull final DataStockMetaData affectedStockMeta) {
        inputType = InputType.DATA_SET_TYPES;
        this.depMode = depMode;
        this.affectedStockMeta = affectedStockMeta;
        this.stocksToFetchFrom = stocksToFetchFrom;
        this.dataSetTypes = dataSetTypes;
    }

    public AssignUnassignTaskDataBundle(@Nonnull final DataStockMetaData affectedStockMeta,
                                        @Nonnull final Collection<GlobalReference> globalReferences,
                                        @Nonnull final DataStockMetaData[] stocksToFetchFrom,
                                        @Nonnull final DependenciesMode depMode) {
        inputType = InputType.GLOBAL_REFERENCES;
        this.depMode = depMode;
        this.affectedStockMeta = affectedStockMeta;
        this.stocksToFetchFrom = stocksToFetchFrom;
        this.globalReferences = globalReferences;
    }

    public DataSetReference[] getDataSetReferences() {
        Instant start = Instant.now();

        // If using implied data set references, e.g. by type or global references,
        // we will need to fetch them first (from db, to e.g. obtain ids...)
        if (!InputType.DATA_SET_REFERENCES.equals(inputType)) {
            dataSetReferences = collectAllImpliedDataSets().toArray(new DataSetReference[0]);
            distinct = true; // We've collected the refs in a set.

            // Also, now that we have fetched the refs, we may act as if they had been the input type all along
            inputType = InputType.DATA_SET_REFERENCES;
            stocksToFetchFrom = null;
            dataSetTypes = null;
            globalReferences = null;
        }

        if (dataSetReferences == null)
            return new DataSetReference[0]; // We won't care about NPEs...

        if (!distinct) {
            dataSetReferences = Arrays.stream(dataSetReferences).distinct().toArray(DataSetReference[]::new);
            distinct = true;
        }

        LOGGER.debug("Loading references took " + Duration.between(start, Instant.now()).getSeconds() + "s");
        return dataSetReferences;
    }

    public DependenciesMode getDependenciesMode() {
        return depMode;
    }

    public DataStockMetaData getAffectedStockMeta() {
        return affectedStockMeta;
    }

    private Set<DataSetReference> collectAllImpliedDataSets() {
        Set<DataSetReference> collectedReferences = new HashSet<>();

        if (InputType.DATA_SET_TYPES.equals(inputType)) {
            for (DataSetsTypes type : dataSetTypes) {
                var daoType = DataSetDaoType.from(type);
                if (daoType == null)
                    throw new IllegalStateException(String.format("There appears to be a data set type without an corresponding dao type. Data set type was '%s'", type));
                DataSetDao<?, ?, ?> dao = daoType.getDao();
                collectedReferences.addAll(collectAllDataSetReferencesFromDao(dao));
            }
        } else if (InputType.GLOBAL_REFERENCES.equals(inputType)) {
            GlobalReferenceDao dao = new GlobalReferenceDao();
            collectedReferences = globalReferences.stream()
                    .map(dao::getByReference)
                    .filter(Objects::nonNull)
                    .map(DataSetReference::new)
                    .collect(Collectors.toSet());
        }

        return collectedReferences;
    }

    ///////////////////
    // Private utils //
    ///////////////////

    private Set<DataSetReference> collectAllDataSetReferencesFromDao(DataSetDao<?, ?, ?> dao) {
        Set<DataSetReference> collectedReferences = new HashSet<>();

        Long totalCount = dao.getCount(stocksToFetchFrom, null, true);
        if (totalCount == null || totalCount == 0) {
            return new HashSet<>();
        }
        int pageSize = 100;
        for (int i = 0; i < totalCount; i++) {
            int startIndex = i * pageSize;
            if (startIndex >= totalCount)
                break;
            int actualChunkSize = Math.max(0, Math.min((int) (totalCount - startIndex), pageSize));

            collectedReferences.addAll(dao.getDataSetReferences(stocksToFetchFrom, null,
                    true, startIndex, actualChunkSize));
        }

        return collectedReferences;
    }

    private enum InputType {
        DATA_SET_REFERENCES, GLOBAL_REFERENCES, DATA_SET_TYPES
    }
}
