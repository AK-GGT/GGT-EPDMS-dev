package de.iai.ilcd.model.utils;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.iai.ilcd.configuration.ConfigurationService;

import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

public class GlobalReferenceComparator implements Comparator<IGlobalReference> {

    protected String lang = ConfigurationService.INSTANCE.getDefaultLanguage();

    public GlobalReferenceComparator() {
    }

    public GlobalReferenceComparator(String language) {
        this.lang = language;
    }

    @Override
    public int compare(IGlobalReference o1, IGlobalReference o2) {
        try {
            return Objects.compare(o1.getShortDescription().getValueWithFallback(lang), o2.getShortDescription().getValueWithFallback(lang), Collator.getInstance());
        } catch (NullPointerException e) {
        }
        return o1.toString().compareTo(o2.toString());
    }

}
