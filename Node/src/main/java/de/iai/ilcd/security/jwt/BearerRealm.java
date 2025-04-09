package de.iai.ilcd.security.jwt;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.security.role.RemoteRole;
import de.iai.ilcd.security.role.RoleMapping;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * The realm for managing bearer token-based requests (RS256)
 *
 * @author sarai
 */
public class BearerRealm extends AuthorizingRealm {

    final public static String REALM_NAME = "bearerRealm";

    public BearerRealm() {
//		super(new MemoryConstrainedCacheManager());
//		setCacheManager(new MemoryConstrainedCacheManager());
//		setAuthenticationTokenClass(BearerToken.class);

//		setAuthenticationCachingEnabled(true);
//		setAuthorizationCachingEnabled(true);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token.getClass().equals(BearerToken.class); // TODO: maybe check signing alg in header too
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        BearerToken bearerToken = (BearerToken) token;

//		if(!user.isApiKeyAllowed())
//			throw new AuthenticationException("API access is not allowed for the specified user");

        return new BearerAuthentication(bearerToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        var p = principals.oneByType(BearerPrincipal.class);
//		if (p != null) return new BearerAuthorization(p.getTokenClaims());
        if (p == null) return null;

        var authz = new BearerAuthorization(p.getTokenClaims());
        // MK: it's should be it's own realm and with it's own implementation of AuthorizationInfo
        // TODO: remove this temp workaround
        var remote = ApplicationContextHolder.getApplicationContext().getBean(RemoteRole.class);
        var mapping = ApplicationContextHolder.getApplicationContext().getBean(RoleMapping.class);
        if (p.getRawToken().contains("InN1aHUtZGVmYXVsdC1rZXktaWQif")) { // partial kid in base64
            var extra_roles = remote.licenseRoles(ConfigurationService.INSTANCE.getOidcExternalRolesUrl(), p.getRawToken());
            for (var r : extra_roles)
                for (var perm : mapping.expand(r).split(";"))
                    authz.addPermission(perm);
        }


        return authz;
    }
}
