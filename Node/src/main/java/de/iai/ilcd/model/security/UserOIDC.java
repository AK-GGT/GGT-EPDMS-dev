package de.iai.ilcd.model.security;

import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRight.AccessRightType;
import de.iai.ilcd.security.jwt.CustomClaimsEnum;
import de.iai.ilcd.security.role.RoleMapping;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oidc.profile.OidcProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserOIDC implements IUser {

    private static final Logger log = LoggerFactory.getLogger(UserOIDC.class);

    private UserProfile profile;
    private RoleMapping roleMapping;

    public UserOIDC(UserProfile profile) {
        this.setProfile(profile);
        this.roleMapping = ApplicationContextHolder.getApplicationContext().getBean(RoleMapping.class);
    }

    @Override
    public Long getId() {
        return Long.valueOf(profile.getId());
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public Address getAddress() {

        var street = profile.getAttribute(CustomClaimsEnum.STREETADDRESS.key);
        var city = profile.getAttribute(CustomClaimsEnum.CITY.key);
        var zip = profile.getAttribute(CustomClaimsEnum.ZIPCODE.key);
        var country = profile.getAttribute(CustomClaimsEnum.COUNTRY.key);

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
        return (String) profile.getAttribute(CustomClaimsEnum.EMAIL.key);
    }

    @Override
    public void setEmail(String email) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getFirstName() {
        return (String) profile.getAttribute(CustomClaimsEnum.FIRSTNAME.key);
    }

    @Override
    public void setFirstName(String firstName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getInstitution() {
        return (String) profile.getAttribute(CustomClaimsEnum.INSTITUTION.key);
    }

    @Override
    public void setInstitution(String institution) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPhone() {
        return (String) profile.getAttribute(CustomClaimsEnum.PHONE.key);
    }

    @Override
    public void setPhone(String phone) {
        // TODO Auto-generated method stub

    }

    @Override
    public Gender getGender() {
        var g = (String) profile.getAttribute(CustomClaimsEnum.GENDER.key);
        if (g == null) return null;
        var gender = Gender.fromValue(g);

        return gender;
    }

    @Override
    public void setGender(Gender gender) {
    }

    @Override
    public String getLastName() {
        return (String) profile.getAttribute(CustomClaimsEnum.LASTNAME.key);
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
        return (String) profile.getAttribute(CustomClaimsEnum.TITLE.key);
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public String getUserName() {
        return ((OidcProfile) profile).getDisplayName();
    }

    @Override
    public void setUserName(String userName) {

    }

    @Override
    public String getResetKey() {
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
        return profile.getUsername();
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

    @Override
    public List<UserGroup> getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDsPurpose() {
        return (String) profile.getAttribute(CustomClaimsEnum.DSPURPOSE.key);
    }

    @Override
    public void setDsPurpose(String dspurpose) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getJobPosition() {
        return (String) profile.getAttribute(CustomClaimsEnum.JOBPOSITION.key);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setApiKey(String apiKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getApiKeyExpiry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setApiKeyExpiry(Date apiKeyExpiry) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApiKeyExpiryString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean isApiKeyAllowed() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSector(List<String> sector) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSectorAsString() {
        return (String) profile.getAttribute(CustomClaimsEnum.SECTOR.key);
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
            var roles = profile.getRoles();
            return roles.contains("SUPER_ADMIN");
        } catch (Exception e) {
            log.warn("failed to parse roles from OIDC profile");
        }

        return false;
    }

    @Override
    public void setSuperAdminPermission(boolean superAdminPermission) {
        // TODO Auto-generated method stubs

    }

    @Override
    public boolean isPrivilegedUser() {
        return isSuperAdminPermission(); // || this.getProfile().getAttribute("ROLE").contains("ADMIN");
    }

    @Override
    public List<AbstractDataStock> getReadableStocks() {
        var dao = new CommonDataStockDao(); // TODO: move this up
        if (this.isPrivilegedUser()) return dao.getAll();
        var lstSar = dao.getAllReadable(getStockAccessRights());
        log.trace("readable stocks: {}", lstSar);
        return lstSar;
    }

    @Override
    public List<StockAccessRight> getStockAccessRights() {

        var erg = new ArrayList<StockAccessRight>();
        erg.addAll(stockAccessRightsfromPermissions(String.join(";", profile.getPermissions())));
        erg.addAll(stockAccessRightsfromRoles(String.join(",", profile.getRoles()))); // TODO: split by ;

        return erg;
    }

    public List<StockAccessRight> stockAccessRightsfromPermissions(String permissions) {
        List<StockAccessRight> erg = new ArrayList<StockAccessRight>();
        if (permissions != null) {
            for (var p : permissions.split(";")) {
                long stockid = -1;
                int protectionint = -1;
                var parts = p.split(":");
                if (parts.length == 3 && parts[1].trim().isEmpty()) continue;
                if (parts.length > 2 && !parts[2].contentEquals("") && !parts[2].contentEquals("*"))
                    stockid = Long.parseLong(parts[2].trim());
                if (parts.length > 1 && !parts[1].contentEquals("") && !parts[1].contentEquals("*"))
                    protectionint = ProtectionType.combine(parts[1].split(","));
                if (parts.length > 1 && parts[1].trim().contentEquals("*"))
                    protectionint = ProtectionType.combine(ProtectionType.values()); // all permissions
                if (stockid == -1 || protectionint == -1) continue;
                erg.add(new StockAccessRight(stockid, AccessRightType.User, -420, protectionint)); // not all users will have database id
                log.trace("permission: {} -> permissions: {}", p, permissions);
            }
        }
        return erg;
    }

    public List<StockAccessRight> stockAccessRightsfromRoles(String roles) {
        List<StockAccessRight> erg = new ArrayList<StockAccessRight>();

        if (roles != null) {
            for (var r : roles.split(",")) {
                var permissions = roleMapping.expand(r);
                erg.addAll(stockAccessRightsfromPermissions(permissions));
                log.trace("role: {} -> permissions: {}", r, permissions);
            }
        }

        return erg;
    }

    public List<StockAccessRight> stockAccessRightsfromAuthorities(List<String> authorities) {
        List<StockAccessRight> erg = new ArrayList<StockAccessRight>();

        if (authorities != null) {
            for (var a : authorities) {
                var permissions = roleMapping.expand(a);
                erg.addAll(stockAccessRightsfromPermissions(permissions));
                log.trace("authority: {} -> permissions: {}", a, permissions);
            }
        }

        return erg;

    }

    @Override
    public List<Organization> getAdministratedOrganizations() {
        // TODO Auto-generated method stub
        return null;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

}
