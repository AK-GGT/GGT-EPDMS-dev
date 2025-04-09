package de.iai.ilcd.model.utils;

import de.fzk.iai.ilcd.service.model.IProcessListVO;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;

public class ProcessComparator<T extends IProcessListVO> implements Comparator<IProcessListVO> {

    private final Logger logger = LogManager.getLogger(ProcessComparator.class);

    private String sortCriterium;

    private boolean sortOrder = true;

    private String language = ConfigurationService.INSTANCE.getDefaultLanguage();

    public ProcessComparator(String sortCriterium, boolean sortOrder) {
        this.sortCriterium = sortCriterium;
        this.sortOrder = sortOrder;
    }

    public ProcessComparator(String sortCriterium, boolean sortOrder, String language) {
        this.sortCriterium = sortCriterium;
        this.sortOrder = sortOrder;
        if (language != null)
            this.language = language;
    }

    @Override
    public int compare(IProcessListVO o1, IProcessListVO o2) {
        if (!sortOrder) {
            IProcessListVO tmp = o1;
            o1 = o2;
            o2 = tmp;
        }
        try {
            if (this.sortCriterium == null)
                return compareName(o1, o2);

            switch (this.sortCriterium) {
                case "name.getValueWithFallback(lang)":
                case "name":
                    return compareName(o1, o2);
                case "type.value":
                case "type":
                    return compareType(o1, o2);
                case "location":
                    return ObjectUtils.compare(o1.getLocation(), o2.getLocation());
                case "classification.classHierarchyAsString":
                case "classes":
                    return compareClasses(o1, o2);
                case "timeInformation.referenceYear":
                case "referenceYear":
                    return compareReferenceYear(o1, o2);
                case "timeInformation.validUntil":
                case "validUntil":
                    return compareValidUntil(o1, o2);
                case "sourceId":
                    return ObjectUtils.compare(o1.getSourceId(), o2.getSourceId());
                case "subType":
                    return compareSubType(o1, o2);
                case "owner":
                    return compareOwner(o1, o2);
                default:
                    return compareName(o1, o2);
            }
        } catch (Exception e) {
            logger.error("error comparing properties for sort key " + this.sortCriterium, e);
        }
        return ObjectUtils.compare(o1.toString(), o2.toString());
    }

    private int compareOwner(IProcessListVO o1, IProcessListVO o2) {
        String s1 = null;
        String s2 = null;
        try {
            s1 = o1.getOwnerReference().getShortDescription().getValueWithFallback(language);
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getOwnerReference().getShortDescription().getValueWithFallback(language);
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareSubType(IProcessListVO o1, IProcessListVO o2) {
        String s1 = null;
        String s2 = null;
        try {
            s1 = o1.getSubType().value();
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getSubType().value();
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareValidUntil(IProcessListVO o1, IProcessListVO o2) {
        Integer s1 = null;
        Integer s2 = null;
        try {
            s1 = o1.getTimeInformation().getValidUntil();
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getTimeInformation().getValidUntil();
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareReferenceYear(IProcessListVO o1, IProcessListVO o2) {
        Integer s1 = null;
        Integer s2 = null;
        try {
            s1 = o1.getTimeInformation().getReferenceYear();
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getTimeInformation().getReferenceYear();
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareClasses(IProcessListVO o1, IProcessListVO o2) {
        String s1 = null;
        String s2 = null;
        try {
            s1 = o1.getClassification().getClassHierarchyAsString();
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getClassification().getClassHierarchyAsString();
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareType(IProcessListVO o1, IProcessListVO o2) {
        String s1 = null;
        String s2 = null;
        try {
            s1 = o1.getType().getValue();
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getType().getValue();
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

    private int compareName(IProcessListVO o1, IProcessListVO o2) {
        String s1 = null;
        String s2 = null;
        try {
            s1 = o1.getName().getValueWithFallback(language);
        } catch (NullPointerException e) {
        }
        try {
            s2 = o2.getName().getValueWithFallback(language);
        } catch (NullPointerException e) {
        }
        return ObjectUtils.compare(s1, s2);
    }

}
