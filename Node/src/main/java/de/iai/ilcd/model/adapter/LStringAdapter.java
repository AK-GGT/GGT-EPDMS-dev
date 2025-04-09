package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.LString;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for localized strings
 */
public class LStringAdapter {

    /**
     * The localized strings
     */
    protected List<LString> lStrings = new ArrayList<LString>();

    /**
     * Create the adapter
     *
     * @param mls instance to adapt
     */
    public LStringAdapter(IMultiLangString mls) {
        if (mls != null) {
            for (ILString s : mls.getLStrings()) {
                lStrings.add(new LString(s.getLang(), s.getValue()));
            }
        }
    }

    /**
     * Create the adapter with language filtering
     *
     * @param mls instance to adapt
     */
    public LStringAdapter(IMultiLangString mls, String language) {
        this(mls, language, false);
    }

    /**
     * Create the adapter with language filtering
     *
     * @param mls instance to adapt
     */
    public LStringAdapter(IMultiLangString mls, String language, boolean langFallback) {
        if (mls != null) {
            for (ILString s : mls.getLStrings()) {
                if (s.getLang().equals(language))
                    lStrings.add(new LString(s.getLang(), s.getValue()));
            }
            if (mls.getLStrings().size() != 0 && lStrings.size() == 0 && langFallback && StringUtils.isNotBlank(mls.getValue("en"))) {
                lStrings.add(new LString("en", mls.getValue("en")));
            }
            if (mls.getLStrings().size() != 0 && lStrings.size() == 0 && langFallback) {
                lStrings.add(new LString(mls.getLStrings().get(0).getValue()));
            }
        }
    }

    /**
     * Copy all localized strings
     *
     * @param src source {@link IMultiLangString} instance
     * @param dst destination {@link IMultiLangString} instance
     */
    // public static void copyLStrings( IMultiLangString src, IMultiLangString
    // dst ) {
    // copyLStrings( src, dst, null );
    // }
    public static void copyLStrings(IMultiLangString src, IMultiLangString dst, String language) {
        copyLStrings(src, dst, language, false);
    }

    /**
     * Copy all localized strings, filtering for language
     *
     * @param src source {@link IMultiLangString} instance
     * @param dst destination {@link IMultiLangString} instance
     */
    public static void copyLStrings(IMultiLangString src, IMultiLangString dst, String language, boolean langFallback) {
        if (src != null && dst != null) {
            boolean found = false;
            for (ILString s : src.getLStrings()) {
                if (language == null) {
                    dst.setValue(s.getLang(), s.getValue());
                } else if (s.getLang().equals(language) && !found) {
                    dst.setValue(s.getLang(), s.getValue());
                    found = true;
                    break;
                }
            }
            if (langFallback && !found) {
                for (ILString s : src.getLStrings()) {
                    for (String lang : ConfigurationService.INSTANCE.getPreferredLanguages()) {
                        if (s.getLang().equals(lang)) {
                            dst.setValue(s.getLang(), s.getValue());
                            found = true;
                            break;
                        }
                    }
                    if (found)
                        break;
                }
            }
        }
    }

    /**
     * Get the localized strings
     *
     * @return localized strings
     */
    public List<LString> getLStrings() {
        return lStrings;
    }
}
