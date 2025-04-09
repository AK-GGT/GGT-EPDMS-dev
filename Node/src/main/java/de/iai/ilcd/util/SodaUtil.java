package de.iai.ilcd.util;

import de.fzk.iai.ilcd.api.app.common.IMultiLang;
import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.common.IClassification;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.common.Classification;
import de.iai.ilcd.model.process.Amount;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserGroup;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DualListModel;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Common utility methods and constants
 */
public final class SodaUtil {

    /**
     * Constant for menu highlighting switch for home / index
     */
    public final static String MENU_HOME = "home";

    /**
     * Constant for menu highlighting switch for process
     */
    public final static String MENU_PROCESS = "proc";

    /**
     * Constant for menu highlighting switch for LCIA method
     */
    public final static String MENU_LCIAMETHOD = "lciam";

    /**
     * Constant for menu highlighting switch for product flow
     */
    public final static String MENU_PRODUCT_FLOW = "pflow";

    /**
     * Constant for menu highlighting switch for elementary flow
     */
    public final static String MENU_ELEMENTARY_FLOW = "eflow";

    /**
     * Constant for menu highlighting switch for flow property
     */
    public final static String MENU_FLOWPROPERTY = "fp";

    /**
     * Constant for menu highlighting switch for unit group
     */
    public final static String MENU_UNITGROUP = "ug";

    /**
     * Constant for menu highlighting switch for source
     */
    public final static String MENU_SOURCE = "src";

    /**
     * Constant for menu highlighting switch for contact
     */
    public final static String MENU_CONTACT = "con";

    /**
     * Constant for menu highlighting switch for lifecyclemodel
     */
    public final static String MENU_LIFECYCLEMODEL = "lcm";

    /**
     * Constant for menu highlighting switch for process search
     */
    public final static String MENU_SEARCHPROCESS = "sproc";

    /**
     * Constant for menu highlighting switch for process search
     */
    public final static String MENU_UPLOAD = "upload";

    /**
     * ID of the built-in admin user (super admin) (ID: 1)
     */
    public final static Long ADMIN_ID = 1L;

    /**
     * ID of the built-in default organization (ID: 1)
     */
    public final static Long DEFAULT_ORGANIZATION_ID = 1L;

    /**
     * ID of the built-in default root stock (ID: 1)
     */
    public final static Long DEFAULT_ROOTSTOCK_ID = 1L;

    /**
     * File extensions for image file types (see import of source data set)
     */
    private final static List<String> IMAGE_EXTENSIONS = Arrays.asList(new String[]{"bmp", "gif", "jpeg", "jpg", "png", "tif", "tiff"});

    /**
     * File extensions for valid file type including images (see import of source data set)
     */
    @SuppressWarnings("unchecked")
    private final static List<String> VALID_EXTENSIONS = ListUtils.union(SodaUtil.IMAGE_EXTENSIONS, Arrays.asList(new String[]{"doc", "docx", "pdf"}));

    private static Logger log = LogManager.getLogger(SodaUtil.class);

    /**
     * Checks if given filename has image extension
     *
     * @param fileName filename to check for image extension
     * @return <code>true</code> if given file name has image extension, else <code>false</code>
     * @see FilenameUtils#isExtension(String, java.util.Collection)
     */
    public static boolean hasImageExtension(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return FilenameUtils.isExtension(fileName.toLowerCase(), SodaUtil.IMAGE_EXTENSIONS);
        }
        return false;
    }

    /**
     * Checks if given filename has valid extension
     *
     * @param fileName filename to check for valid extension
     * @return <code>true</code> if given file name has valid extension, else <code>false</code>
     * @see FilenameUtils#isExtension(String, java.util.Collection)
     */
    public static boolean hasValidExtension(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return FilenameUtils.isExtension(fileName.toLowerCase(), SodaUtil.VALID_EXTENSIONS);
        }
        return false;
    }

    /**
     * Replace all occurrences of a String within another String ({@link StringUtils#replace(String, String, String)})
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replace(String text, String searchString, String replacement) {
        return StringUtils.replace(text, searchString, replacement);
    }

    /**
     * Determine if the total count of elements (count source + count target) is equal
     * for both provided {@link DualListModel}s
     *
     * @param <T> type of list model entries
     * @param l1  first {@link DualListModel}
     * @param l2  second {@link DualListModel}
     * @return <code>true</code> if total count of elements is equal, <code>false</code> otherwise
     */
    public static <T> boolean isTotalElementCountEqual(DualListModel<T> l1, DualListModel<T> l2) {
        int cntL1 = 0;
        int cntL2 = 0;
        if (l1 != null) {
            cntL1 = l1.getSource().size() + l1.getTarget().size();
        }
        if (l2 != null) {
            cntL2 = l2.getSource().size() + l2.getTarget().size();
        }
        return cntL1 == cntL2;
    }

    /**
     * Print thread info (calling method + thread name/id shown)
     */
    public static void threadInfo() {
        final Thread t = Thread.currentThread();
        StackTraceElement[] elements = t.getStackTrace();
        for (StackTraceElement e : elements) {
            if (!e.getClassName().equals(SodaUtil.class.getName()) && !e.getClassName().equals(Thread.class.getName())) {
                System.out.println(e.getClassName() + "#" + e.getMethodName() + "(..); Thread: " + t.getName() + " / id=" + t.getId() + " (" + e.getFileName()
                        + ":" + e.getLineNumber() + ")");
                return;
            }
        }
    }

    /**
     * Sysout with thread info and calling method
     *
     * @param s string to print
     */
    public static void sysout(String s) {
        final Thread t = Thread.currentThread();
        StackTraceElement[] elements = t.getStackTrace();
        for (StackTraceElement e : elements) {
            if (!e.getClassName().equals(SodaUtil.class.getName()) && !e.getClassName().equals(Thread.class.getName())) {
                System.out.println(s + "\n\t" + e.getClassName() + "#" + e.getMethodName() + "(..); Thread: " + t.getName() + " / id=" + t.getId() + " ("
                        + e.getFileName() + ":" + e.getLineNumber() + ")\n");
                return;
            }
        }
    }

    /**
     * Helper method to print a language-aware hint, if the given group is an admin group
     *
     * @param g  the group to check for admin group
     * @param o  the organization to check with
     * @param rb ResourceBundle
     * @return String containing <i>(Admin-Gruppe)</i> or <i>(Admin group)</i>
     */
    public static String adminHint(UserGroup g, Organization o, ResourceBundle rb) {
        if (o.hasAdminGroup()) {
            return (g.getId() == o.getAdminGroup().getId() ? " (" + rb.getString("admin.org.adminGroup") + ")" : "");
        } else {
            return "";
        }
    }

    /**
     * Helper method to print a language-aware hint, if the given user is an admin user
     *
     * @param u  the user to check for admin user
     * @param o  the organization to check with
     * @param rb ResourceBundle
     * @return String containing <i>(Admin-Benutzer)</i> or <i>(Admin user)</i>
     */
    public static String adminHint(User u, Organization o, ResourceBundle rb) {
        if (o.hasAdmin()) {
            return (u.getId() == o.getAdminUser().getId() ? " (" + rb.getString("admin.org.adminUser") + ")" : "");
        } else {
            return "";
        }
    }

    /**
     * Helper method to print a language-aware hint, if the given group has no users
     *
     * @param g  the group to check for users
     * @param rb ResourceBundle
     * @return String containing <i>(0 Benutzer)</i> or <i>(0 User)</i>
     */
    public static String noUsersHint(UserGroup g, ResourceBundle rb) {
        return (!g.hasUsers() ? " (0 " + rb.getString("admin.user") + ")" : "");
    }

    /**
     * Helper method to print a language-aware hint for EPD scenario groups
     *
     * @param group the EPD scenario group
     * @param rb    ResourceBundle
     * @return String containing EPD scenario group
     */
    public static String groupHint(String group, ResourceBundle rb) {
        return StringUtils.isNotBlank(group) ? " " + rb.getString("public.group") + " " + group : "";
    }

    /**
     * Encodes the specified string.
     *
     * @param plain the string to be encoded
     * @return the encoded string
     */
    public static String encodeURLComponent(String plain) {
        if (plain == null) {
            return null;
        }
        try {
            return URLEncoder.encode(plain, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes the specified string.
     *
     * @param encoded the string to be decoded
     * @return the decoded string
     */
    public static String decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the specified <code>subject</code> is contained in the provided <code>collection</code>. <br />
     * Comparison is being performed with provided <code>comparator</code>.
     *
     * @param collection the collection
     * @param comparator comparator
     * @param subject    the subject to find
     * @param <E>        type of collection
     * @return <code>true</code> if <code>subject</code> is contained, <code>false</code> otherwise (including
     * <code>null</code> for <code>collection</code>)
     * @throws NullPointerException if <code>comparator</code> is <code>null</code>
     */
    public static <E> boolean contains(Collection<E> collection, Comparator<E> comparator, E subject) {
        if (collection == null) {
            return false;
        }
        Iterator<E> it = collection.iterator();
        if (subject == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (comparator.compare(it.next(), subject) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get an amount filtered by module
     *
     * @param amounts list of amounts
     * @param module  filter module
     * @return amount filtered by module, null otherwise
     */
    public static Amount getAmountByModule(List<Amount> amounts, String module) {
        for (final Amount a : amounts) {
            // no null checks here because a.getModule() cannot be null and module (String) is checked in
            // de.iai.ilcd.model.process.Process.getDeclaredModulesForExchanges()/getDeclaredModulesForLciaResults()
            if (a.getModule().equals(module)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Get an amount filtered by module and scenario
     *
     * @param amounts  list of amounts
     * @param module   filter module
     * @param scenario filter scenario
     * @return amount filtered by module and scenario, null otherwise
     */
    public static Amount getAmountByModuleScenario(List<Amount> amounts, String module, String scenario) {
        for (final Amount a : amounts) {
            // no null checks here because a.getModule() cannot be null and module (String) is checked in
            // de.iai.ilcd.model .process.Process.getDeclaredModulesForExchanges()/
            // getDeclaredModulesScenariosForExchanges/getDeclaredModulesForLciaResults()
            // and scenario cannot be null
            try {
                if (a.getModule().equals(module) && a.getScenario().equals(scenario)) {
                    return a;
                }
            } catch (NullPointerException e) {
            }
        }
        return null;
    }

    /**
     * Get value for provided language with fall back to default
     *
     * @param lang desired language
     * @param lStr language string
     * @return value for provided language (or fall back)
     */
    public static ILString getLStringWithFallback(String lang, IMultiLangString lStr) {
        ILString fallback = null;
        final String defaultLang = ConfigurationService.INSTANCE.getDefaultLanguage();
        if (lStr != null && CollectionUtils.isNotEmpty(lStr.getLStrings())) {
            for (ILString ml : lStr.getLStrings()) {
                if (StringUtils.equals(ml.getLang(), lang)) {
                    return ml;
                }
                if (StringUtils.equals(ml.getLang(), defaultLang)) {
                    fallback = ml;
                }
            }
            if (fallback == null) {
                fallback = lStr.getLStrings().get(0);
            }
        }
        return fallback;
    }

    /**
     * Get string value for provided language with fall back to default
     *
     * @param lang desired language
     * @param lStr language string
     * @return value for provided language (or fall back)
     */
    public static String getLStringValueWithFallback(String lang, IMultiLangString lStr) {
        ILString foo = SodaUtil.getLStringWithFallback(lang, lStr);
        if (foo != null) {
            return foo.getValue();
        }
        return null;
    }

    /**
     * Get value for provided language with fall back to default for XML API types
     *
     * @param lang       desired language
     * @param multilangs list of {@link IMultiLang}
     * @return value for provided language (or fall back)
     */
    public static IMultiLang getMultilangWithFallback(String lang, List<? extends IMultiLang> multilangs) {
        IMultiLang fallback = null;
        final String defaultLang = ConfigurationService.INSTANCE.getDefaultLanguage();
        if (CollectionUtils.isNotEmpty(multilangs)) {
            for (IMultiLang ml : multilangs) {
                if (StringUtils.equals(ml.getLang(), lang)) {
                    return ml;
                }
                if (StringUtils.equals(ml.getLang(), defaultLang)) {
                    fallback = ml;
                }
            }
            if (fallback == null) {
                fallback = multilangs.get(0);
            }
        }
        return fallback;
    }

    /**
     * Helper method to build key for accessing property file value
     *
     * @param matPropDefName the original material property name as read
     * @return the prefixed key
     */
    public static String getValueFromMatPropKey(String matPropDefName) {
        if (StringUtils.isNotBlank(matPropDefName)) {
            // replace whitespaces inside key with underscore, afterwards add prefix "matmlPropName."
            // i. e. gross density ==> matmlPropName.gross_density
            String result = "datasetdetail.epd.matmlPropName." + matPropDefName.replaceAll("\\s+", "_");
            log.trace("getting material property i18n key for {}, result is {}", matPropDefName, result);
            return result;
        }
        log.trace("given material property key is blank");
        return matPropDefName;
    }

    /**
     * Get the highest class (highest level) of a classification
     *
     * @param classification classification to get highest class from
     * @return highest class, or <code>null</code> if classification was <code>null</code>
     */
    public static ClClass getHighestClClass(Classification classification) {
        if (classification != null && CollectionUtils.isNotEmpty(classification.getClasses())) {
            ClClass highestClass = classification.getClasses().get(0);
            for (ClClass cl : classification.getClasses()) {
                if (cl.getLevel() > highestClass.getLevel()) {
                    highestClass = cl;
                }
            }
            return highestClass;
        }
        return null;
    }

    /**
     * Get the highest class (highest level) of a classification
     *
     * @param classification classification to get highest class from
     * @return highest class, or <code>null</code> if classification was <code>null</code>
     */
    public static IClass getHighestClass(IClassification classification) {
        if (classification != null && CollectionUtils.isNotEmpty(classification.getIClassList())) {
            IClass highestClass = classification.getIClassList().get(0);
            for (IClass cl : classification.getIClassList()) {
                if (cl.getLevel() > highestClass.getLevel()) {
                    highestClass = cl;
                }
            }
            return highestClass;
        }
        return null;
    }

    /**
     * Get the Id of the highest class (highest level) of a classification
     *
     * @param classification classification to get highest class from
     * @return id of the highest class, or <code>null</code> if classification was <code>null</code>
     */
    public static String getHighestClassId(IClassification classification) {
        IClass highestClass = getHighestClass(classification);
        if (highestClass != null)
            return highestClass.getClId();
        else
            return null;
    }

}
