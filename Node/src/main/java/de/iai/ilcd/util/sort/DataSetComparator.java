package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.common.DataSet;

import java.util.Comparator;

/**
 * The comparator class for comparing {@link DataSet}.
 *
 * @author sarai
 */
public class DataSetComparator implements Comparator<DataSet> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(DataSet ds1, DataSet ds2) {
        // return
        // ds2.getDataSetType().ordinal().compareTo(ds1.getDataSetType().ordinal());
        return ds2.getDataSetType().compareTo(ds1.getDataSetType());
    }

}
