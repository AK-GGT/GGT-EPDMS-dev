package de.iai.ilcd.security.jwt;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * The info class for bearer tokens.
 *
 * @author sarai
 */
public class BearerAuthentication implements AuthenticationInfo { // TODO: add more attributes here?

    private static final long serialVersionUID = 7124358331031636214L;

    private BearerToken token;

    public BearerAuthentication(BearerToken token) {
        this.token = token; // TODO: how many time this is instantiated?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrincipalCollection getPrincipals() {
        if (token != null) {
            return new SimplePrincipalCollection(token.getPrincipal(), BearerRealm.REALM_NAME);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCredentials() {
        return token;
    }

}
