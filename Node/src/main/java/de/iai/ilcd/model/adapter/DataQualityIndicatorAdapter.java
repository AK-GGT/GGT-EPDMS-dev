package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.DataQualityIndicatorType;
import de.fzk.iai.ilcd.service.model.process.IDataQualityIndicator;

import java.util.Set;

/**
 * Adapter for {@link IDataQualityIndicator}
 */
public class DataQualityIndicatorAdapter extends DataQualityIndicatorType {

    /**
     * Create the adapter
     *
     * @param adaptee object to adapt
     */
    public DataQualityIndicatorAdapter(IDataQualityIndicator adaptee) {
        this.setName(adaptee.getName());
        this.setValue(adaptee.getValue());
    }

    /**
     * Copy quality indicator via adapter
     *
     * @param src source set
     * @param dst destination set
     */
    public static void copyDataQualityIndicators(Set<IDataQualityIndicator> src, Set<IDataQualityIndicator> dst) {
        if (src != null && dst != null) {
            for (IDataQualityIndicator srcItem : src) {
                dst.add(new DataQualityIndicatorAdapter(srcItem));
            }
        }
    }

}
