# OpenID Configuration for shiro.ini

soda4LCA relies on [pac4j](https://github.com/pac4j/pac4j) and [buji-pac4j](https://github.com/bujiio/buji-pac4j) to integrate openID capabilities into soda4LCA.


![pac4j-shiro](https://www.pac4j.org/img/logo-shiro.png)


## under [main] section

### Define new Realm

```ini
pac4jRealm = io.buji.pac4j.realm.Pac4jRealm
```

and then tell the securityManger about it:

```ini
securityManager.realms = $dbRealm, $bearerRealm, $bearerRealmOLD, $pac4jRealm
```

### Define OpenID client

A generic client for OpenID is provided by pac4j

```ini
oidClient = org.pac4j.oidc.client.OidcClient
```

then inject necessary configuration during node startup from the `ConfigurationService` using a custom class `OIDCConfig` in package `de.iai.ilcd.security.oidc`.

```ini
oidcConfig = de.iai.ilcd.security.oidc.OIDCConfig
oidClient = org.pac4j.oidc.client.OidcClient
oidClient.configuration = $oidcConfig
```

### Generate shiro roles and permissions from token

A generic authorization generator is provided by `buji` that generate roles and permission from token attributes.

A second custom roles authorizer is added to provide ability to contact a remote license server that returns extra roles that the identity provider is not aware of

```
# default authz generator
roleAdminAuthGenerator = org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator
roleAdminAuthGenerator.roleAttributes = roles
roleAdminAuthGenerator.permissionAttributes = permissions

# a second custom one for a customer with a vague business requirements
roleAdminAuthGenerator2 = de.iai.ilcd.security.oidc.CustomRoleAuthorizer
roleAdminAuthGenerator2.roleAttributes = authorities
roleAdminAuthGenerator2.permissionAttributes = permissions

oidClient.authorizationGenerators = $roleAdminAuthGenerator, $roleAdminAuthGenerator2
```

### Define callbackURL and callbackFilter

To make these values configurable AND keep using shiro.ini, we relay on `org.apache.shiro.util.Factory` to inject these values from the ConfigurationService during runtime:


```java
public class CallbackFilterDefaultUrl implements Factory<String> {

	@Override
	public String getInstance() {
		return ConfigurationService.INSTANCE.getOidcCallbackFilterDefaultUrl();
	}

}
```

```
callbackUrl = de.iai.ilcd.security.oidc.CallbackUrl
clients.callbackUrl = $callbackUrl
clients.clients = $oidClient
defaultUrl = de.iai.ilcd.security.oidc.CallbackFilterDefaultUrl
callbackFilter.defaultUrl = $defaultUrl
```



### Define Security filter

By default, it relies on the DefaultSecurityLogic which has the following behavior:

If the HTTP request matches the matchers configuration (or no matchers are defined), the security is applied. Otherwise, the user is automatically granted access.

1. First, if the user is not authenticated (no profile) and if some clients have been defined in the clients parameter, a login is tried for the direct clients.

2. Then, if the user has a profile, authorizations are checked according to the authorizers configuration. If the authorizations are valid, the user is granted access. Otherwise, a 403 error page is displayed.

3. Finally, if the user is not authenticated (no profile), he is redirected to the appropriate identity provider if the first defined client is an indirect one in the clients configuration. Otherwise, a 401 error page is displayed.


A generic SecurityFilter is provided by `buji`:

```ini
oidcSecurityFilter = io.buji.pac4j.filter.SecurityFilter
oidcSecurityFilter.config = $config
oidcSecurityFilter.clients = OidcClient
```

Notice: soda4LCA has a bean also called: `SecurityFilter` in `eu.europa.ec.jrc.lca.commons.security.filter`. This WILL cause problems if you try to springily the project. keep that in mind.

### Define central logout.

To handle the logout, a logout endpoint is necessary to perform:

1. the local logout by removing the pac4j profiles from the session
2. the central logout by calling the identity provider logout endpoint. This is the Single-Log-Out (= SLO) process.


A generic LogoutFilter is provided by `buji`:

```ini
pac4jLogout = io.buji.pac4j.filter.LogoutFilter
pac4jLogout.config = $config


pac4jCentralLogout = io.buji.pac4j.filter.LogoutFilter
pac4jCentralLogout.config = $config
pac4jCentralLogout.localLogout = false
pac4jCentralLogout.centralLogout = true
pac4jCentralLogout.logoutUrlPattern = /.*
```

## under [urls] section

### point security filter to `/oidc.xhml`

```ini
/oidc.xhtml = oidcSecurityFilter
```

### point `/postauth` to callback filter

```ini
/postauth/** = callbackFilter
/postauth = callbackFilter
```

### point `/logout` to pac4j's logout filter

```ini
/logout = logout
/pac4jLogout = pac4jLogout
/pac4jCentralLogout = pac4jCentralLogout
```

## References

* https://www.pac4j.org/docs/security-filter.html
* https://github.com/bujiio/buji-pac4j/wiki/
* https://github.com/bujiio/buji-pac4j/wiki/Logout-configuration
* https://www.pac4j.org/docs/logout-endpoint.html