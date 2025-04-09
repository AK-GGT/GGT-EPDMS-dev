package de.iai.ilcd.security.jwt;

import de.iai.ilcd.model.security.UserJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: why shiro trigger authz (unprompt) for this realm
// maybe side effect of multi-realm support? because BearerRealmOLD AuthorizingRealm
// which is called everytime is isPermit is invoked
// see: https://shiro.apache.org/authorization.html#modularrealmauthorizer
public class BearerAuthorization implements AuthorizationInfo {

    private static final Logger log = LoggerFactory.getLogger(UserJWT.class);

    private static final long serialVersionUID = 73167186247376405L;

    private Claims tokenClaims;
    private Collection<String> permissions;
    private Collection<String> roles;

    public BearerAuthorization() {
        // empty body
        this.tokenClaims = Jwts.claims();
        load();
    }

    public BearerAuthorization(Claims tokenClaims) {
        this.tokenClaims = tokenClaims;
        load();
    }

    private void load() { // TODO: load properly
        this.permissions = new HashSet<>();
        this.roles = new HashSet<>();
        this.permissions.addAll(
                (Collection<? extends String>) this.tokenClaims.getOrDefault(CustomClaimsEnum.PERMISSIONS.key, Set.of())
        );

        this.roles.addAll(
                (Collection<? extends String>) this.tokenClaims.getOrDefault(CustomClaimsEnum.ROLES.key, Set.of())
        );


        log.trace("permissions: {}", this.permissions);
        log.trace("roles: {}", this.roles);
    }

    @Override
    public Collection<String> getRoles() {
        return this.roles;
    }

    public void addRole(String e) {
        this.roles.add(e);
    }

    @Override
    public Collection<String> getStringPermissions() {
        return this.permissions;
    }

    public void addPermission(String e) {
        this.permissions.add(e);
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        // WildcardString -> WildcardPermission

        if (permissions == null) return List.of();
        return permissions.stream().map(x -> new WildcardPermission(x)).collect(Collectors.toSet());
    }

}
