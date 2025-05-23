package de.iai.ilcd.security.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Security Realm for shiro. Please note that some code fragments were copied (and modified)
 * from {@link AuthorizingRealm} in order to enable permission checking for guests.
 */
public class IlcdSecurityRealm extends AuthorizingRealm {

    /**
     * Name of the realm
     */
    public static final String REALM_NAME = "dbRealm";

    /**
     * Logger
     */
    private static final Logger log = LogManager.getLogger(IlcdSecurityRealm.class);

    /**
     * Get the encrypted password for plain text and hash salt
     *
     * @param plainPassword plain text password
     * @param hashSalt      salt for the hash
     * @return result hex string
     */
    public static String getEncryptedPassword(String plainPassword, String hashSalt) {
        return new Sha512Hash(plainPassword, hashSalt, 5).toHex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {

        if (pc != null) {
            // TODO: implement an AuthenticationStrategy if this didn't grab the primaryPrinciple
            var principal = pc.oneByType(IlcdPrinciple.class);
            if (principal != null)
                return new IlcdAuthorizationInfo(principal.getName());
        }
        return new IlcdAuthorizationInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
        AuthenticationInfo authInfo = new IlcdAuthenticationInfo(at);
        return authInfo;
    }

    /**
     * Clear the cache
     *
     * @param principals principal collection to clear cache for
     */
    public void clearAuthorizationInfo(PrincipalCollection principals) {
        this.clearCachedAuthorizationInfo(principals);
    }

    /*
     * Below are some copied methods from AuthorizingRealm with minor changed
     * to support guest permission checking.
     */

    /**
     * {@inheritDoc} <br />
     * Original code from {@link AuthorizingRealm}, <code>null</code> check removed (guest support)
     */
    @Override
    protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) { // TODO: rm this and enable: setAuthorizationCachingEnabled(true)

        AuthorizationInfo info = null;

        if (log.isTraceEnabled()) {
            log.trace("Retrieving AuthorizationInfo for principals [" + principals + "]");
        }

        Cache<Object, AuthorizationInfo> cache = this.getAvailableAuthorizationCache();
        if (cache != null) {
            if (log.isTraceEnabled()) {
                log.trace("Attempting to retrieve the AuthorizationInfo from cache.");
            }
            Object key = this.getAuthorizationCacheKey(principals);
            info = cache.get(key);
            if (log.isTraceEnabled()) {
                if (info == null) {
                    log.trace("No AuthorizationInfo found in cache for principals [" + principals + "]");
                } else {
                    log.trace("AuthorizationInfo found in cache for principals [" + principals + "]");
                }
            }
        }

        if (info == null) {
            // Call template method if the info was not found in a cache
            info = this.doGetAuthorizationInfo(principals);
            // If the info is not null and the cache has been created, then cache the authorization info.
            if (info != null && cache != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Caching authorization info for principals: [" + principals + "].");
                }
                Object key = this.getAuthorizationCacheKey(principals);
                cache.put(key, info);
            }
        }

        return info;
    }

    /**
     * {@inheritDoc} <br />
     * Original code from {@link AuthorizingRealm}, <code>null</code> returns string <code>guest</code>
     */
    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return principals != null ? principals : "guest";
    }

    /**
     * {@inheritDoc} <br />
     * Original code from {@link AuthorizingRealm}, copied due to dependency of
     * {@link #getAuthorizationInfo(PrincipalCollection)}
     */
    private Cache<Object, AuthorizationInfo> getAvailableAuthorizationCache() {
        Cache<Object, AuthorizationInfo> cache = this.getAuthorizationCache();
        if (cache == null && this.isAuthorizationCachingEnabled()) {
            cache = this.getAuthorizationCacheLazy();
        }
        return cache;
    }

    /**
     * {@inheritDoc} <br />
     * Original code from {@link AuthorizingRealm}, copied and modified due to
     * dependency of {@link #getAvailableAuthorizationCache()}
     */
    private Cache<Object, AuthorizationInfo> getAuthorizationCacheLazy() {
        Cache<Object, AuthorizationInfo> authorizationCache = this.getAuthorizationCache();
        if (authorizationCache == null) {

            if (log.isDebugEnabled()) {
                log.debug("No authorizationCache instance set.  Checking for a cacheManager...");
            }

            CacheManager cacheManager = this.getCacheManager();

            if (cacheManager != null) {
                String cacheName = this.getAuthorizationCacheName();
                if (log.isDebugEnabled()) {
                    log.debug("CacheManager [" + cacheManager + "] has been configured.  Building " + "authorization cache named [" + cacheName + "]");
                }
                authorizationCache = cacheManager.getCache(cacheName);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("No cache or cacheManager properties have been set.  Authorization cache cannot " + "be obtained.");
                }
            }
        }

        return authorizationCache;
    }

}
