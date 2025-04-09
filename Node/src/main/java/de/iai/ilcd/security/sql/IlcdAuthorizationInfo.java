package de.iai.ilcd.security.sql;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.IUser;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Authorization Info. If anything is changed on the permissions, call {@link #permissionsChanged()} to trigger a reload
 * of the permissions for all currently active users on their next request.
 */
@SuppressWarnings("serial")
public class IlcdAuthorizationInfo extends SimpleAuthorizationInfo implements AuthorizationInfo {

    /**
     * Time stamp of last time permissions were changed
     */
    private static long lastPermissionChange = System.currentTimeMillis();
    /**
     * Name of the user (<code>null</code> for guests)
     */
    private final String userName;
    /**
     * DAO to access users
     */
    private final UserDao dao = new UserDao();
    /**
     * The permissions
     */
    private final Set<Permission> permissions = new HashSet<Permission>();
    /**
     * Time stamp of last time permissions were loaded
     */
    private long lastPermissionLoad = 0;

    /**
     * Create authorization info for for a user
     *
     * @param userName user name of user
     */
    public IlcdAuthorizationInfo(String userName) {
        this.userName = userName;
    }

    /**
     * Create authorization info for guests
     */
    public IlcdAuthorizationInfo() {
        this.userName = null;
    }

    /**
     * Call if anything is changed on the permissions (will trigger reload)
     */
    public static void permissionsChanged() {
        IlcdAuthorizationInfo.lastPermissionChange = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Set<Permission> getObjectPermissions() {
        if (this.lastPermissionLoad < IlcdAuthorizationInfo.lastPermissionChange) {
            this.lastPermissionLoad = System.currentTimeMillis();
            this.loadPermissions();
        }
        return this.permissions;
    }

    /**
     * Load the shiro wildcard string permissions
     */
    private void loadPermissions() {
        this.permissions.clear();
        IUser user = null;
        try {
            if (this.userName != null) { // TODO: get current user directly without dao
                user = this.dao.getUser(this.userName);
            }

            this.permissions.addAll(IlcdShiroPermissions.read(user));
        } catch (Exception e) {
            // Nop
        }
    }

}
