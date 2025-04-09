package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.utils.DistributedSearchLog;
import de.iai.ilcd.rest.RegistryService;
import org.primefaces.model.DualListModel;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * Backing bean for processes search
 */
@ManagedBean
@ViewScoped
public class ProcessesSearchHandler extends ProcessesHandler {

    public final static String FILTER_COMPLIANCE_MODE_AND = "AND";
    public final static String FILTER_COMPLIANCE_MODE_OR = "OR";
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8234619084745241858L;
    /**
     * Filter key for lower reference year. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_REF_YEAR_LOWER = "referenceYearLower";
    /**
     * Filter key for upper reference year. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_REF_YEAR_UPPER = "referenceYearUpper";
    /**
     * Filter key for locations. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_LOCATIONS = "location";
    /**
     * Filter keys for compliance systems match mode. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_COMPLIANCE_MODE = "complianceMode";
    /**
     * Filter key for compliance systems. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_COMPLIANCE = "compliance";

    /**
     * Filter key for type. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_TYPE = "type";

    /**
     * Filter key for parameterized only. Do not change value, is same as in createQueryObject for ProcessDAO!
     */
    private final static String FILTER_PARAMETERIZED = "parameterized";

    /**
     * Filter key for distributed search. Do not change value, is same as in createQueryObject for DataSetDAO!
     */
    private final static String FILTER_DISTRIBUTED = "distributed";

    /**
     * Filter key for description. Do not change value, is same as in createQueryObject for DataSetDAO!
     */
    private final static String FILTER_DESCRIPTION = "description";

    /**
     * Filter key for classes. Do not change value, is same as in createQueryObject for DataSetDAO!
     */
    private final static String FILTER_CLASSES = "classes";

    private static final String FILTER_REGISTRY_UUID = "registeredIn";

    private static final String FILTER_REGISTRY_VIRTUAL = "virtual";

    /**
     * List of all locations from database
     */
    private final List<SelectItem> allLocations;

    /**
     * List of all reference years from database
     */
    private final List<SelectItem> referenceYears = new ArrayList<SelectItem>();

    /**
     * List of types from database
     */
    private final List<SelectItem> types;

    /**
     * List of types from database
     */
    private final List<SelectItem> complianceSystems;
    private final RegistryService registryService;
    /**
     * List of all classes as dual list for pick list from database
     */
    private DualListModel<String> pickAllClasses;
    private Long registryId;

    /**
     * Initialize the handler, get all lists that well be needed
     */
    public ProcessesSearchHandler() {
        super();
        // Load all locations
        this.allLocations = new ArrayList<SelectItem>();
        for (GeographicalArea geoArea : super.getDaoInstance().getAllLocations()) {
            this.allLocations.add(new SelectItem(geoArea.getAreaCode(), geoArea.getName()));
        }

        // Load all years
        for (Integer refYear : super.getDaoInstance().getReferenceYears()) {
            if (refYear != null) {
                this.referenceYears.add(new SelectItem(refYear.toString()));
            }
        }

        // get all types
        this.types = new ArrayList<SelectItem>();
        for (TypeOfProcessValue o : TypeOfProcessValue.values()) {
            this.types.add(new SelectItem(o, o.value()));
        }

        // get all complianceSystems
        this.complianceSystems = new ArrayList<SelectItem>();
        for (Source s : (new SourceDao()).getComplianceSystems()) {
            if (s != null) {
                this.complianceSystems.add(new SelectItem(s.getUuidAsString(), s.getDefaultName()));
            }
        }

        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.registryService = ctx.getBean(RegistryService.class);

    }

    public void postConstruct() {
        // Load all classes for pick list
        List<SelectItem> all2ndLevelClasses = super.getAll2ndLevelClasses();

        // sort them so that the pick lists will be sorted alphabetically
        Collections.sort(all2ndLevelClasses,
                (c1, c2) -> String.valueOf(c1.getValue()).toLowerCase(Locale.ROOT)
                        .compareTo(String.valueOf(c2.getValue()).toLowerCase(Locale.ROOT))); // Anonymous Comparator<SelectItem>

        // initialize DualListModel
        List<String> allClassesSource = new ArrayList<String>();
        List<String> allClassesTarget = new ArrayList<String>();
        for (SelectItem item : super.getAll2ndLevelClasses()) {
            allClassesSource.add((String) item.getValue());
        }

        this.pickAllClasses = new DualListModel<String>(allClassesSource, allClassesTarget);
    }

    /**
     * Get the select items for the locations multiple selection list
     *
     * @return select items for the locations multiple selection list
     */
    public List<SelectItem> getAllLocations() {
        return this.allLocations;
    }

    /**
     * Get the select items for the type selection single selection list
     *
     * @return select items for the type selection single selection list
     */
    public List<SelectItem> getTypes() {
        return this.types;
    }

    /**
     * Get the select items for the reference year single selection lists
     *
     * @return select items for the reference year single selection lists
     */
    public List<SelectItem> getReferenceYears() {
        return this.referenceYears;
    }

    /**
     * Get the parameterized filter value
     *
     * @return parameterized filter value
     */
    public Boolean getParameterizedFilter() {
        return super.getFilterBoolean(ProcessesSearchHandler.FILTER_PARAMETERIZED);
    }

    /**
     * Set the parameterized filter value
     *
     * @param parameterized new value to set
     */
    public void setParameterizedFilter(Boolean parameterized) {
        if (Boolean.TRUE.equals(parameterized)) {
            super.setFilter(ProcessesSearchHandler.FILTER_PARAMETERIZED, Boolean.TRUE);
        } else {
            super.setFilter(ProcessesSearchHandler.FILTER_PARAMETERIZED, null);
        }
    }

    /**
     * Get the parameterized filter value
     *
     * @return parameterized filter value
     */
    public Boolean getDistributedFilter() {
        return super.getFilterBoolean(ProcessesSearchHandler.FILTER_DISTRIBUTED);
    }

    /**
     * Set the parameterized filter value
     *
     * @param parameterized new value to set
     */
    public void setDistributedFilter(Boolean parameterized) {
        if (Boolean.TRUE.equals(parameterized)) {
            super.setFilter(ProcessesSearchHandler.FILTER_DISTRIBUTED, Boolean.TRUE);
            super.getLazyDataModel().setLog(new DistributedSearchLog());
            if (!ConfigurationService.INSTANCE.isRegistryBasedNetworking()) {
                super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, Boolean.TRUE);
            }
        } else {
            super.setFilter(ProcessesSearchHandler.FILTER_DISTRIBUTED, null);
            super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_UUID, null);
            super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, null);
        }
    }

    /**
     * Get the selected type
     *
     * @return selected type
     */
    public String getTypeFilter() {
        return super.getFilter(ProcessesSearchHandler.FILTER_TYPE);
    }

    /**
     * Set the selected type
     *
     * @param type type to set
     */
    public void setTypeFilter(String type) {
        super.setFilter(ProcessesSearchHandler.FILTER_TYPE, type);
    }

    /**
     * Get the description filter value
     *
     * @return description filter value
     */
    public String getDescriptionFilter() {
        return super.getFilter(ProcessesSearchHandler.FILTER_DESCRIPTION);
    }

    /**
     * Set the description filter value
     *
     * @param descFilter description filter value to set
     */
    public void setDescriptionFilter(String descFilter) {
        super.setFilter(ProcessesSearchHandler.FILTER_DESCRIPTION, descFilter);
    }

    /**
     * Get the selected locations
     *
     * @return selected locations
     */
    public List<String> getLocationsFilter() {
        String[] val = super.getFilterStringArr(ProcessesSearchHandler.FILTER_LOCATIONS);
        if (val != null) {
            return Arrays.asList(val);
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Set the selected locations
     *
     * @param locations locations to set
     */
    public void setLocationsFilter(List<String> locations) {
        String[] val;
        if (locations != null && !locations.isEmpty()) {
            val = locations.toArray(new String[0]);
        } else {
            val = null;
        }
        super.setFilter(ProcessesSearchHandler.FILTER_LOCATIONS, val);
    }

    /**
     * Get the selected compliance systems
     *
     * @return selected compliance systems
     */
    public List<String> getComplianceFilter() {
        String[] val = super.getFilterStringArr(ProcessesSearchHandler.FILTER_COMPLIANCE);
        if (val != null) {
            List<String> result = new ArrayList<String>();
            for (String s : val) {
                result.add(s);
            }
            return result;
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Set the selected compliance systems
     *
     * @param complianceSystems compliance systems to set
     */
    public void setComplianceFilter(List<String> complianceSystems) {
        String[] val;
        if (complianceSystems != null && !complianceSystems.isEmpty()) {
            val = complianceSystems.toArray(new String[0]);
        } else {
            val = null;
        }
        super.setFilter(ProcessesSearchHandler.FILTER_COMPLIANCE, val);
    }

    /**
     * Get the parameterized filter value
     *
     * @return parameterized filter value
     */
    public String getComplianceModeFilter() {
        String val = super.getFilter(ProcessesSearchHandler.FILTER_COMPLIANCE_MODE);
        if (val == null)
            return ProcessesSearchHandler.FILTER_COMPLIANCE_MODE_OR;
        else return val;
    }

    /**
     * Set the parameterized filter value
     *
     * @param parameterized new value to set
     */
    public void setComplianceModeFilter(String complianceMode) {
        super.setFilter(ProcessesSearchHandler.FILTER_COMPLIANCE_MODE, complianceMode);
    }

    /**
     * Get the filter value for lower reference year
     *
     * @return filter value for lower reference year
     */
    public Integer getReferenceYearLowerFilter() {
        // ugly but required until criteria API is used in DAO routines
        return this.parseInteger(this.getFilter(ProcessesSearchHandler.FILTER_REF_YEAR_LOWER));
    }

    /**
     * Set the filter value for lower reference year
     *
     * @param refYearLower new value to set
     */
    public void setReferenceYearLowerFilter(Integer refYearLower) {
        // ugly but required until criteria API is used in DAO routines
        if (refYearLower != null && refYearLower.intValue() != 0) {
            this.setFilter(ProcessesSearchHandler.FILTER_REF_YEAR_LOWER, refYearLower.toString());
        } else {
            this.setFilter(ProcessesSearchHandler.FILTER_REF_YEAR_LOWER, null);
        }
    }

    /**
     * Get the filter value for upper reference year
     *
     * @return filter value for upper reference year
     */
    public Integer getReferenceYearUpperFilter() {
        // ugly but required until criteria API is used in DAO routines
        return this.parseInteger(this.getFilter(ProcessesSearchHandler.FILTER_REF_YEAR_UPPER));
    }

    /**
     * Set the filter value for upper reference year
     *
     * @param refYearLower new value to set
     */
    public void setReferenceYearUpperFilter(Integer refYearUpper) {
        // ugly but required until criteria API is used in DAO routines
        if (refYearUpper != null && refYearUpper.intValue() != 0) {
            this.setFilter(ProcessesSearchHandler.FILTER_REF_YEAR_UPPER, refYearUpper.toString());
        } else {
            this.setFilter(ProcessesSearchHandler.FILTER_REF_YEAR_UPPER, null);
        }
    }

    /**
     * Get the pick list classes
     *
     * @return pick list classes
     */
    public DualListModel<String> getPickAllClasses() {
        return this.pickAllClasses;
    }

    /**
     * Set pick list model for all classes
     *
     * @param pickAllClasses list model to set
     */
    public void setPickAllClasses(DualListModel<String> pickAllClasses) {
        this.pickAllClasses = pickAllClasses;

        if (pickAllClasses != null) {
            List<String> lstClasses = new ArrayList<String>();
            for (String target : pickAllClasses.getTarget()) {
                lstClasses.add(target);
            }
            if (lstClasses.isEmpty()) {
                super.setFilter(ProcessesSearchHandler.FILTER_CLASSES, null);
            } else {
                super.setFilter(ProcessesSearchHandler.FILTER_CLASSES, lstClasses.toArray(new String[0]));
            }
        }
    }

    /**
     * Determined if a search was performed
     *
     * @return <code>true</code> if search was performed and result list shall be displayed, else <code>false</code>
     */
    public boolean getSearched() {
        return this.searched;
    }

    /**
     * Load the form, returns <code>null</code> (no redirect). Sets internal {@link #getSearched() search flag} to
     * <code>false</code>
     *
     * @return <code>null</code>
     */
    public String loadForm() {
        this.searched = false;
        return null;
    }

    /**
     * Do the search, sets internal {@link #getSearched() search flag} to <code>true</code>, trigger {@link #doFilter()}
     * and return <code>null</code> (no
     * redirect)
     *
     * @return <code>null</code>
     */
    public String search() {
        this.searched = true;
        if (this.getDistributedFilter() != null)
            this.getLazyDataModel().setDistributedSearch(this.getDistributedFilter());
        this.doFilter();
        return null;
    }

    /**
     * Parse integer. Null string, empty string or non-integer string will result in <code>null</code> <strong>Remove as
     * soon as criteria API is used for DAO
     * routines</strong>
     *
     * @param s string to parse
     * @return value as Integer or <code>null</code> if not possible
     */
    private Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRegistry() {
        return super.getFilter(ProcessesSearchHandler.FILTER_REGISTRY_UUID);
    }

    public void setRegistry(String registry) {
        if (registry != null && !registry.equals("")) {
            Registry reg = this.registryService.findByUUID(registry);
            // if(!reg.getVirtual()){
            // super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, Boolean.FALSE);
            // }else{
            // super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, Boolean.TRUE);
            // }

            super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, Boolean.FALSE);
            this.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_UUID, registry);
            this.registryId = reg.getId();

        } else {
            this.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_UUID, null);
            super.setFilter(ProcessesSearchHandler.FILTER_REGISTRY_VIRTUAL, null);
        }
    }

    public Long getRegistryId() {
        return this.registryId;
    }

    public boolean isLogEmpty() {
        return this.getLazyDataModel().getLog() == null || this.getLazyDataModel().getLog().isEmpty();
    }

    public List<SelectItem> getComplianceSystems() {
        return complianceSystems;
    }
}
