# OpenID custom claims

soda4LCA need some information from the identity provider to create a user session with correct profile information. 

If the token obtained from the identity provider:

1. contains the claims defined below.
2. public key in the discovery document has been serialized as a java object and placed in the keys path with suffix of `public.key`.

Then the same token can be also used as a Bearer token to communicate with soda4LCA rest endpoint. 

You can find and up-to-date list of these claims in `UserOIDC.java`.

## Mandatory

### `"roles"`

JSON array of custom roles that correspond to shiro defined roles.

### `"permissions"`

JSON array of custom - shiro compatible - permissions that maps to the relevant stock access rights.

## Optional

* `"generateNewTokens"`
* `"ver"`
* `"address"`
* `"city"`
* `"zipCode"`
* `"street"`
* `"country"`
* `"organization"`
* `"organizationName"`
* `"organizationUnit"`
* `"organizationGroupNames"`
* `"userGroups"`
* `"userGroupName"`
* `"userGroupOrganizationName"`
* `"administratedOrganizationsNames"`
* `"email"`
* `"firstName"`
* `"lastName"`
* `"name"`
* `"title"`
* `"phone"`
* `"gender"`
* `"dspurpose"`
* `"institution"`
* `"jobPosition"`
* `"sector"`

## Sample Bearer JWT

Preferably, your identity provider admin should create the aforementioned custom claims under a separate scope to be as close as possible to this:

![sample token](https://user-images.githubusercontent.com/1815268/156834766-3982efb2-e4f0-4be2-a1b6-c93446aa9e08.png)


## References

* `CustomClaimsEnum.java`