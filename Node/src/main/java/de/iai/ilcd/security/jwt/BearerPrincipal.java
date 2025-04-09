package de.iai.ilcd.security.jwt;

import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.model.security.UserJWT;
import de.iai.ilcd.security.FatPrincipal;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearerPrincipal implements FatPrincipal {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerPrincipal.class);

    /**
     * Principle hands over the claims to the BearerAuthorization
     *
     * @see BearerRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    private Claims tokenClaims;
    private String rawToken;
    private UserJWT user;

//	public BearerPrincipal(Claims tokenClaims) {
//		this.tokenClaims = tokenClaims;
//	}

    public BearerPrincipal(String rawToken) {
        var jwt = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);
        this.rawToken = rawToken;
        this.tokenClaims = jwt.parseClaimsJws(rawToken);
        this.user = new UserJWT(tokenClaims, rawToken);
        LOGGER.trace("available claims: {}", this.getTokenClaims().keySet());
    }

    @Override
    public String getName() {
        return tokenClaims.getSubject();
    }

    public Claims getTokenClaims() {
        return tokenClaims;
    }

    public void setTokenClaims(Claims tokenClaims) {
        this.tokenClaims = tokenClaims;
    }

    @Override
    public IUser getSodaUser() {
        return user;
    }

    public String getRawToken() {
        return rawToken;
    }
}
