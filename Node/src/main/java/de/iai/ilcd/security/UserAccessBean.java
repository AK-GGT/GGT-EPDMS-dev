package de.iai.ilcd.security;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User access bean
 */
@ManagedBean(name = "user")
@ViewScoped
@Deprecated
public class UserAccessBean extends AbstractHandler { // TODO: rm!!

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 556220221006512832L;

    /**
     * User object
     */
    private IUser user = SecurityUtil.getCurrentUser();

    /**
     * Determine if user is logged in
     *
     * @return <code>true</code> if user logged in, else <code>false</code>
     */
    public boolean isLoggedIn() {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser == null) {
            return false;
        }

        if (currentUser.isAuthenticated() || currentUser.isRemembered()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if user shall be able to enter admin area
     *
     * @return <code>true</code> if access shall be granted, <code>false</code> otherwise
     * @see SecurityUtil#hasAdminAreaAccessRight()
     */
    public boolean hasAdminAreaAccessRight() {
        return SecurityUtil.hasAdminAreaAccessRight();
    }

    /**
     * Determine if user is super admin
     *
     * @return <code>true</code> if super admin access shall be granted, <code>false</code> otherwise
     * @see SecurityUtil#hasSuperAdminPermission()
     */
    public boolean hasSuperAdminPermission() {
        return SecurityUtil.hasSuperAdminPermission();
    }

    /**
     * Determines if user has permission to import data sets into resp. stock.
     *
     * @return
     */
    public boolean hasImportRight(IDataStockMetaData ds) {
        return SecurityUtil.hasImportPermission(ds);
    }

    /**
     * Determines there is any root data stock into which the user can import data sets.
     *
     * @return
     */
    public boolean hasAnyImportRight() {
        AvailableStockHandler stockHandler = new AvailableStockHandler();
        List<IDataStockMetaData> possibleStocks = stockHandler.getRootStocksMeta().stream()
                .filter(stock -> hasImportRight(stock)).collect(Collectors.toList());

        return possibleStocks.size() > 0;
    }

    /**
     * Determine if the current user has dataset detail view (means {@link ProtectionType#EXPORT}) right
     *
     * @param ds data set to check for
     * @return <code>true</code> if dataset detail view right is present, <code>false</code> otherwise
     */
    public boolean hasDatasetDetailRights(DataSet ds) {
        return SecurityUtil.hasExportPermission(ds);
    }

    /**
     * Get the user object for the current user.
     *
     * @return user object
     */
    public IUser getUserObject() {
        return SecurityUtil.getCurrentUser();
    }

    public IUser getUser() {
        return user;
    }

    public void setUser(IUser user) {
        this.user = (User) user;
    }

    public String getUserName() {
        return user.getUserName();
    }

}
