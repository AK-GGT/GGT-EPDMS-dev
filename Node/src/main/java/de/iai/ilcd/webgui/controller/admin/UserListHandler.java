package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import de.iai.ilcd.webgui.enums.TriStateCheckbox;
import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handler for the user list in admin area
 */
@ViewScoped
@ManagedBean(name = "userListHandler")
public class UserListHandler extends AbstractAdminOrgDependentListHandler<User, UserDao> {

    protected final static Logger log = LogManager.getLogger(UserListHandler.class);
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1884241468855139691L;
    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;

    private TriStateCheckbox superAdminPermissionFilter = TriStateCheckbox.EMPTY;

    /**
     * In the UI a TriStateCheckbox may be used to filter for booleans (no filter,
     * field=true, field=false). This map stores the field names together with
     * the filterstates (which are to be added to the filterMap, which is used in the lazy loading, later on).
     */
    private Map<String, TriStateCheckbox> additionalTermsFilters = new HashMap<String, TriStateCheckbox>();

    /**
     * Create the handler
     */
    public UserListHandler() {
        super(new UserDao());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSelected() {
        final User[] selectedItems = this.getSelectedItems();
        if (selectedItems == null) {
            return;
        }

        for (User item : selectedItems) {
            // Admin user not deletable
            if (ObjectUtils.equals(item.getId(), SodaUtil.ADMIN_ID)) {
                continue;    // not selectable in facelet and not deletable (double check)
            }
            try {
                this.getDao().remove(item);
                this.addI18NFacesMessage("facesMsg.removeSuccess", FacesMessage.SEVERITY_INFO, item.getUserName());
            } catch (Exception ex) {
                this.addI18NFacesMessage("facesMsg.removeError", FacesMessage.SEVERITY_ERROR, item.getUserName());
                if (log.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }

        this.dirty.stockModified();
        IlcdAuthorizationInfo.permissionsChanged();

        this.clearSelection();
        this.reloadCount();
    }

    /**
     * Get the selected users.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#getSelectedItems() getSelectedItems} in Facelets (see
     * it's documentation for the reason)
     * </p>
     *
     * @return selected users
     */
    public User[] getSelectedUsers() {
        return super.getSelectedItems();
    }

    /**
     * Set the selected users.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#setSelectedItems(Object[]) setSelectedItems} in Facelets
     * (see it's documentation for the reason)
     * </p>
     *
     * @param selected selected users
     */
    public void setSelectedUsers(User[] selected) {
        super.setSelectedItems(selected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long loadElementCount(Organization o) {
        Map<String, Object> filters = new HashMap<>();

        filters.put("organization", o);

        return this.getDao().getCount(filters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<User> lazyLoad(Organization o, int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        filters.put("organisation.id", o.getId());
        List<User> result = super.lazyLoadAll(first, pageSize, sortField, sortOrder, filters);

        return result;
    }

    @Override
    protected void addCustomFiltersToQuery(Map<String, Object> filters) {

        // superAdminPermission
        if (this.superAdminPermissionFilter != TriStateCheckbox.EMPTY)
            filters.put("superAdminPermission", superAdminPermissionFilter.getBooleanValue().booleanValue());

        // additional Terms
        Map<String, Boolean> addTermFilter = new HashMap<>();
        for (Entry<String, TriStateCheckbox> e : additionalTermsFilters.entrySet())
            if (!TriStateCheckbox.EMPTY.equals(e.getValue()))
                addTermFilter.put(e.getKey(), e.getValue().getBooleanValue());
        filters.put("acceptedAdditionalTermsMap", addTermFilter);


    }

    /**
     * Get the dirty flag bean
     *
     * @return dirty flag bean
     */
    public DirtyFlagBean getDirty() {
        return this.dirty;
    }

    /**
     * Set the dirty flag bean
     *
     * @param dirty the dirty flag bean
     */
    public void setDirty(DirtyFlagBean dirty) {
        this.dirty = dirty;
    }

    public String getSuperAdminPermissionFilter() {
        return String.valueOf(this.superAdminPermissionFilter.intValue());
    }

    public void setSuperAdminPermissionFilter(String pfIntString) {
        TriStateCheckbox t = TriStateCheckbox.from(Integer.parseInt(pfIntString));
        this.superAdminPermissionFilter = t;
    }

    public AddTermsFilterAccessor getAddTermsFilterAccessor(String key) {
        AddTermsFilterAccessor anonymousInstance = new AddTermsFilterAccessor(key);
        return anonymousInstance;
    }

    public class AddTermsFilterAccessor {

        String key;

        public AddTermsFilterAccessor(String key) {
            this.key = key;
        }

        public String getaddTermFilterState() {
            TriStateCheckbox checkboxState = additionalTermsFilters.get(key);
            int i = checkboxState != null ? checkboxState.intValue() : TriStateCheckbox.EMPTY.intValue();
            return String.valueOf(i);
        }

        public void setaddTermFilterState(String intValueString) {
            int i = Integer.parseInt(intValueString);
            additionalTermsFilters.put(key, TriStateCheckbox.from(i));
        }

    }

}
