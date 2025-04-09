package de.iai.ilcd.model.utils;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;

import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

public class DataSourceGlobalReferenceComparator extends GlobalReferenceComparator implements Comparator<IGlobalReference> {

    @Override
    public int compare(IGlobalReference o1, IGlobalReference o2) {
        // the source references representing all versions of the database always get precedence
        if ("b497a91f-e14b-4b69-8f28-f50eb1576766".equals(o1.getRefObjectId()))
            return -100;
        else if ("28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a".equals(o1.getRefObjectId()))
            return -100;
        else if ("b497a91f-e14b-4b69-8f28-f50eb1576766".equals(o2.getRefObjectId()))
            return 100;
        else if ("28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a".equals(o2.getRefObjectId()))
            return 100;
        try {
            return Objects.compare(o1.getShortDescription().getValueWithFallback(lang), o2.getShortDescription().getValueWithFallback(lang), Collator.getInstance());
        } catch (NullPointerException e) {
        }
        return o1.toString().compareTo(o2.toString());
    }


}
