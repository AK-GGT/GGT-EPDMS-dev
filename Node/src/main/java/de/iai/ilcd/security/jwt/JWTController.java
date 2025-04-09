package de.iai.ilcd.security.jwt;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.security.*;
import de.iai.ilcd.rest.AuthenticateResource;
import de.iai.ilcd.security.ProtectableType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.security.sql.IlcdShiroPermissions;
import de.iai.ilcd.webgui.controller.admin.UserHandler;
import eu.europa.ec.jrc.lca.commons.security.encryption.CipherUtil;
import eu.europa.ec.jrc.lca.commons.security.encryption.FileKeyLocation;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Controller("sodaJWTController")
public class JWTController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTController.class);
    private final FileKeyLocation fileKeyLocation;

    @Autowired
    public JWTController(FileKeyLocation fileKeyLocation) {
        this.fileKeyLocation = fileKeyLocation;
    }

    /**
     * @param userName
     * @param random
     * @see UserHandler
     * @see AuthenticateResource
     */
    @Deprecated
    public String generateTokenOLD(String userName, String random) {
        var secret = DatatypeConverter.parseBase64Binary("secret");
        var sigAlg = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(secret, sigAlg.getJcaName());
        Long ttl = ConfigurationService.INSTANCE.getTokenTTL() * 1000;

        var jwts = Jwts.builder().setSubject(userName).claim("random", random);

        if (ConfigurationService.INSTANCE.isTokenTTL())
            jwts.setExpiration(new Date(System.currentTimeMillis() + ttl));

        return jwts.signWith(sigAlg, signingKey).compact();
    }

    @Deprecated
    public Claims parseClaimsJwsOLD(String tokenOLD) {
        var secret = DatatypeConverter.parseBase64Binary("secret");
        var sigAlg = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(secret, sigAlg.getJcaName());

        return Jwts.parser()//.parserBuilder()
                .setSigningKey(signingKey)
//				.build()
                .parseClaimsJws(tokenOLD)
                .getBody();
    }

    /**
     * populate claims here to be read by BearerAuthorization
     * during Authorization phase.
     * <p>
     * Make clams generated here matches whatever BearerAuthorization
     * is trying to read
     *
     * @param user
     * @return A compact URL-safe JWT string.
     * @see BearerAuthorization
     */
    public String generateToken(IUser user) {
        return generateToken(user, System.currentTimeMillis());
    }

    /**
     * Same as {@link JWTController#generateToken(IUser)} plus ability to specify issue time
     *
     * @param user
     * @param issue_time unix time in x 1000
     * @return A compact URL-safe JWT string.
     */
    public String generateToken(IUser user, long issue_time) {

        // prevent issue tokens in the future
        assert issue_time <= System.currentTimeMillis();

        var signingKey = CipherUtil.getKey(fileKeyLocation.getPrivateKey());
        var sigAlg = SignatureAlgorithm.RS256;
        var ttl = ConfigurationService.INSTANCE.getTokenTTL() * 1000;

        var token = Jwts.builder()
                .setSubject(user.getUserName())
                .setIssuer(ConfigurationService.INSTANCE.getNodeId())
                .setAudience("any")
                .claim(CustomClaimsEnum.VERSION.key, ConfigurationService.INSTANCE.getVersionTag())
                .claim(CustomClaimsEnum.PERMISSIONS.key, getPermissions(user))
                .claim(CustomClaimsEnum.ROLES.key, getRoles(user))
                .setIssuedAt(new Date(issue_time))
                .setExpiration(new Date(issue_time + ttl))
                .signWith(sigAlg, signingKey);

        appendPersonalInfo(token, user);

        return token.compact();
    }

    /**
     * <ol>
     * 		<li>Grab default permissions</li>
     * 		<li>Grab existing permissions (in db) for given username</li>
     * </ol>
     *
     * @return
     * @see IlcdAuthorizationInfo#permissions
     */

    private Set<String> getPermissions(IUser currentUser) { // TODO: split by ";" instead

        // default permissions
        var permissions = new HashSet<String>();

        // Existing stock access rights for current user
        if (!(currentUser instanceof User)) return permissions;

        for (Permission p : IlcdShiroPermissions.read(currentUser))
            if (currentUser.isPrivilegedUser() || !p.toString().contains(":") || !p.toString().split(":")[1].isEmpty())
                permissions.add(p.toString());

        return permissions;
    }

    // from sql db
    private Set<String> getRoles(IUser user) {

        if (!(user instanceof User)) return Set.of();
        var permissions = IlcdShiroPermissions.read(user);

        if (permissions.contains(new WildcardPermission(SecurityUtil.toWildcardString("*", "*", "*"))))
            return Set.of("SUPER_ADMIN");

        if (permissions.contains(new WildcardPermission(ProtectableType.ADMIN_AREA.name())))
            return Set.of("ADMIN");

        return Set.of();
    }

    private void appendPersonalInfo(JwtBuilder token, IUser currentUser) {

        var address = Jwts.claims();
        address.put(CustomClaimsEnum.CITY.key, Optional.ofNullable(currentUser)
                .map(IUser::getAddress)
                .map(Address::getCity)
                .orElse(null));
        address.put(CustomClaimsEnum.ZIPCODE.key, Optional.ofNullable(currentUser)
                .map(IUser::getAddress)
                .map(Address::getZipCode)
                .orElse(null));
        address.put(CustomClaimsEnum.COUNTRY.key, Optional.ofNullable(currentUser)
                .map(IUser::getAddress)
                .map(Address::getCountry)
                .orElse(null));
        address.put(CustomClaimsEnum.STREETADDRESS.key, Optional.ofNullable(currentUser)
                .map(IUser::getAddress)
                .map(Address::getStreetAddress)
                .orElse(null));


        var organization = Jwts.claims();

        organization.put(CustomClaimsEnum.ORGANIZATIONName.key, Optional.ofNullable(currentUser)
                .map(IUser::getOrganization)
                .map(Organization::getName)
                .orElse(null));

        organization.put(CustomClaimsEnum.ORGANIZATIONUnit.key, Optional.ofNullable(currentUser)
                .map(IUser::getOrganization)
                .map(Organization::getOrganisationUnit)
                .orElse(null));

        // comma separated groupnames
        organization.put(CustomClaimsEnum.ORGANIZATIONGroupNames.key, Optional.ofNullable(currentUser)
                .map(IUser::getOrganization)
                .map(Organization::getGroups)
                .map(ls -> ls.stream()
                        .map(l -> l.getDisplayName())
                        .collect(Collectors.joining(",")))
                .orElse(null));


        // user groups
        var userGroups = new LinkedList<Claims>();
        for (var g : currentUser.getGroups()) {
            var userGroup = Jwts.claims();
            userGroup.put(CustomClaimsEnum.USERGROUPNAME.key, g.getGroupName());
            userGroup.put(CustomClaimsEnum.USERGROUPORGNAME.key,
                    Optional.ofNullable(g)
                            .map(UserGroup::getOrganization)
                            .map(Organization::getName)
                            .orElse(null)
            );
            userGroups.add(userGroup);
        }


        //getAdministratedOrganizations
        var administratedOrganizations = currentUser.getAdministratedOrganizations().stream().map(Organization::getName).collect(Collectors.joining(","));


        // TODO: continue the rest
        token.claim(CustomClaimsEnum.EMAIL.key, currentUser.getEmail());
        token.claim(CustomClaimsEnum.TITLE.key, currentUser.getTitle());
        token.claim(CustomClaimsEnum.FIRSTNAME.key, currentUser.getFirstName());
        token.claim(CustomClaimsEnum.LASTNAME.key, currentUser.getLastName());
        token.claim(CustomClaimsEnum.GENERATETOKEN.key, false); // TODO: when should a token generate a token?
        token.claim(CustomClaimsEnum.JOBPOSITION.key, currentUser.getJobPosition());
        token.claim(CustomClaimsEnum.ADDRESS.key, address);
        token.claim(CustomClaimsEnum.ORGANIZATION.key, organization);
        token.claim(CustomClaimsEnum.USERGROUPS.key, userGroups);
        token.claim(CustomClaimsEnum.ADMINISTRATEDORGANIZATIONS.key, administratedOrganizations);
        token.claim(CustomClaimsEnum.PHONE.key, currentUser.getPhone());
        token.claim(CustomClaimsEnum.DSPURPOSE.key, currentUser.getDsPurpose());
        token.claim(CustomClaimsEnum.SECTOR.key, currentUser.getSectorAsString());
        token.claim(CustomClaimsEnum.INSTITUTION.key, currentUser.getInstitution());
    }

    /**
     * <p>
     * Scans key directory for all public keys that ends with "public.key"
     * and tries to validate the signature against any of them.
     * </p>
     *
     * <p>
     * This give the ability to accept tokens issued by different nodes.
     * </p>
     *
     * <p>
     * Example: NodeA and NodeB are friends. NodeB wants to let the users of NodeA
     * access NodeB's datastocks and datasets.
     * <p>
     * NodeB asks for NodeA's public key and adds it to the key directory.
     * </p>
     *
     * <p>
     * This relation can uni-directional or multi-directional
     * </p>
     *
     * @param token JWT with the proper claims and signed with a known public key.
     * @return Claims object (a map of key/value pairs)
     * @throws AuthenticationException if we couldn't validation against any public key.
     */
    // TODO: how many times this is called?
    // TODO: cachable?
    // TODO: skip verify?
    public Claims parseClaimsJws(String token) throws AuthenticationException {

        var dir = new File(fileKeyLocation.getLocation());
        var publickeysfiles = dir.listFiles((d, name) -> name.endsWith("public.key"));

        if (publickeysfiles == null) {
            var msg = "Cannot find any file that end with 'public.key' in " + fileKeyLocation.getLocation();
            LOGGER.info(msg);
            throw new AuthenticationException(msg);
        }
        for (var f : publickeysfiles) {
            var key = CipherUtil.getKey(fileKeyLocation.getPublicKey(f.getAbsolutePath()));

            LOGGER.debug("verifying with key {}", f.getAbsolutePath());

            try {
                return Jwts.parser()//.parserBuilder()
                        .setSigningKey(key)
//						.build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                LOGGER.debug("Public key {} cannot verify given token", f);
            }
        }

        LOGGER.warn("Token doesn't match any of accessible public key");

        throw new AuthenticationException("Failed to match token with any public key");
    }

}
