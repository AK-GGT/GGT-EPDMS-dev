package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.dao.NetworkNodeDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.dao.SearchResultCount;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.webgui.util.LocaleBean;
import de.iai.ilcd.webgui.util.PrimefacesUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for process list view
 */
@ManagedBean
@ViewScoped
public class ProcessesHandler extends AbstractDataSetsHandler<Process, ProcessDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3347083803227668593L;

    private static final Logger logger = LogManager.getLogger(ProcessesHandler.class);

    /**
     * Key for name filter
     */
    private final static String NAME_FILTER_KEY = "name";

    /**
     * Filter key for classes.
     */
    private final static String CLASSES_FILTER_KEY = "classes";
    /**
     * Flag if search was performed
     */
    protected boolean searched = false;
    /**
     *
     */
    private List<SelectItem> all2ndLevelClasses = null;
    /**
     * Available stocks bean
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocks;
    /**
     * Locale bean
     */
    @ManagedProperty(value = "#{localeBean}")
    private LocaleBean localeBean;

    /**
     * Initialize handler
     */
    public ProcessesHandler() {
        // TODO remove 3rd arg in constructor call once primefaces fixes datatable issue
        super(Process.class, new ProcessDao(), "tableForm:processTable");
    }

    /**
     * Get the selected classes
     *
     * @return selected classes
     */
    public List<String> getSelectedClasses() {
        String[] val = super.getFilterStringArr(ProcessesHandler.CLASSES_FILTER_KEY);
        if (val != null) {
            return new ArrayList<String>(Arrays.asList(val));
        } else {
            return null;
        }
    }

    /**
     * Set the selected classes
     *
     * @param selected selected classes
     */
    public void setSelectedClasses(List<String> selected) {
        String[] val;
        if (selected != null && selected.size() > 0) {
            val = selected.toArray(new String[0]);
        } else {
            val = null;
        }
        super.setFilter(ProcessesHandler.CLASSES_FILTER_KEY, val);
    }

    /**
     * Get the current value of name filter
     *
     * @return current value of name filter
     */
    public String getNameFilter() {
        return super.getFilter(ProcessesHandler.NAME_FILTER_KEY);
    }

    /**
     * Set value for name filter
     *
     * @param nameFilter name filter value to set
     */
    public void setNameFilter(String nameFilter) {
        super.setFilter(ProcessesHandler.NAME_FILTER_KEY, nameFilter);
    }

    /**
     * Get all 2nd level classification classes
     *
     * @return 2nd level classification classes
     */
    public List<SelectItem> getAll2ndLevelClasses() {
        if (this.all2ndLevelClasses == null) {
            this.all2ndLevelClasses = new ArrayList<SelectItem>();

            // Load all classes for pick list
            for (ClClass topClass : super.getDaoInstance().getTopClasses(this.availableStocks.getAllStocksMeta().toArray(new IDataStockMetaData[0]))) {
                for (ClClass subClass : super.getDaoInstance().getSubClasses(topClass.getName(), "1", true, this.availableStocks.getAllStocksMeta().toArray(new IDataStockMetaData[0]))) {
                    final String topAndSubClassName = topClass.getName() + " / " + subClass.getName();
                    this.all2ndLevelClasses.add(new SelectItem(topAndSubClassName, topAndSubClassName));
                }
            }
        }
        return this.all2ndLevelClasses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetLazyDataModel<Process> createLazyDataModel() {
        @SuppressWarnings("serial")
        DataSetLazyDataModel<Process> tmp = new DataSetLazyDataModel<Process>(this.getType(), this.getDaoInstance(), true, this.getStockSelection().getCurrentStockAsArray()) {
            @Override
            public List<Process> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {

                // Normalize for legacy signature in lazyLoad method
                String sortField = (String) PrimefacesUtil.getLegacySortData(sortMeta).get("sortField");
                SortOrder sortOrder = (SortOrder) PrimefacesUtil.getLegacySortData(sortMeta).get("sortOrder");
                Map<String, Object> filters = PrimefacesUtil.getLegacyFilterData(filterMeta);

                // TODO this disables the filtering for the search results page.
                //      It would be nice to be able to filter there, too.
                if (!ProcessesHandler.this.searched) {
                    this.setParams(filters);
                    try {
                        if (!(getParams() == null))
                            for (String s : getParams().keySet()) {
                                filters.put(s, getParams().get(s));
                            }
                    } catch (NullPointerException e) {
                    }
                }

                List<Process> result;

                if (!this.isDistributedSearch()) {
                    this.setRowCount((int) this.getDaoObject().searchResultCount(new ValueParser(this.getParams()), this.isMostRecentOnly(), this.getStocks()));
                    result = this.getDaoObject().searchDist(this.getType(), new ValueParser(this.getParams()), first, pageSize, sortField, !(SortOrder.DESCENDING.equals(sortOrder)), localeBean.getLocale().getLanguage(), true, this.isMostRecentOnly(), this.getStocks(), null, this.getLog());
                } else {
                    logger.debug("distributed search");
                    SearchResultCount count = new SearchResultCount();
                    result = this.getDaoObject().searchDist(this.getType(), new ValueParser(this.getParams()), first, pageSize, sortField, !(SortOrder.DESCENDING.equals(sortOrder)), localeBean.getLocale().getLanguage(), true, this.isMostRecentOnly(), this.getStocks(), null, this.getLog(), count);
                    this.setRowCount(count.getValue());
                    this.getDaoObject().sort(result, sortField, !(SortOrder.DESCENDING.equals(sortOrder)), localeBean.getLocale().getLanguage());
                }

                return result;
            }
        };
        return tmp;
    }

    /**
     * Get node base URL by registry ID and node ID
     *
     * @param registryUuid registry UUID (optional, may be null in p2p mode)
     * @param nodeId       node ID
     * @return node base URL or <code>null</code> on errors (e.g. {@link NumberFormatException} with
     * <code>registryId</code>)
     */
    public String getNodeBaseUrl(String registryUuid, String nodeId) {
        logger.trace("getting node base URL for registry {} and node {}", registryUuid, nodeId);
        NetworkNodeDao nnd = new NetworkNodeDao();
        try {
            String result;
            if (!StringUtils.isBlank(registryUuid)) {
                // long registryIdLng = Long.parseLong( registryId );
                NetworkNode node = nnd.getNetworkNode(nodeId, registryUuid);
                if (node == null) {
                    logger.trace("network node not found by {} {}", nodeId, registryUuid);
                }
                result = node.getBaseUrl();
            } else {
                result = nnd.getNetworkNode(nodeId).getBaseUrl();
            }
            if (result.endsWith("resource/"))
                result = StringUtils.substringBeforeLast(result, "resource/");
            logger.trace("result is {}", result);
            return result;
        } catch (Exception ex) {
            logger.trace(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Get node id by node ID
     *
     * @param nodeId node ID
     * @return node id or <code>null</code> on errors (e.g. {@link NumberFormatException})
     */
    public Long getNodeId(String nodeId) {
        NetworkNodeDao nnd = new NetworkNodeDao();
        try {
            return nnd.getNetworkNode(nodeId).getId();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the available stocks bean
     *
     * @return available stocks bean
     */
    public AvailableStockHandler getAvailableStocks() {
        return this.availableStocks;
    }

    /**
     * Set available stocks bean
     *
     * @param availableStocks available stocks bean to set
     */
    public void setAvailableStocks(AvailableStockHandler availableStocks) {
        this.availableStocks = availableStocks;
    }

    public LocaleBean getLocaleBean() {
        return localeBean;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }
}
