package de.iai.ilcd.security.jwtold;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.security.FatPrincipal;
import de.iai.ilcd.security.sql.IlcdPrinciple;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * The info class for bearer tokens.
 *
 * @author sarai
 */
public class BearerInfoOLD implements AuthenticationInfo {

    private static final long serialVersionUID = 7124358331031636214L;

    private BearerTokenOLD token;

    private UserDao dao = new UserDao();

    public BearerInfoOLD(BearerTokenOLD token) {
        this.token = token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrincipalCollection getPrincipals() {
        IUser user = getUser();
        if (user != null) {
            return new SimplePrincipalCollection(
                    new IlcdPrinciple(user), IlcdSecurityRealm.REALM_NAME);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCredentials() {
        IUser user = getUser();
        if (user != null) {
            return user.getApiKey();
        }
        return null;
    }

    /**
     * Gets user from token
     *
     * @return a user resembled in token
     */
    public IUser getUser() {
        if (token != null && token.getPrincipal() instanceof IlcdPrinciple) {
            String userName = ((FatPrincipal) token.getPrincipal()).getName();
            return dao.getUser(userName);
        }
        return null;
    }

}