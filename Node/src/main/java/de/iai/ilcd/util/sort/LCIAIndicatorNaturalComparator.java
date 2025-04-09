package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.process.LciaResult;

import java.util.Comparator;

public class LCIAIndicatorNaturalComparator implements Comparator<LciaResult> {

    @Override
    public int compare(LciaResult o1, LciaResult o2) {

        String name1 = o1.getMethodReference().getShortDescription().getDefaultValue();
        String name2 = o2.getMethodReference().getShortDescription().getDefaultValue();

        return name1.compareTo(name2);
    }
}
