package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.api.app.contact.ContactDataSet;
import de.fzk.iai.ilcd.api.app.flow.FlowDataSet;
import de.fzk.iai.ilcd.api.app.lciamethod.LCIAMethodDataSet;
import de.fzk.iai.ilcd.api.app.lifecyclemodel.LifeCycleModelDataSet;
import de.fzk.iai.ilcd.api.app.process.ProcessDataSet;
import de.fzk.iai.ilcd.api.app.source.SourceDataSet;
import de.fzk.iai.ilcd.api.app.unitgroup.UnitGroupDataSet;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.api.app.flowproperty.FlowPropertyDataSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author clemens.duepmeier
 */
public enum DataSetType {
    PROCESS("process data set", "processes"),
    LCIAMETHOD("lcia method data set", "lciamethods"),
    FLOW("flow data set", "flows"),
    FLOWPROPERTY("flow property data set", "flowproperties"),
    UNITGROUP("unit group data set", "unitgroups"),
    SOURCE("source data set", "sources"),
    CONTACT("contact data set", "contacts"),
    LIFECYCLEMODEL("lifecyclemodel data set", "lifecyclemodels");

    final private String value;

    final private String standardFolderName;

    DataSetType(String value, String standardFolderName) {
        this.value = value;
        this.standardFolderName = standardFolderName;
    }

    public static DataSetType fromValue(String value) {
        for (DataSetType enumValue : DataSetType.values()) {
            if (enumValue.getValue().equals(value)) {
                return enumValue;
            }
        }
        return null;
    }

    /**
     * We define a specific order in which processing large collections of data sets of different
     * types is most robust.<br/><br/>
     * E.g.<br/>
     * Many data sets point to unit groups, so in some cases modifications that need to be persisted are
     * leading to less optimistic-locking exceptions and such when unit groups are processed first.
     *
     * @return comparator that orders the data set types by how often the corresponding data set types are
     * referenced -- highly referenced types first.
     */
    public static Comparator<DataSetType> getComparatorForProcessingOrder() {
        Map<DataSetType, Integer> priorityMap = new HashMap<>();

        priorityMap.put(DataSetType.CONTACT, 0);
        priorityMap.put(DataSetType.SOURCE, 1);
        priorityMap.put(DataSetType.UNITGROUP, 2);
        priorityMap.put(DataSetType.FLOWPROPERTY, 3);
        priorityMap.put(DataSetType.FLOW, 4);
        priorityMap.put(DataSetType.LCIAMETHOD, 5);
        priorityMap.put(DataSetType.PROCESS, 6);
        priorityMap.put(DataSetType.LIFECYCLEMODEL, 7);

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

    public String getValue() {
        return this.value;
    }

    public String getStandardFolderName() {
        return this.standardFolderName;
    }

    public ILCDTypes getILCDType() {
        switch (this) {
            case PROCESS:
                return ILCDTypes.PROCESS;
            case FLOW:
                return ILCDTypes.FLOW;
            case CONTACT:
                return ILCDTypes.CONTACT;
            case SOURCE:
                return ILCDTypes.SOURCE;
            case LCIAMETHOD:
                return ILCDTypes.LCIAMETHOD;
            case FLOWPROPERTY:
                return ILCDTypes.FLOWPROPERTY;
            case UNITGROUP:
                return ILCDTypes.UNITGROUP;
            case LIFECYCLEMODEL:
                return ILCDTypes.LIFECYCLEMODEL;
            default:
                return null;
        }
    }

    public Class<?> getILCDClass() {
        switch (this) {
            case PROCESS:
                return ProcessDataSet.class;
            case FLOW:
                return FlowDataSet.class;
            case CONTACT:
                return ContactDataSet.class;
            case SOURCE:
                return SourceDataSet.class;
            case LCIAMETHOD:
                return LCIAMethodDataSet.class;
            case FLOWPROPERTY:
                return FlowPropertyDataSet.class;
            case UNITGROUP:
                return UnitGroupDataSet.class;
            case LIFECYCLEMODEL:
                return LifeCycleModelDataSet.class;
            default:
                return null;
        }
    }

}