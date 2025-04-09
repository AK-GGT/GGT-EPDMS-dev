package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.UserGroupDao;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.Map;

/**
 * Admin handler for group list
 */
@ViewScoped
@ManagedBean
public class GroupListHandler extends AbstractAdminOrgDependentListHandler<UserGroup, UserGroupDao> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1292889465701056930L;

    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;

    /**
     * Create the handler
     */
    public GroupListHandler() {
        super(new UserGroupDao());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSelected() {
        final UserGroup[] selectedItems = this.getSelectedItems();
        if (selectedItems == null) {
            return;
        }

        for (UserGroup item : selectedItems) {
            try {
                this.getDao().remove(item);
                this.addI18NFacesMessage("facesMsg.removeSuccess", FacesMessage.SEVERITY_INFO, item.getGroupName());
            } catch (Exception ex) {
                this.addI18NFacesMessage("facesMsg.removeError", FacesMessage.SEVERITY_ERROR, item.getGroupName());
            }
        }

        this.dirty.stockModified();
        IlcdAuthorizationInfo.permissionsChanged();

        this.clearSelection();
        this.reloadCount();
    }

    /**
     * Get the selected groups.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#getSelectedItems() getSelectedItems} in Facelets (see
     * it's documentation for the reason)
     * </p>
     *
     * @return selected groups
     */
    public UserGroup[] getSelectedGroups() {
        return super.getSelectedItems();
    }

    /**
     * Set the selected groups.
     * <p>
     * <b>Do not replace</b> by {@link AbstractAdminListHandler#setSelectedItems(Object[]) setSelectedItems} in Facelets
     * (see it's documentation for the reason)
     * </p>
     *
     * @param selected selected groups
     */
    public void setSelectedGroups(UserGroup[] selected) {
        super.setSelectedItems(selected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long loadElementCount(Organization o) {
        return this.getDao().getGroupsCount(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UserGroup> lazyLoad(Organization o, int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return this.getDao().getGroups(o, first, pageSize);
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

}
