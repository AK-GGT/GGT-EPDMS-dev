package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.flow.FlowPropertyDescription;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Comparator;

public class CarbogenicsComparator implements Comparator<FlowPropertyDescription> {

    public static final String[] order = {"62e503ce-544a-4599-b2ad-bcea15a7bf20", "262a541b-209e-44cc-a426-33bce30de7b1"};

    public CarbogenicsComparator() {
        this.comp = new FixedOrderComparator<>(order);
        this.comp.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
    }

    private final FixedOrderComparator<String> comp;

    @Override
    public int compare(FlowPropertyDescription o1, FlowPropertyDescription o2) {

        String uuid1;
        String uuid2;
        try {
            uuid1 = o1.getFlowPropertyRef().getRefObjectId().trim().toLowerCase();
            uuid2 = o2.getFlowPropertyRef().getRefObjectId().trim().toLowerCase();
        } catch (NullPointerException npe) {
            return ObjectUtils.compare(o1.toString(), o2.toString());
        }

        return comp.compare(uuid1, uuid2);
    }
}
