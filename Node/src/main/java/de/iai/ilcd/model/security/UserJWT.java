package de.iai.ilcd.model.security;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.OrganizationDao;
import de.iai.ilcd.model.dao.UserGroupDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.security.ProtectableType;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRight.AccessRightType;
import de.iai.ilcd.security.jwt.CustomClaimsEnum;
import de.iai.ilcd.security.role.RemoteRole;
import de.iai.ilcd.security.role.RoleMapping;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Setters are left empty. mutating the token requires a new signature.
 *
 * @author MK
 * @since soda4LCA 7.0.0
 */
public class UserJWT implements IUser {

    private static final Logger log = LoggerFactory.getLogger(UserJWT.class);

    private Claims tokenClaims;

    private List<String> extra_permissions;

    public UserJWT(Claims tokenClaims, String rawToken) {
        this.tokenClaims = tokenClaims;
        this.extra_permissions = new LinkedList<>();

        var remote = ApplicationContextHolder.getApplicationContext().getBean(RemoteRole.class);
        var mapping = ApplicationContextHolder.getApplicationContext().getBean(RoleMapping.class);

        // TODO: remove this temp workaround
        if (rawToken.contains("InN1aHUtZGVmYXVsdC1rZXktaWQif")) { // partial kid in base64
            var extra_roles = remote.licenseRoles(ConfigurationService.INSTANCE.getOidcExternalRolesUrl(), rawToken);
            for (var r : extra_roles)
                this.extra_permissions.add(mapping.expand(r));
        }

        // add READ and EXPORT permissions for data stocks configured as public
        var public_datastocks_id = ConfigurationService.INSTANCE.getAPIPublicDataStocks()
                .stream()
                .map(this::getDatastockUUID)
                .filter(v -> v != -420)
                .map(erg -> erg.toString())
                .collect(Collectors.toList());

        // Give READ and EXPORT permission to public datastocks
        for (var id : public_datastocks_id)
            this.extra_permissions.add(
                    SecurityUtil.toWildcardString(
                            ProtectableType.STOCK,
                            new ProtectionType[]{ProtectionType.READ, ProtectionType.EXPORT},
                            id)
            );
    }

    @Override
    public Long getId() {
        return -420L;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public Address getAddress() {

        var street = tokenClaims.getOrDefault(CustomClaimsEnum.STREETADDRESS.key, null);
        var city = tokenClaims.getOrDefault(CustomClaimsEnum.CITY.key, null);
        var zip = tokenClaims.getOrDefault(CustomClaimsEnum.ZIPCODE.key, null);
        var country = tokenClaims.getOrDefault(CustomClaimsEnum.COUNTRY.key, null);

        var add = new Address();

        if (city != null) add.setCity((String) city);
        if (country != null) add.setCountry((String) country);
        if (street != null) add.setStreetAddress((String) street);
        if (zip != null) add.setZipCode((String) zip);

        return add;
    }

    @Override
    public void setAddress(Address address) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getEmail() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.EMAIL.key, "");
    }

    @Override
    public void setEmail(String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFirstName() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.FIRSTNAME.key, "");
    }

    @Override
    public void setFirstName(String firstName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getInstitution() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.INSTITUTION.key, "");
    }

    @Override
    public void setInstitution(String institution) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPhone() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.PHONE.key, "");
    }

    @Override
    public void setPhone(String phone) {
        // TODO Auto-generated method stub

    }

    @Override
    public Gender getGender() {
        var g = (String) tokenClaims.getOrDefault(CustomClaimsEnum.GENDER.key, null);
        if (g == null) return null;
        var gender = Gender.fromValue(g);

        return gender;
    }

    @Override
    public void setGender(Gender gender) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLastName() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.LASTNAME.key, "");
    }

    @Override
    public void setLastName(String lastName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPasswordHash() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPasswordHash(String passwordHash) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPasswordHashSalt() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPasswordHashSalt(String passwordHashSalt) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getTitle() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.TITLE.key, "");
    }

    @Override
    public void setTitle(String title) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getUserName() {
        return tokenClaims.getSubject();
    }

    @Override
    public void setUserName(String userName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getResetKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResetKey(String resetKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getResetTimestamp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResetTimestamp(Date resetTimestamp) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDisplayName() {
        return getTitle() + " " + getFirstName() + " " + getLastName();
    }

    @Override
    public String getRegistrationKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRegistrationKey(String registrationKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isActivated() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * //	 * @see JWTController#appendPersonalInfo(JwtBuilder, IUser)
     */
    @Override
    public List<UserGroup> getGroups() {

        var dao = new UserGroupDao();
        var groupnames = (String) tokenClaims.getOrDefault(CustomClaimsEnum.USERGROUPNAME.key, null);
        if (groupnames == null) return List.of();

        return Arrays.asList(groupnames.split(","))
                .stream()
                .map(name -> dao.getGroup(name))
                .filter(x -> x != null)
                .collect(Collectors.toList());
    }

    @Override
    public String getDsPurpose() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.DSPURPOSE.key, "");
    }

    @Override
    public void setDsPurpose(String dspurpose) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getJobPosition() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.JOBPOSITION.key, "");
    }

    @Override
    public void setJobPosition(String jobPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getAcceptedAdditionalTerms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAcceptedAdditionalTerms(Boolean acceptedAdditionalTerms) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Boolean> getAcceptedAdditionalTermsMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAcceptedAdditionalTermsMap(Map<String, Boolean> acceptedAdditionalTermsMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApiKey() {
        return tokenClaims.toString();
    }

    @Override
    public void setApiKey(String apiKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getApiKeyExpiry() {
        return tokenClaims.getExpiration();
    }

    @Override
    public void setApiKeyExpiry(Date apiKeyExpiry) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApiKeyExpiryString() {
        return tokenClaims.getExpiration().toString();
    }

    @Override
    public Boolean isApiKeyAllowed() {
        return (boolean) tokenClaims.getOrDefault(CustomClaimsEnum.GENERATETOKEN.key, false);
    }

    @Override
    public void setApiKeyAllowed(boolean apiKeyAllowed) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addToGroup(UserGroup group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFromGroup(UserGroup group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFromAllGroups() {
        // TODO Auto-generated method stub

    }

    @Override
    public Organization getOrganization() {
        var name = (String) tokenClaims.getOrDefault(CustomClaimsEnum.ORGANIZATIONName.key, null);
        if (name == null) return null;

        var dao = new OrganizationDao();
        return dao.getByName(name);
    }

    @Override
    public void setOrganization(Organization organization) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPrivacyPolicyAccepted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean isPasswordExpired() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPasswordExpired(Boolean passwordExpired) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getLicenseConditions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLicenseConditions(Boolean licenseConditions) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getSector() {
        return Arrays.asList(this.getSectorAsString().split(","));
    }

    @Override
    public void setSector(List<String> sector) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSectorAsString() {
        return (String) tokenClaims.getOrDefault(CustomClaimsEnum.SECTOR.key, "");
    }

    @Override
    public String getSectorOther() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSectorOther(String sectorOther) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSuperAdminPermission() {
        try {
            var roles = tokenClaims.get(CustomClaimsEnum.ROLES.key, List.class);
            return roles.contains("SUPER_ADMIN");
        } catch (Exception e) {
            log.warn("failed to parse roles from token");
        }

        return false;
    }

    @Override
    public void setSuperAdminPermission(boolean superAdminPermission) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPrivilegedUser() {
        return isSuperAdminPermission();
    }

    /**
     * -1 is the numerical equivalent for wildcard
     * <p>
     * //	 * @see BearerAuthorization#getObjectPermissions()
     * //	 * @see BearerAuthorization#getStringPermissions()
     */
    @Override
    public List<StockAccessRight> getStockAccessRights() {
        var token_permissions = tokenClaims.get(CustomClaimsEnum.PERMISSIONS.key);
        var token_roles = tokenClaims.get(CustomClaimsEnum.ROLES.key);
        var token_authorities = tokenClaims.get(CustomClaimsEnum.AUTHORITIES.key);
        log.debug("token permissions: {}", token_permissions);
        log.debug("token roles: {}", token_roles);
        log.debug("token authorities: {}", token_authorities);
        List<String> permissions = new LinkedList<>();
        if (token_permissions instanceof List)
            permissions = (List<String>) token_permissions;
        else if (token_permissions == null) ;
        else { // OIDC // TODO: refactor this out of here
//			permissions = Arrays.asList(((String) token_permissions).split(";"));
            var oidcUser = new UserOIDC(null);

            List<StockAccessRight> fromPermissions = oidcUser.stockAccessRightsfromPermissions((String) token_permissions);
            log.trace("stock access rights from permissions: {}", fromPermissions);

            List<StockAccessRight> fromRoles = oidcUser.stockAccessRightsfromRoles((String) token_roles);
            log.trace("stock access rights from roles: {}", fromRoles);

            List<StockAccessRight> fromAuthorities = oidcUser.stockAccessRightsfromAuthorities((List<String>) token_authorities);
            log.trace("stock access rights from authorities: {}", fromAuthorities);

            List<StockAccessRight> result = new ArrayList<>();
            result.addAll(fromPermissions);
            result.addAll(fromRoles);
            result.addAll(fromAuthorities);

            return result;
        }


        permissions.addAll(extra_permissions);

        List<StockAccessRight> erg = new ArrayList<StockAccessRight>();
        // TODO: gets it's own place away from here
        for (var per : permissions)
            for (var p : per.split(";")) { // multiple permissions can be delimited by semi-colons // TODO: no longer allowed, remove this
                long stockid = -1;
                int protectionint = -1;
                var parts = p.split(":");
                if (parts.length == 3 && parts[1].trim().isEmpty()) continue;
                if (parts.length > 2 && !parts[2].contentEquals("") && !parts[2].contentEquals("*"))
                    stockid = Long.parseLong(parts[2]);
                if (parts.length > 1 && !parts[1].contentEquals("") && !parts[1].contentEquals("*"))
                    protectionint = ProtectionType.combine(parts[1].split(","));
                if (parts.length > 1 && parts[1].trim().contentEquals("*"))
                    protectionint = ProtectionType.combine(ProtectionType.values()); // all permissions
                if (stockid == -1 || protectionint == -1) continue;
                erg.add(new StockAccessRight(stockid, AccessRightType.User, -420, protectionint)); // not all users will have database id
            }

        return erg;
    }

    @Override
    public List<AbstractDataStock> getReadableStocks() {
        var dao = new CommonDataStockDao(); // TODO: move this up

        if (this.isPrivilegedUser()) return dao.getAll();
        return dao.getAllReadable(getStockAccessRights());
    }

    /**
     * returns a list of getAdministratedOrganizations that exists on this Node.
     * <p>
     * if the dao couldn't find an organization with given name, it's going to ignore it.
     */
    @Override
    public List<Organization> getAdministratedOrganizations() {
        var dao = new OrganizationDao();
        var orgs = (String) tokenClaims.getOrDefault(CustomClaimsEnum.ADMINISTRATEDORGANIZATIONS.key, null);
        if (orgs == null)
            return List.of();

        return Arrays.asList(orgs.split(",")).stream()
                .map(name -> dao.getByName(name))
                .filter(x -> x != null)
                .collect(Collectors.toList());
    }

    /**
     * @param uuid of an existing datastock
     * @return sql database id (not uuid) of given datastock name
     */
    private Long getDatastockUUID(String uuid) {
        var dao = new CommonDataStockDao(); // TODO: avoid wasteful instantiation
        try {
            var ds = dao.getDataStockByUuid(uuid.trim());
            return ds.getId();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Cannot find any datastock with a UUID: " + uuid);
            return -420L;
        }
    }

}
