package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.AbstractDao;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.UserAccessBean;
import org.primefaces.model.SortOrder;

import javax.faces.bean.ManagedProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Admin handler for list with entries that depend on organization of current user
 *
 * @param <T> Type of organization dependent entity
 * @param <D> DAO type for the entity
 */
public abstract class AbstractAdminOrgDependentListHandler<T, D extends AbstractDao<T>> extends AbstractAdminListHandler<T> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1292848765701056930L;

    /**
     * DAO
     */
    private final D dao;
    /**
     * User access bean that is being injected
     */
    @ManagedProperty(value = "#{user}")
    private UserAccessBean userBean;
    /**
     * Organization of current user
     */
    private Organization organization = null;

    /**
     * Create handler for admin lists that differ between organization admin / super admin
     *
     * @param dao dao to use
     */
    protected AbstractAdminOrgDependentListHandler(D dao) {
        super();
        this.dao = dao;
    }

    /**
     * Load element count for organization dependent elements (organization admin)
     *
     * @param o organization to load for
     * @return loaded count
     */
    protected abstract long loadElementCount(Organization o);

    /**
     * Load ALL elements (super admin)
     *
     * @param first     fist index
     * @param pageSize  max result item count
     * @param sortField sort field
     * @param sortOrder sort order
     * @param filters   filters
     * @return loaded elements
     */
    protected abstract List<T> lazyLoad(Organization o, int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters);

    /**
     * Load element count for ALL elements (super admin)
     *
     * @return loaded count
     */
    protected long loadElementCountAll() {
        return this.dao.getAllCount();
    }

    /**
     * Load ALL elements (super admin)
     *
     * @param first     fist index
     * @param pageSize  max result item count
     * @param sortField sort field
     * @param sortOrder sort order
     * @param filters   filters
     * @return loaded elements
     */
    protected List<T> lazyLoadAll(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        String orderByKeyword = SortOrder.DESCENDING.equals(sortOrder) ? "DESC" : "ASC";
        return this.dao.get(first, pageSize, filters, sortField, orderByKeyword);
    }

    /**
     * Get the DAO
     *
     * @return DAO
     */
    public D getDao() {
        return this.dao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void postConstruct() {
        if (this.userBean.hasSuperAdminPermission()) {
            this.organization = null;
        }
        // only organization groups
        else {
            Organization o = this.userBean.getUserObject().getOrganization();
            SecurityUtil.assertIsOrganizationAdmin(o);
            this.organization = o;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final long loadElementCount() {

        if (this.organization != null) {
            return this.loadElementCount(this.organization);
        } else if (this.userBean.hasSuperAdminPermission()) {
            return this.loadElementCountAll();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<T> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        addCustomFiltersToQuery(filters); // if not overriden, this does nothing.

        List<T> result;

        if (this.organization != null) {
            result = this.lazyLoad(this.organization, first, pageSize, sortField, sortOrder, filters);
        } else if (this.userBean.hasSuperAdminPermission()) {
            result = this.lazyLoadAll(first, pageSize, sortField, sortOrder, filters);
        } else {
            return Collections.emptyList();
        }

        return result;
    }

    protected void addCustomFiltersToQuery(Map<String, Object> filters) {
        // Override this if necessary
    }

    /**
     * Get the user access bean
     *
     * @return user access bean
     */
    public UserAccessBean getUserBean() {
        return this.userBean;
    }

    /**
     * Setter for the injection of the the user access bean
     *
     * @param userBean user access bean to set
     */
    public void setUserBean(UserAccessBean userBean) {
        this.userBean = userBean;
    }
}
