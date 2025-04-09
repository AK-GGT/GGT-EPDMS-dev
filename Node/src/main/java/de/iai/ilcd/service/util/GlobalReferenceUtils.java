package de.iai.ilcd.service.util;

import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Static Util methods for dealing with local References
 */
public class GlobalReferenceUtils {

    /*
     * The logger for logging data
     */
    protected static final Logger log = LogManager.getLogger(GlobalReferenceUtils.class);

    /**
     * Gets referenced data sets from given stocks. If the stocks are null,
     * it just gets all available (referenced) data sets.
     *
     * @param refs
     * @param sourceStocks
     * @return
     */
    public static DataSet[] getReferencedDataSetsFromStocks(Collection<GlobalReference> references,
                                                            IDataStockMetaData[] sourceStocks) {
        List<DataSet> dataSets = new ArrayList<>();
        EnumMap<GlobalReferenceTypeValue, Set<GlobalReference>> typedReferences = createInvertedTypeMap(references);

        for (GlobalReferenceTypeValue typeKey : typedReferences.keySet()) {
            Collection<DataSetDao<?, ?, ?>> daos = DataSetDao.getDaosForType(typeKey);
            Collection<GlobalReference> relevantReferences = restrictReferencesToStocks(typedReferences.get(typeKey), sourceStocks, daos);
            for (GlobalReference reference : relevantReferences) {
                DataSet dataSet = fetch(reference, daos);
                if (dataSet != null)
                    dataSets.add(dataSet);
            }
        }
        return dataSets.toArray(new DataSet[dataSets.size()]);
    }

    /**
     * Tries to find dataSet from given reference using the provided daos.
     *
     * @param reference
     * @param daos
     * @return null if nothing has been found.
     */
    private static DataSet fetch(GlobalReference reference, Collection<DataSetDao<?, ?, ?>> daos) {
        if (reference == null)
            return null;

        for (DataSetDao<?, ?, ?> dao : daos) {
            DataSet dataSet = dao.getByUuidAndOptionalVersion(reference.getUuid().getUuid(), reference.getVersion());
            if (dataSet != null)
                return dataSet;
        }
        if (log.isDebugEnabled())
            log.debug("No " + reference.getType().getValue() + " dataset found with UUID: " + reference.getUuid().getUuid());
        return null;
    }

    /**
     * Takes a collection of references, groups them by type and puts them into a
     * map with types as keys.
     * This way we can instantiate one dao per type, while iterating through all references of the given type.
     *
     * @param refs
     * @return
     */
    private static EnumMap<GlobalReferenceTypeValue, Set<GlobalReference>> createInvertedTypeMap(Collection<GlobalReference> refs) {
        EnumMap<GlobalReferenceTypeValue, Set<GlobalReference>> invertedTypeMap = new EnumMap<>(GlobalReferenceTypeValue.class);
        for (GlobalReferenceTypeValue type : GlobalReferenceTypeValue.values()) {
            invertedTypeMap.put(type, new HashSet<GlobalReference>());
        }
        for (GlobalReference ref : refs) {
            Set<GlobalReference> set = invertedTypeMap.get(ref.getType());
            set.add(ref);
            invertedTypeMap.put(ref.getType(), set);
        }
        for (GlobalReferenceTypeValue type : invertedTypeMap.keySet()) {
            if (invertedTypeMap.get(type).isEmpty())
                invertedTypeMap.remove(type);
        }

        invertedTypeMap.remove(GlobalReferenceTypeValue.OTHER_EXTERNAL_FILE); //For now those data sets will not be handled.

        return invertedTypeMap;
    }

    /**
     * Restricts a collection of references for which the same dao is responsible to
     * a collection of Stocks.
     *
     * @param set
     * @param sourceStocks
     * @param suitableDao
     * @return
     */
    private static Collection<GlobalReference> restrictReferencesToStocks(Collection<GlobalReference> collection,
                                                                          IDataStockMetaData[] sourceStocks, Collection<DataSetDao<?, ?, ?>> daos) {
        if (sourceStocks == null) {
            return collection;
        }
        Collection<GlobalReference> trimmedSet = new HashSet<>();
        for (int i = 0; i < sourceStocks.length; i++) {
            for (GlobalReference reference : collection) {
                if (relevantReference(reference, daos, sourceStocks[i])) {
                    trimmedSet.add(reference);
                }
            }
            collection.removeAll(trimmedSet);
        }
        return trimmedSet;
    }

    private static Boolean relevantReference(GlobalReference reference, Collection<DataSetDao<?, ?, ?>> daos,
                                             IDataStockMetaData stock) {
        for (DataSetDao<?, ?, ?> dao : daos) {
            if (dao.isInDatastockByUuid(reference.getUuid().getUuid(), reference.getVersion(), stock)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetches the referenced data sets.
     *
     * @param references
     * @return
     */
    DataSet[] getReferencedDataSets(Collection<GlobalReference> references) {
        return getReferencedDataSetsFromStocks(references, null);
    }

}
