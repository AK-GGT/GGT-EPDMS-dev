package de.iai.ilcd.security.jwtold;

import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.security.sql.IlcdPrinciple;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * The realm for managing bearer token-based requests (HS256)
 *
 * @author sarai
 */
@Deprecated
public class BearerRealmOLD extends AuthorizingRealm {

    public BearerRealmOLD() {
        setAuthenticationTokenClass(BearerTokenOLD.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        var t = token.getClass();
        return token != null && token.getClass().equals(BearerTokenOLD.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        BearerTokenOLD bearerToken = (BearerTokenOLD) token;
        if (bearerToken != null) {
            AuthenticationInfo authInfo = new BearerInfoOLD(bearerToken);
            if (authInfo != null) {
                return authInfo;
            }
        }
        throw new AuthenticationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        var p = principals.oneByType(IlcdPrinciple.class);
//		if(p == null) return new BearerAuthorization();
//		return new BearerAuthorization(p.getTokenClaims());
        if (p != null) return new IlcdAuthorizationInfo(p.getName());
        return null;

//		if ( principals != null ) {
//			var principle = (IlcdPrinciple) principals.getPrimaryPrincipal();
//			return new IlcdAuthorizationInfo( principle.getName()  );
//		}
//		else {
//			return new IlcdAuthorizationInfo();
//		}
    }

}