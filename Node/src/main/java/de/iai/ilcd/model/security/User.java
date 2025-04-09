package de.iai.ilcd.model.security;

import de.iai.ilcd.model.dao.CommonDataStockDao;
import de.iai.ilcd.model.dao.OrganizationDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRightDao;
import de.iai.ilcd.util.SodaUtil;
import org.apache.commons.lang.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "user")
public class User implements ISecurityEntity, Serializable, IUser {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userName;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "PASSWORD_HASH_SALT")
    private String passwordHashSalt;

    private String firstName;

    private String lastName;

    private String title;

    @Enumerated(EnumType.STRING)
    private Gender gender = null;

    private String institution;

    private String phone;

    private String email;

    private String registrationKey;

    private String dsPurpose;

    private String jobPosition;

    @Column(unique = true)
    private String apiKey;

    @Column(name = "apikeyexpiry")
    private Date apiKeyExpiry;

    @Column(name = "apikeyallowed")
    private Boolean apiKeyAllowed = true;

    @Transient
    private Boolean licenseConditions = false;

    @Column(name = "acceptedAddTerms")
    private Boolean acceptedAdditionalTerms = false;

    @Embedded
    private Address address = new Address();

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    // Does not contain CascadeType.REMOVE
    private List<UserGroup> groups = new ArrayList<UserGroup>();

    @Basic
    @Column(name = "super_admin_permission")
    private boolean superAdminPermission;

    /**
     * The organization of the user
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "organization", nullable = true)
    private Organization organization;

    @Basic
    @Column(name = "privacy_policy_accepted")
    private Boolean privacyPolicyAccepted;

    @Basic
    @Column(name = "password_expired")
    private Boolean passwordExpired;


    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "user_sector", joinColumns = @JoinColumn(name = "User_ID"))
    private List<String> sector = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "user_acceptedaddterms", joinColumns = @JoinColumn(name = "User_ID"))
    @MapKeyColumn(name = "additionalterm")
    @Column(name = "accepted")
    private Map<String, Boolean> acceptedAdditionalTermsMap = new HashMap<String, Boolean>();

    @Basic
    @Column(name = "sector_other")
    private String sectorOther;

    @Column(name = "resetkey")
    private String resetKey;

    @Column(name = "resettimestamp")
    private Date resetTimestamp;


    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHashSalt() {
        return this.passwordHashSalt;
    }

    public void setPasswordHashSalt(String passwordHashSalt) {
        this.passwordHashSalt = passwordHashSalt;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Date getResetTimestamp() {
        return resetTimestamp;
    }

    public void setResetTimestamp(Date resetTimestamp) {
        this.resetTimestamp = resetTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return this.userName;
    }

    public String getRegistrationKey() {
        return this.registrationKey;
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }

    public boolean isActivated() {
        return registrationKey == null;
    }

    public List<UserGroup> getGroups() {
        return this.groups;
    }

    protected void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }

    public String getDsPurpose() {
        return this.dsPurpose;
    }

    public void setDsPurpose(String dspurpose) {
        this.dsPurpose = dspurpose;
    }

    public String getJobPosition() {
        return this.jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public Boolean getAcceptedAdditionalTerms() {
        return acceptedAdditionalTerms;
    }

    public void setAcceptedAdditionalTerms(Boolean acceptedAdditionalTerms) {
        this.acceptedAdditionalTerms = acceptedAdditionalTerms;
    }

    public Map<String, Boolean> getAcceptedAdditionalTermsMap() {
        return this.acceptedAdditionalTermsMap;
    }

    public void setAcceptedAdditionalTermsMap(Map<String, Boolean> acceptedAdditionalTermsMap) {
        this.acceptedAdditionalTermsMap = acceptedAdditionalTermsMap;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Date getApiKeyExpiry() {
        return apiKeyExpiry;
    }

    public void setApiKeyExpiry(Date apiKeyExpiry) {
        this.apiKeyExpiry = apiKeyExpiry;
    }

    public String getApiKeyExpiryString() {
        if (apiKeyExpiry != null) {
            return apiKeyExpiry.toString();
        }
        return "---";
    }

    public Boolean isApiKeyAllowed() {
        return apiKeyAllowed;

    }

    public void setApiKeyAllowed(boolean apiKeyAllowed) {
        this.apiKeyAllowed = apiKeyAllowed;
    }

    /**
     * @param group
     */
    public void addToGroup(UserGroup group) {
        if (!this.groups.contains(group)) {
            this.groups.add(group);
            group.addUser(this);
        }
    }

    public void removeFromGroup(UserGroup group) {
        if (this.groups.contains(group)) {
            this.groups.remove(group);
        }
    }

    public void removeFromAllGroups() {
        List<UserGroup> allGroups = new ArrayList<UserGroup>();
        allGroups.addAll(this.groups);
        for (UserGroup group : allGroups) {
            group.removeUser(this); // this removes the group also locally
        }
    }

    /**
     * Get the organization of this user
     *
     * @return organization of this user
     */
    public Organization getOrganization() {
        return this.organization;
    }

    /**
     * Set the organization of this user
     *
     * @param organization organization of this user to set
     */
    public void setOrganization(Organization organization) {
        if (ObjectUtils.equals(this.organization, organization)) {
            return;
        }
        Organization oldOrg = this.organization;
        this.organization = organization;

        if (oldOrg != null) {
            oldOrg.removeUser(this);
        }

        if (this.organization != null) {
            this.organization.addUser(this);
        }
    }

    public boolean isPrivacyPolicyAccepted() {
        return this.privacyPolicyAccepted != null ? this.privacyPolicyAccepted : false;
    }

    public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted) {
        this.privacyPolicyAccepted = privacyPolicyAccepted;
    }

    public Boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(Boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public Boolean getLicenseConditions() {
        return licenseConditions;
    }

    public void setLicenseConditions(Boolean licenseConditions) {
        this.licenseConditions = licenseConditions;
    }

    public List<String> getSector() {
        return sector;
    }

    public void setSector(List<String> sector) {
        this.sector = new ArrayList<String>(sector);
    }

    public String getSectorAsString() {
        StringBuffer buf = new StringBuffer();
        for (String s : this.sector) {
            if (buf.length() != 0) {
                buf.append(";");
            }
            buf.append(s);
        }
        return buf.toString();
    }

    public String getSectorOther() {
        return sectorOther;
    }

    public void setSectorOther(String sectorOther) {
        this.sectorOther = sectorOther;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.userName == null) ? 0 : this.userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!this.userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.security.User[userName=" + this.userName + "]";
    }

    /**
     * Empty address means only <code>null</code> values in DB &rArr; address
     * is null after load from DB. If address is <code>null</code>, it will
     * be initialized with an empty address object.
     */
    @PostLoad
    @PostUpdate
    protected void postLoad() {
        if (this.address == null) {
            this.address = new Address();
        }
    }

    /**
     * Get the flag if this user has super admin permission
     *
     * @return <code>true</code> if super admin permission set, <code>false</code> otherwise
     */
    public boolean isSuperAdminPermission() {
        return this.superAdminPermission;
    }

    /**
     * Set the flag if this user has super admin permission (ignores incoming <code>false</code> for built-in admin user
     * (ID = {@link SodaUtil#ADMIN_ID})
     *
     * @param superAdminPermission new flag
     */
    public void setSuperAdminPermission(boolean superAdminPermission) {
        this.superAdminPermission = superAdminPermission || ObjectUtils.equals(this.getId(), SodaUtil.ADMIN_ID);
    }

    @Override
    public boolean isPrivilegedUser() {
        return isSuperAdminPermission();
    }

    @Override
    public List<StockAccessRight> getStockAccessRights() {
        StockAccessRightDao sarDao = new StockAccessRightDao();
        return sarDao.get(this);
    }

    @Override
    public List<AbstractDataStock> getReadableStocks() {
        CommonDataStockDao stockDao = new CommonDataStockDao();
        return stockDao.getAllReadable(this);
    }

    @Override
    public List<Organization> getAdministratedOrganizations() {
        OrganizationDao orgDao = new OrganizationDao();
        return orgDao.getAdministratedOrganizations(this);
    }

}
