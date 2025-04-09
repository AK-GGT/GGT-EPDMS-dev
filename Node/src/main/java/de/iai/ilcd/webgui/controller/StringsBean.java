package de.iai.ilcd.webgui.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.el.LambdaExpression;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Application-scoped, stateless Bean, that gives access to various flexible
 * toString methods, which can then be utilized in the UI.
 * </p>
 *
 * <p>
 * E.g: Given a process we want to display its complianceSystems in a data table
 * in the UI. The easiest way to do so would be to map the Collection of entities
 * onto a single String - e.g. with line breaks and bullets as separators,<br/>
 * which can be achieved by calling (from the frontend,
 * otherwise see {@link javax.el.LambdaExpression})
 *
 * <pre>
 * <code>#{stringify.toBulletList(dataset.complianceSystems, cs -> cs.name)}.</code>
 * </pre>
 * </p>
 */
@ManagedBean(name = "stringify")
@ApplicationScoped
public class StringsBean implements Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -1838197630178463814L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(StringsBean.class);

    /**
     * Alphabetical, case-insensitive comparator, that considers <code>null</code> a
     * minimal element.
     */
    private static final Comparator<String> DEFAULT_COMPARATOR = (s, t) -> {
        if (s == null && t == null)
            return 0;
        else if (s == null)
            return -1;
        else if (t == null)
            return 1;
        else
            return s.toLowerCase().compareTo(t.toLowerCase());
    };

    private static final String BULLET_PREFIX = "• ";

    private static final String LINE_SEPARATOR = System.lineSeparator();


    /**
     * Maps a collection of objects to a bulleted list. Duplicate strings, blanks
     * and null values are automatically removed. The list is alphabetically sorted
     * (case ignored).<br/>
     * Please note: If the provided object collection is null or empty a blank String is
     * returned.
     *
     * <p>
     * Example usage:<br/>
     * <code>#{stringify.toBulletList(dataset.complianceSystems, cs -> cs.name)}.</code><br/>
     * Example output:<br/>
     * <pre>
     * 		• DIN EN 15804+A2 (Overall compliance: Not defined)
     * 		• ISO 14025 (Overall compliance: Not defined)
     * 	</pre>
     * </p>
     *
     * @param objs         Collection of objects
     * @param toStringImpl <code>javax.el.LambdaExpression</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @return String that separates the entries by bullets and line breaks (based on line.separator system property)
     */
    public String toBulletList(Collection<Object> objs, LambdaExpression toStringImpl) {
        return toCustomList(BULLET_PREFIX, objs, toStringImpl, LINE_SEPARATOR + BULLET_PREFIX, "");
    }

    /**
     * Maps a collection of objects to a bulleted list with CRLF line breaks. Duplicate strings, blanks
     * and null values are automatically removed. The list is alphabetically sorted
     * (case ignored).<br/>
     * Please note: If the provided object collection is null or empty a blank String is
     * returned.
     *
     * <p>
     * Example usage:<br/>
     * <code>#{stringify.toBulletCRLFList(dataset.complianceSystems, cs -> cs.name)}.</code><br/>
     * Example output:<br/>
     * <pre>
     * 		• DIN EN 15804+A2 (Overall compliance: Not defined)
     * 		• ISO 14025 (Overall compliance: Not defined)
     * 	</pre>
     * </p>
     *
     * @param objs         Collection of objects
     * @param toStringImpl <code>javax.el.LambdaExpression</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @return String that separates the entries by bullets and line breaks (CRLF)
     */
    public String toBulletCRLFList(Collection<Object> objs, LambdaExpression toStringImpl) {
        return toCustomList(BULLET_PREFIX, objs, toStringImpl, "\r\n" + BULLET_PREFIX, "");
    }

    /**
     * Maps a collection of objects to a simple list with line breaks. Duplicate strings, blanks
     * and null values are automatically removed. The list is alphabetically sorted
     * (case ignored).<br/>
     * Please note: If the provided object collection is null or empty a blank String is
     * returned.
     *
     * <p>
     * Example usage:<br/>
     * <code>#{stringify.toSimpleList(dataset.complianceSystems, cs -> cs.name)}.</code><br/>
     * Example output:<br/>
     * <pre>
     * 		• DIN EN 15804+A2 (Overall compliance: Not defined)
     * 		• ISO 14025 (Overall compliance: Not defined)
     * 	</pre>
     * </p>
     *
     * @param objs         Collection of objects
     * @param toStringImpl <code>javax.el.LambdaExpression</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @return String that separates the entries by line breaks (based on line.separator system property)
     */
    public String toSimpleList(Collection<Object> objs, LambdaExpression toStringImpl) {
        return toCustomList("", objs, toStringImpl, LINE_SEPARATOR, "");
    }

    /**
     * Maps a collection of objects to a simple list with CRLF line breaks. Duplicate strings, blanks
     * and null values are automatically removed. The list is alphabetically sorted
     * (case ignored).<br/>
     * Please note: If the provided object collection is null or empty a blank String is
     * returned.
     *
     * <p>
     * Example usage:<br/>
     * <code>#{stringify.toSimpleCRLFList(dataset.complianceSystems, cs -> cs.name)}.</code><br/>
     * Example output:<br/>
     * <pre>
     * 		• DIN EN 15804+A2 (Overall compliance: Not defined)
     * 		• ISO 14025 (Overall compliance: Not defined)
     * 	</pre>
     * </p>
     *
     * @param objs         Collection of objects
     * @param toStringImpl <code>javax.el.LambdaExpression</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @return String that separates the entries by line breaks (CRLF)
     */
    public String toSimpleCRLFList(Collection<Object> objs, LambdaExpression toStringImpl) {
        return toCustomList("", objs, toStringImpl, "\r\n", "");
    }

    /**
     * Method to convert a collection of objects to a single string,
     * utilising the provided to toString implementation.
     *
     * <pre>
     * E.g We want to generate a bulleted list from two person objects
     * 	prefix = "### MY CUSTOM LIST ###" + LINE_SEPARATOR + BULLET_PREFIX
     * 	infix = LINE_SEPARATOR + BULLET_PREFIX
     * 	suffix = LINE_SEPARATOR + "   …"
     *
     * with objects
     * 	godel = {firstName = 'Kurt'; lastName = 'Goedel', favouriteSubject = 'Logic'}
     * 	noether = {firstName = 'Emmy'; lastName = 'Noether', favouriteSubject = 'Algebraic Geometry'}
     *
     * and
     * 	toStringImpl = #{p -> p.firstName + " " + p.lastName + ", " + favouriteSubject}
     *
     * will yield rendered output (order is alphabetical (case-insensitive)):
     *
     * 		### MY CUSTOM LIST ###
     * 		• Emmy Noether, Algebraic Geometry
     * 		• Kurt Goedel, Logic
     *        …
     * </pre>
     *
     * @param prefix       String that gets prepended,
     *                     e.g. "• "
     * @param objs         Collection of objects
     * @param toStringImpl <code>javax.el.LambdaExpression</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @param infix        Separator for the strings that represent different objects,
     *                     e.g. LINE_SEPARATOR + "• "
     * @param suffix       String that get appended,
     *                     e.g. LINE_SEPARATOR + "    …"
     * @return List representation as String
     */
    public String toCustomList(String prefix, Collection<Object> objs, LambdaExpression toStringImpl, String infix, String suffix) {

        // We resolve javax.el.LambdaExpression to java.util.Function<Object,String>
        // Note: We're actually just allowing the ELContext to resolve EL expresssions like
        // 'backingBean.contacts' (inlcuding all the casting and method references).
        Function<Object, String> toString;
        if (toStringImpl != null)
            toString = o -> String.valueOf(toStringImpl.invoke(FacesContext.getCurrentInstance().getELContext(), o));
        else
            //noinspection Convert2MethodRef
            toString = o -> String.valueOf(o);

        return toCustomList(prefix, objs, toString, infix, suffix);
    }

    /**
     * Method to convert a collection of objects to a single string,
     * utilising the provided to toString implementation.
     *
     * <pre>
     * E.g We want to generate a bulleted list from two person objects
     * 	prefix = "### MY CUSTOM LIST ###" + LINE_SEPARATOR + BULLET_PREFIX
     * 	infix = LINE_SEPARATOR + BULLET_PREFIX
     * 	suffix = LINE_SEPARATOR + "   …"
     *
     * with objects
     * 	godel = {firstName = 'Kurt'; lastName = 'Goedel', favouriteSubject = 'Logic'}
     * 	noether = {firstName = 'Emmy'; lastName = 'Noether', favouriteSubject = 'Algebraic Geometry'}
     *
     * and
     * 	toStringImpl = p -> p.firstName + " " + p.lastName + ", " + favouriteSubject
     *
     * will yield rendered output (order is alphabetical (case-insensitive)):
     *
     * 		### MY CUSTOM LIST ###
     * 		• Emmy Noether, Algebraic Geometry
     * 		• Kurt Goedel, Logic
     *        …
     * </pre>
     *
     * @param prefix       String that gets prepended,
     *                     e.g. "• "
     * @param objs         Collection of objects
     * @param toStringImpl <code>java.util.function.Function</code> that
     *                     handles/overrides the toString mapping for the provided
     *                     objects.
     * @param infix        Separator for the strings that represent different objects,
     *                     e.g. LINE_SEPARATOR + "• "
     * @param suffix       String that get appended,
     *                     e.g. LINE_SEPARATOR + "    …"
     * @return List representation as String
     */
    private String toCustomList(String prefix, Collection<Object> objs, Function<Object, String> toStringImpl, String infix, String suffix) {

        if (objs == null || objs.isEmpty()) { // Nothing to do then. Let's return an empty string.
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(objs == null ? "Provided collection of objects was null." : "Provided obj collection was empty.");
            return "";
        }

        // Normalizing provided objects: apply provided toString Implementation and sort
        // (alphabetically) into a SortedSet (uniqueness)
        SortedSet<String> stringSet = objs.stream()
                .map(toStringImpl)
                .filter(s -> s != null
                        && !s.trim().isEmpty()
                        && !s.equals(toStringImpl.apply(null))
                )
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(DEFAULT_COMPARATOR) // unique + sorted
                ));

        // Now let's build the String
        if (stringSet.isEmpty())
            return ""; // Since it's gonna be used in the UI..

        // interpret null method parameters for prefix/infix/suffix as empty strings
        prefix = prefix == null ? "" : prefix;
        infix = infix == null ? "" : infix;
        suffix = suffix == null ? "" : suffix;

        // return e.g. "• Wood Panel/n• Toughened safety glass /n..."
        return prefix + String.join(infix, stringSet) + suffix;
    }

}