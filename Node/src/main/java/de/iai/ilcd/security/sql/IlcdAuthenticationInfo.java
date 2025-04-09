package de.iai.ilcd.security.sql;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.security.Principal;

public class IlcdAuthenticationInfo implements AuthenticationInfo {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 591932215980524142L;

    /**
     * Token for authentication
     */
    private final AuthenticationToken authToken;

    /**
     * DAO for the user objects
     */
    private final UserDao userDao = new UserDao();

    /**
     * Create the authentication info
     *
     * @param at token for the info
     */
    public IlcdAuthenticationInfo(AuthenticationToken at) {
        this.authToken = at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(
                new IlcdPrinciple(getUser()), IlcdSecurityRealm.REALM_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCredentials() {
        return getUser().getPasswordHash();
    }

    /**
     * Get the user for the current authentication token
     *
     * @return user for the current authentication token (or <code>null</code> if none found)
     */
    private User getUser() {
        if (this.authToken == null) {
            return null;
        }
        try {
            User user = this.userDao.getUser(((Principal) this.authToken.getPrincipal()).getName()); // TODO: not like that
            return user.isActivated() ? user : null;
        } catch (Exception e) {
            return null;
        }
    }

}
