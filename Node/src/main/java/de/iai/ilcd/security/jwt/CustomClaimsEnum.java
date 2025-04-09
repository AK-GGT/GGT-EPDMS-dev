package de.iai.ilcd.security.jwt;

/**
 * Ensure that claims during generation phase {@link JWTController#generateToken(String)}
 * matches the claims during Authorization phase {@link BearerAuthorization#BearerAuthorization(io.jsonwebtoken.Claims)}
 *
 * @author MK
 * @since soda4LCA 7.0.0
 */
public enum CustomClaimsEnum { // TODO: add all claims not just custom ones?

    VERSION("ver"),
    PERMISSIONS("permissions"),
    ROLES("roles"),
    AUTHORITIES("authorities"),
    GENERATETOKEN("generateNewTokens"),

    ADDRESS("address"),
    CITY("city"),
    ZIPCODE("zipCode"),
    STREETADDRESS("street"),
    COUNTRY("country"),

    // Organziation (Map)
    ORGANIZATION("organization"),
    //	ORGANIZATIONID("organizationID"),
    ORGANIZATIONName("organizationName"),
    ORGANIZATIONUnit("organizationUnit"),
    //	ORGANIZATIONADDRESS("organizationAddress"),
    ORGANIZATIONGroupNames("organizationGroupNames"),

    // UserGroups (List.of(Map))
    USERGROUPS("userGroups"),
    //	USERGROUPID("userGroupID"),
    USERGROUPNAME("userGroupName"),
    USERGROUPORGNAME("userGroupOrganizationName"),

    ADMINISTRATEDORGANIZATIONS("administratedOrganizationsNames"),

    EMAIL("email"),
    FIRSTNAME("firstName"),
    LASTNAME("lastName"),
    NAME("name"),
    TITLE("title"),
    PHONE("phone"),
    GENDER("gender"),
    DSPURPOSE("dspurpose"),
    INSTITUTION("institution"),
    JOBPOSITION("jobPosition"),
    SECTOR("sector");


    public String key;

    CustomClaimsEnum(String key) {
        this.key = key;
    }
}
