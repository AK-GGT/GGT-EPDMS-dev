package eu.europa.ec.jrc.lca.commons.view.model;

import de.iai.ilcd.webgui.util.PrimefacesUtil;
import eu.europa.ec.jrc.lca.commons.dao.SearchParameters;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;
import eu.europa.ec.jrc.lca.commons.service.LazyLoader;
import org.primefaces.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ILCDLazyDataModel<T extends ILongIdObject> extends LazyDataModel<T> implements SelectableDataModel<T> {

    public static final int DEFAULT_PAGE_SIZE = 10;
    private static final long serialVersionUID = 1688174744205570903L;
    private LazyLoader<T> loader;

    private SearchParameters initialRestriction;

    public ILCDLazyDataModel(LazyLoader<T> loader) {
        this(loader, null);
    }

    public ILCDLazyDataModel(LazyLoader<T> loader, SearchParameters initialRestriction) {
        this.setLoader(loader);
        this.setInitialRestriction(initialRestriction);
        this.setPageSize(DEFAULT_PAGE_SIZE);
        this.setRowCount(loader.count(initialRestriction).intValue());
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {

        // Normalize for legacy signature in lazyLoad method
        String sortField = (String) PrimefacesUtil.getLegacySortData(sortMeta).get("sortField");
        SortOrder sortOrder = (SortOrder) PrimefacesUtil.getLegacySortData(sortMeta).get("sortOrder");
        Map<String, Object> filters = PrimefacesUtil.getLegacyFilterData(filterMeta);

        SearchParameters sp = new SearchParameters(first, pageSize, sortField, (sortOrder == SortOrder.ASCENDING),
                getParametersMap(filters));

        if (getInitialRestriction() != null) {
            sp.append(getInitialRestriction());
        }

        this.setRowCount(getLoader().count(sp).intValue());
        return getLoader().loadLazy(sp);
    }

    private Map<String, Object[]> getParametersMap(Map<String, Object> filters) {
        Map<String, Object[]> paramsMap = new HashMap<String, Object[]>();
        for (Entry<String, Object> entry : filters.entrySet()) {
            paramsMap.put(entry.getKey(), new Object[]{entry.getValue()});
        }
        return paramsMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getRowData(String rowKey) {
        for (ILongIdObject entity : (List<ILongIdObject>) getWrappedData()) {
            if (entity.getId().toString().equals(rowKey))
                return (T) entity;
        }
        return null;
    }

    @Override
    public Object getRowKey(T t) {
        return t.getId();
    }

    public LazyLoader<T> getLoader() {
        return loader;
    }

    public void setLoader(LazyLoader<T> loader) {
        this.loader = loader;
    }

    public SearchParameters getInitialRestriction() {
        return initialRestriction;
    }

    public void setInitialRestriction(SearchParameters initialRestriction) {
        this.initialRestriction = initialRestriction;
    }
}
