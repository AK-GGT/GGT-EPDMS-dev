package de.iai.ilcd.security.jwt;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.realm.AuthorizingRealm;

/**
 * The class resembling the bearer token for authentication.
 *
 * @author sarai
 */
public class BearerToken implements AuthenticationToken {

    private static final long serialVersionUID = -3379300982758142191L;

    private String username;
    private String rawToken;
    private BearerPrincipal bearerPrincipal;

    /**
     * Initializing the bearer token with given information
     *
     * @param username The name of the user to create bearer token for
     * @param rawToken keeping the token in the original JSON format for future use
     */
    public BearerToken(String username, String rawToken) {
        this.username = username;
        this.rawToken = rawToken;
        this.bearerPrincipal = new BearerPrincipal(rawToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPrincipal() {
        return bearerPrincipal;
    }

    /**
     * <p>
     * Shiro's Default behavior of when matching credentials is it calls
     * <code>getCredentials</code> on AuthenticationToken and
     * AuthenticationInfo.
     * </p>
     *
     *
     * <p>
     * To avoid overriding <code>assertCredentialsMatch()</code> in realm or
     * implementing the a custom credential matcher. we just return this.
     * </p>
     * <p>
     * <p>
     * {@inheritDoc}
     *
     * @see AuthorizingRealm#assertCredentialsMatch(AuthenticationToken, AuthenticationInfo)
     * @see SimpleCredentialsMatcher
     */
    @Override
    public Object getCredentials() {
        return this;
    }

    public String getRawToken() {
        return rawToken;
    }

    public void setRawToken(String rawToken) {
        this.rawToken = rawToken;
    }

}
