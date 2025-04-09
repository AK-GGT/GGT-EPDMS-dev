package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.process.Exchange;

import java.util.Comparator;

public class LCIIndicatorNaturalComparator implements Comparator<Exchange> {

    @Override
    public int compare(Exchange o1, Exchange o2) {

        String name1 = o1.getFlowReference().getShortDescription().getDefaultValue();
        String name2 = o2.getFlowReference().getShortDescription().getDefaultValue();

        return name1.compareTo(name2);
    }
}
