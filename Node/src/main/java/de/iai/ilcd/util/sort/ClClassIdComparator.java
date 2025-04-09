package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.common.ClClass;

import java.util.Comparator;

public class ClClassIdComparator implements Comparator<ClClass> {

    private NaturalOrderComparator noc = new NaturalOrderComparator();

    public int compare(ClClass arg0, ClClass arg1) {
        if (arg0 == null || arg1 == null)
            return 0;
        if (arg0.getClId() == null) {
            if (arg1.getClId() == null)
                return 0;
            else
                return +1;
        }
        if (arg1.getClId() == null) {
            if (arg0.getClId() == null)
                return 0;
            else
                return -1;
        }
        return noc.compare(arg0.getClId(), arg1.getClId());
    }

}
