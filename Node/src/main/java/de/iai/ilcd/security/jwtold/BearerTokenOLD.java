package de.iai.ilcd.security.jwtold;

import de.iai.ilcd.security.sql.IlcdPrinciple;
import org.apache.shiro.authc.AuthenticationToken;

public class BearerTokenOLD implements AuthenticationToken {

    private static final long serialVersionUID = -3379300982758142191L;

    private String username;

    private String randomness;

    private String rawToken;

    private IlcdPrinciple principal;

    /**
     * Initializing the bearer token with given information
     *
     * @param username   The name of the user to create bearer token for
     * @param randomness The randomness for token related to given user
     * @param rawToken   keeping the token in the original JSON format for future use
     */
    public BearerTokenOLD(String username, String randomness, String rawToken) {
        this.username = username;
        this.randomness = randomness;
        this.rawToken = rawToken;
        this.principal = new IlcdPrinciple(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCredentials() {
        return randomness;
    }

    public String getRawToken() {
        return rawToken;
    }

    public void setRawToken(String rawToken) {
        this.rawToken = rawToken;
    }

}
