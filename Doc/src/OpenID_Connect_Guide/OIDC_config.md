# OpenID Configuration for soda4LCA

![openid-logo](https://openid.net/wordpress-content/uploads/2014/09/openid-r-logo-900x360.png)

soda4LCA supports authenticating users using an external identity provider that supports OpenID Connect 2.0.

This feature can be enabled be populating the relevant configuration keys (feature.oidc.*) in the soda4LCA.properties.

![pac4j-stateful](https://www.pac4j.org/img/pac4j-stateful.png)

When everything is configured correctly, a new button (OIDC login) will appear on the bottom left next to login.

![soda4lca-oidclogin](https://user-images.githubusercontent.com/1815268/156820848-fa9cc9af-5b45-4628-96dc-d5a5db8aa2b6.png)


## Relevant Configurations

### `feature.oidc.baseURI`

Point to the location of the identity provider auth URI. (https://xyz.com/authserver/default)

### `feature.oidc.realm`

Name the realm where the identity provider has created the client application for soda4LCA (soda-relm)

### `feature.oidc.discoveryURI`

Point to the location of the discovery document of the identity provider.

The discovery document is expected to contain all the mandatory field as defined in the spec.

### `feature.oidc.clientID`

Name of the client application that the identity provider has created for soda4LCA (ex: soda4lca-client).

### `feature.oidc.secret`

Secret for the previously defined client application in `client_secret_basic`.

### `feature.oidc.clientAuthenticationMethod`

Authentication method for the previously defined client application. (client_secret_basic)

### `feature.oidc.preferredJwsAlgorithm`

Identity provider MUST support signing in RS256.

### `feature.oidc.scope`

Scopes that the previously defined client has access to.

The default "openid" has to be one of them. plus separated values. (openid+email+profile)

### `feature.oidc.callbackUrl`

soda4LCA Node base URL + the location defined in "shiro.ini" next to callbackFilter (example: http://soda4lca.io/postauth).

This must be also configured on the identity provider level.

### `feature.oidc.callbackFilterDefaultUrl`

https://oidc-auth-test.soda4lca.io

soda4LCA Node base URL.

### `feature.oidc.roleMappings`

Additional datastock permissions to be defined based on roles obtained from the identity provider. (ROLE_A, 3, READ, EXPORT; ROLE_B, 4, READ, EXPORT)

`ROLE, STOCK_ID, PROTECTION_TYPE... ; ROLE, STOCK_ID, PROTECTION_TYPE...; ROLE, STOCK_ID, PROTECTION_TYPE...`


The example above gives authenticate users who have:

1. `ROLE_A` as a `role` read and export permission to datastock with id 3 (database id, not UUID).
2. `ROLE_B` as a `role` read and export permission to datastock with id 4 (database id, not UUID).


### `feature.oidc.externalRolesUrl`

https://xyz.com/current-user/c293a9bc-591f-45a5-acfd-31250c92450c/license-info

Point to the location of a license server for merging in extra roles.


Send the access token obtained from the identity provider (in the Authorization: Bearer ) to the URL of the license to obtain additional roles. 

The expected response must contain an array of roles:
```
{
    "roles": [
        "ADMIN",
        "USER",
        "ROLE_B"
    ]
}
```

This response will be parsed and the following roles will be merged-in with the roles obtained from the identity provider.

1. `ADMIN`
2. `USER`
3. `ROLE_B`

## Communicate with soda4LCA REST endpoint

If the token obtained from the identity provider:

1. contains the claims defined in [OIDC_spec.md](./OIDC_spec.md).
2. signed with `RS256`.
3. public key in the discovery document has been serialized as a java object and placed in the keys path with suffix of `public.key`.

Then the same token can be also used as a Bearer token to communicate with soda4LCA rest endpoint.


## Serialize RSA public key

In order to maintain backward compatibly with some existing legacy functionalities; the public key has to be serialized as follows:

1. Grab the public exponent and the modules from the discovery document (under `/.well-known/jwks.json`).

![jwts-json](https://user-images.githubusercontent.com/1815268/156939094-6e27b8d8-1e79-4d5c-84ef-a9eed5c66322.png)

2. Use the following routine (in `KeyGenerator.java`) to parse the base64 URL into java's `RSAPublicKeySpec`.

```java
	/**
	 * 
	 * @param modulus_base64URL
	 *            The "n" (modulus) parameter contains the modulus value for the
	 *            RSA public key. It is represented as a Base64urlUInt-encoded
	 *            value.
	 * @param e_base64URL
	 *            The "e" (exponent) parameter contains the exponent value for
	 *            the RSA public key. It is represented as a
	 *            Base64urlUInt-encoded value.
	 * @return
	 */
	public static RSAPublicKeySpec getPublicKey(String modulus_base64URL, String e_base64URL) {		
		var mod = new BigInteger(1, Base64.getUrlDecoder().decode(modulus_base64URL.trim().getBytes()));
		var e = new BigInteger(Base64.getUrlDecoder().decode(e_base64URL.trim().getBytes()));
		return new RSAPublicKeySpec(mod, e);
	}
```

3. Use the following routine (in `FileKeyLocation.java`) to write the serialize the object to the `-Dkey.path` location on disk.

```java
	private void storePublic(RSAPublicKeySpec publicKey) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("storing public key at " + getLocation() + PUBLIC_KEY_FILE_NAME);
		ObjectOutputStream oout = null;
		try {
			oout = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(getLocation() + PUBLIC_KEY_FILE_NAME)));
			BigInteger mod = publicKey.getModulus();
			BigInteger exp = publicKey.getPublicExponent();
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (FileNotFoundException e) {
			LOGGER.error("[storePublic] " + e.getMessage());
		} catch (IOException e) {
			LOGGER.error("[storePublic] " + e.getMessage());
		} catch (Exception e) {
			LOGGER.error("[storePublic]", e);
		} finally {
			try {
				if (oout != null) {
					oout.close();
				}
			} catch (IOException e) {
				LOGGER.error("[storePublic]", e);
			}
		}
	}
```

## References

* https://www.pac4j.org/docs/clients/openid-connect.html
* https://github.com/bujiio/buji-pac4j/wiki
* https://openid.net/specs/openid-connect-core-1_0.html
* https://openid.net/specs/openid-authentication-2_0-12.html
