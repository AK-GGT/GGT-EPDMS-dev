package de.iai.ilcd.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Case insensitive file name filter
 */
public class CaseInsensitiveFilenameFilter implements FilenameFilter {

    /**
     * The name to match case insensitive
     */
    private final String nameToMatch;

    /**
     * Create the case insensitive file name filter
     *
     * @param nameToMatch the name to match case insensitive
     */
    public CaseInsensitiveFilenameFilter(String nameToMatch) {
        this.nameToMatch = nameToMatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(File dir, String name) {
        return StringUtils.equalsIgnoreCase(this.nameToMatch, name);
    }

}
