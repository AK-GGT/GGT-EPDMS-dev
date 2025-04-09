package de.iai.ilcd.model.security;

import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.security.StockAccessRight;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IUser {

    Long getId();

    void setId(Long id);

    Address getAddress();

    void setAddress(Address address);

    String getEmail();

    void setEmail(String email);

    String getFirstName();

    void setFirstName(String firstName);

    String getInstitution();

    void setInstitution(String institution);

    String getPhone();

    void setPhone(String phone);

    Gender getGender();

    void setGender(Gender gender);

    String getLastName();

    void setLastName(String lastName);

    String getPasswordHash();

    void setPasswordHash(String passwordHash);

    String getPasswordHashSalt();

    void setPasswordHashSalt(String passwordHashSalt);

    String getTitle();

    void setTitle(String title);

    String getUserName();

    void setUserName(String userName);

    String getResetKey();

    void setResetKey(String resetKey);

    Date getResetTimestamp();

    void setResetTimestamp(Date resetTimestamp);

    String getDisplayName();

    String getRegistrationKey();

    void setRegistrationKey(String registrationKey);

    boolean isActivated();

    List<UserGroup> getGroups();

    String getDsPurpose();

    void setDsPurpose(String dspurpose);

    String getJobPosition();

    void setJobPosition(String jobPosition);

    Boolean getAcceptedAdditionalTerms();

    void setAcceptedAdditionalTerms(Boolean acceptedAdditionalTerms);

    Map<String, Boolean> getAcceptedAdditionalTermsMap();

    void setAcceptedAdditionalTermsMap(Map<String, Boolean> acceptedAdditionalTermsMap);

    String getApiKey();

    void setApiKey(String apiKey);

    Date getApiKeyExpiry();

    void setApiKeyExpiry(Date apiKeyExpiry);

    String getApiKeyExpiryString();

    Boolean isApiKeyAllowed();

    void setApiKeyAllowed(boolean apiKeyAllowed);

    void addToGroup(UserGroup group);

    void removeFromGroup(UserGroup group);

    void removeFromAllGroups();

    Organization getOrganization();

    void setOrganization(Organization organization);

    boolean isPrivacyPolicyAccepted();

    void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted);

    Boolean isPasswordExpired();

    void setPasswordExpired(Boolean passwordExpired);

    Boolean getLicenseConditions();

    void setLicenseConditions(Boolean licenseConditions);

    List<String> getSector();

    void setSector(List<String> sector);

    String getSectorAsString();

    String getSectorOther();

    void setSectorOther(String sectorOther);

    int hashCode();

    boolean equals(Object obj);

    String toString();

    boolean isSuperAdminPermission();

    void setSuperAdminPermission(boolean superAdminPermission);

    /**
     * superset of {@link #isSuperAdminPermission()}
     *
     * @return True if the user is an admin or a super admin
     */
    boolean isPrivilegedUser();

    List<AbstractDataStock> getReadableStocks();

    List<StockAccessRight> getStockAccessRights();

    List<Organization> getAdministratedOrganizations();

}
