package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.service.util.DataSetsTypes;

import java.util.*;

public enum DataSetDaoType {

    CONTACT(new ContactDao(), DataSetType.CONTACT, de.iai.ilcd.model.contact.Contact.class),
    ELEMENTARY_FLOW(new ElementaryFlowDao(), DataSetType.FLOW, de.iai.ilcd.model.flow.ElementaryFlow.class),
    PRODUCT_FLOW(new ProductFlowDao(), DataSetType.FLOW, de.iai.ilcd.model.flow.ProductFlow.class),
    FLOW_PROPERTY(new FlowPropertyDao(), DataSetType.FLOWPROPERTY, de.iai.ilcd.model.flowproperty.FlowProperty.class),
    LCIA_METHOD(new LCIAMethodDao(), DataSetType.LCIAMETHOD, de.iai.ilcd.model.lciamethod.LCIAMethod.class),
    LIFE_CYCLE_MODEL(new LifeCycleModelDao(), DataSetType.LIFECYCLEMODEL, de.iai.ilcd.model.lifecyclemodel.LifeCycleModel.class),
    PROCESS(new ProcessDao(), DataSetType.PROCESS, de.iai.ilcd.model.process.Process.class),
    SOURCE(new SourceDao(), DataSetType.SOURCE, de.iai.ilcd.model.source.Source.class),
    UNIT_GROUP(new UnitGroupDao(), DataSetType.UNITGROUP, de.iai.ilcd.model.unitgroup.UnitGroup.class);

    final DataSetDao<?, ?, ?> dao;
    final DataSetType dsType;
    final Class<? extends DataSet> datasetClass;

    DataSetDaoType(DataSetDao<?, ?, ?> dao, DataSetType dsType, Class<? extends DataSet> datasetClass) {
        this.dao = dao;
        this.dsType = dsType;
        this.datasetClass = datasetClass;
    }

    public static DataSetDaoType getFor(DataSet ds) {
        if (ds == null || ds.getDataSetType() == null)
            return null;

        // DataSetType.Flow is not sufficient (there's two possible daos responsible)..
        if (ds instanceof ElementaryFlow) {
            return ELEMENTARY_FLOW;
        } else if (ds instanceof ProductFlow) {
            return PRODUCT_FLOW;

        } else {
            // data set type is sufficient..

            for (DataSetDaoType daoType : DataSetDaoType.values())
                if (daoType.getDsType().equals(ds.getDataSetType()))
                    return daoType;
        }

        // else we have found nothing.
        return null;
    }

    public static DataSetDaoType getFor(Class<? extends DataSet> dataSetClass) {
        for (DataSetDaoType daoType : DataSetDaoType.values()) {
            if (daoType.datasetClass.equals(dataSetClass))
                return daoType;
        }
        return null;
    }

    public static EnumMap<DataSetDaoType, Set<DataSet>> groupByDaoType(Collection<DataSet> dsCollection) {
        EnumMap<DataSetDaoType, Set<DataSet>> invertedTypeMap = new EnumMap<>(DataSetDaoType.class);

        // populate the map with empty sets
        for (DataSetDaoType type : DataSetDaoType.values()) {
            invertedTypeMap.put(type, new HashSet<>());
        }

        // sort the data sets
        for (DataSet ds : dsCollection) {
            DataSetDaoType daoType = DataSetDaoType.getFor(ds);
            Set<DataSet> set = invertedTypeMap.get(daoType);
            set.add(ds);
            invertedTypeMap.put(daoType, set);
        }

        // remove entries with no data sets
        for (DataSetDaoType type : invertedTypeMap.keySet()) {
            if (invertedTypeMap.get(type).isEmpty())
                invertedTypeMap.remove(type);
        }

        return invertedTypeMap;
    }

    public static DataSetDaoType from(DataSetsTypes type) {
        switch (type) {
            case CONTACTS:
                return CONTACT;
            case ELEMENTARY_FLOWS:
                return ELEMENTARY_FLOW;
            case FLOW_PROPERTIES:
                return FLOW_PROPERTY;
            case LCIA_METHODS:
                return LCIA_METHOD;
            case LIFECYCLEMODELS:
                return LIFE_CYCLE_MODEL;
            case PROCESSES:
                return PROCESS;
            case PRODUCT_FLOWS:
                return PRODUCT_FLOW;
            case SOURCES:
                return SOURCE;
            case UNIT_GROUPS:
                return UNIT_GROUP;
            default:
                return null;
        }
    }

    public static List<DataSetDaoType> from(GlobalReferenceTypeValue grtTypeValue) {
        switch (grtTypeValue) {
            case CONTACT_DATA_SET:
                return List.of(CONTACT);
            case FLOW_DATA_SET:
                return List.of(ELEMENTARY_FLOW, PRODUCT_FLOW);
            case FLOW_PROPERTY_DATA_SET:
                return List.of(FLOW_PROPERTY);
            case LCIA_METHOD_DATA_SET:
                return List.of(LCIA_METHOD);
            case LIFE_CYCLE_DATA_SET:
                return List.of(LIFE_CYCLE_MODEL);
            case PROCESS_DATA_SET:
                return List.of(PROCESS);
            case SOURCE_DATA_SET:
                return List.of(SOURCE);
            case UNIT_GROUP_DATA_SET:
                return List.of(UNIT_GROUP);
            default:
                return List.of();
        }
    }

    /**
     * We define a specific order in which processing large collections of data sets of different
     * types is most robust.<br/><br/>
     * E.g.<br/>
     * Many data sets point to unit groups, so in some cases modifications that need to be persisted are
     * leading to less optimistic-locking exceptions and such when unit groups are processed first.
     *
     * @return comparator that orders the dao types by how often the corresponding data set types are referenced
     * -- highly referenced types first.
     */
    public static Comparator<DataSetDaoType> getComparatorForProcessingOrder() {
        Map<DataSetDaoType, Integer> priorityMap = new HashMap<>();

        priorityMap.put(DataSetDaoType.UNIT_GROUP, 0);
        priorityMap.put(DataSetDaoType.CONTACT, 1);
        priorityMap.put(DataSetDaoType.SOURCE, 2);
        priorityMap.put(DataSetDaoType.FLOW_PROPERTY, 3);
        priorityMap.put(DataSetDaoType.ELEMENTARY_FLOW, 4);
        priorityMap.put(DataSetDaoType.PRODUCT_FLOW, 5);
        priorityMap.put(DataSetDaoType.LCIA_METHOD, 6);
        priorityMap.put(DataSetDaoType.PROCESS, 7);
        priorityMap.put(DataSetDaoType.LIFE_CYCLE_MODEL, 8);

        return (r, s) -> {

            if (r == null && s == null)
                return 0;
            else if (r == null)
                return -1;
            else if (s == null)
                return 1;
            else
                return priorityMap.get(r).compareTo(priorityMap.get(s));
        };

    }

    public static Iterator<DataSetDaoType> iterator(Comparator<DataSetDaoType> comparator) {
        if (comparator == null)
            comparator = getComparatorForProcessingOrder();
        TreeSet<DataSetDaoType> sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(List.of(DataSetDaoType.values()));
        return sortedSet.iterator();
    }

    public DataSetDao<?, ?, ?> getDao() {
        return this.dao;
    }

    private DataSetType getDsType() {
        return this.dsType;
    }

    public Class<? extends DataSet> getDatasetClass() {
        return datasetClass;
    }
}
