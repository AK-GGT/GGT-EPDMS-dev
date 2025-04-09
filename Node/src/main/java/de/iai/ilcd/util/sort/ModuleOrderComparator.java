package de.iai.ilcd.util.sort;

/*
ModuleOrderComparator.java -- Perform 'natural order' comparisons of modules strings.
2019 by Alhajras Algdairy <alhajras.algdairy@gmail.com>
*/

public class ModuleOrderComparator extends NaturalOrderComparator {

    // This method has been implemented to take care of the situation where we have
    // a range of of modules.
    // For example: [A1, C3, A4, B2, B5, A2, A1-A3, B4, C1, B1, A5, A3, B3, C4, C2]
    // Should become [A1, A2, A3, A1-A3, A4, A5, B1, B2, B3, B4, B5, C1, C2, C3, C4]
    // and not [A1, A1-A3, A2, A3, A4, A5, B1, B2, B3, B4, B5, C1, C2, C3, C4]
    public int compare(Object o1, Object o2) {

        if (o1 == null) {
            if (o2 == null)
                return 0;
            else
                return 1;
        } else if (o2 == null)
            return -1;

        String[] range1 = o1.toString().split("-");
        String[] range2 = o2.toString().split("-");

        if (range1.length == 2) {
            if (range1[1].equals(o2.toString()))
                return 1;
            return super.compare(range1[1], o2);
        } else if (range2.length == 2) {
            if (range2[1].equals(o1.toString()))
                return -1;
            return super.compare(o1, range2[1]);
        }
        return super.compare(o1, o2);
    }
}
