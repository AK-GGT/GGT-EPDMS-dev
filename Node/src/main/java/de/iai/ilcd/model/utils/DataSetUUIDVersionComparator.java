package de.iai.ilcd.model.utils;

import de.iai.ilcd.model.common.DataSet;

import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

public class DataSetUUIDVersionComparator<T extends DataSet> implements Comparator<DataSet> {

    // default is descending order (newest versions first)
    private boolean sortOrder = false;

    public DataSetUUIDVersionComparator(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    public DataSetUUIDVersionComparator() {
    }

    @Override
    public int compare(DataSet o1, DataSet o2) {
        if (!sortOrder) {
            DataSet tmp = o1;
            o1 = o2;
            o2 = tmp;
        }
        try {
            if (!o1.getUuidAsString().equalsIgnoreCase(o2.getUuidAsString()))
                return o1.getUuidAsString().compareTo(o2.getUuidAsString());
            return Objects.compare(o1.getDataSetVersion(), o2.getDataSetVersion(), Collator.getInstance());
        } catch (Exception e) {
        }
        return o1.toString().compareTo(o2.toString());
    }

}