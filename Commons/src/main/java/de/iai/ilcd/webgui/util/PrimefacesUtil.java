package de.iai.ilcd.webgui.util;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PrimefacesUtil {

    /**
     * This method converts the modern data to legacy data.
     * <p>
     * <i>Sorting for multiple fields at once is impossible with legacy data.</i>
     * We consider only the first non-null Entry in the sortMeta map.
     * </p>
     *
     * @param sortMeta
     * @return Map {<br/>
     * "sortField"=(String) fieldName,<br/>
     * "sortOrder"=(org.primefaces.model.SortOrder) sortOrder<br/>
     * }
     */
    public static Map<String, Object> getLegacySortData(Map<String, SortMeta> sortMeta) {
        Map<String, Object> data = new HashMap<String, Object>();
        // get the first map-entry's sortMeta
        Optional<SortMeta> firstNonNull = sortMeta.values().stream().filter(meta -> meta != null).findFirst();
        if (!firstNonNull.isPresent())
            return data; // empty map
        // extract legacy data
        data.put("sortField", firstNonNull.get().getSortField());
        data.put("sortOrder", firstNonNull.get().getSortOrder());
        return data;
    }

    /**
     * This method converts the primefaces Map<String, FilterMeta> that is used in
     * lazy-loading, to a more straight-forward Map<String, Object>, which was used
     * prior to primefaces 8.
     *
     * @param filterMeta
     * @return Map with filter entries of the form { (String) fieldName = (Object)
     * objectToMatch }
     */
    public static Map<String, Object> getLegacyFilterData(Map<String, FilterMeta> filterMeta) {
        Map<String, Object> filters = new HashMap<String, Object>();
        for (FilterMeta fm : filterMeta.values()) {
            filters.put(fm.getFilterField(), fm.getFilterValue());
        }
        return filters;
    }
}
