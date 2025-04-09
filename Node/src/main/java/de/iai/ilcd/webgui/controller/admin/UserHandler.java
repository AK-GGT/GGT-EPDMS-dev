package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.Sectors;
import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.model.dao.GeographicalAreaDao;
import de.iai.ilcd.model.dao.OrganizationDao;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.dao.UserGroupDao;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.UserAccessBean;
import de.iai.ilcd.security.jwt.JWTController;
import de.iai.ilcd.security.sql.IlcdAuthorizationInfo;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import de.iai.ilcd.webgui.controller.url.URLGeneratorBean;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.extensions.event.ClipboardErrorEvent;
import org.primefaces.extensions.event.ClipboardSuccessEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class UserHandler extends AbstractAdminEntryHandler<User> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 8636585943955487402L;

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(UserHandler.class);
    /**
     * DAO for users
     */
    private final UserDao dao = new UserDao();
    /**
     * Verify password value
     */
    private String verifyPassword = "";
    /**
     * Plain password
     */
    private String plainPassword = null;
    /**
     * List of countries
     */
    private List<GeographicalArea> countries = null;
    /**
     * Initial organization (required for {@link #isOrganizationChangeRequiresGroupRemovals()})
     */
    private Organization initialOrganization;

    /**
     * User access bean
     */
    @ManagedProperty(value = "#{user}")
    private UserAccessBean userBean;

    @ManagedProperty("#{sectors}")
    private Sectors sectors;

    /**
     * List of all organizations
     */
    private List<Organization> allOrganizations;

    private GeographicalArea chosenCountry;

    private String token = "";

    private Boolean apiKeyAllowed;

    private String copyInput;

    private boolean passwordExpired;

    /**
     * Creates a new instance of UserHandler
     */
    public UserHandler() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditEntryUrl(URLGeneratorBean url, Long id) {
        return url.getUser().getEdit(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCloseEntryUrl(URLGeneratorBean url) {
        return url.getUser().getShowList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewEntryUrl(URLGeneratorBean url) {
        return url.getUser().getNew();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postEntrySet() {
        final User user = this.getEntry();

        GeographicalAreaDao gDao = new GeographicalAreaDao();
        this.countries = gDao.getCountries();

        if (!this.isCreateView()) {
            // throws exception if permission not present
            SecurityUtil.assertCanWriteUser(user.getId(), this.getGenericRoleError());
        }

        if (this.userBean.hasSuperAdminPermission()) {
            OrganizationDao oDao = new OrganizationDao();
            this.allOrganizations = oDao.getAll();
            if (!this.isCreateView()) {
                this.initialOrganization = user.getOrganization();
            }
        } else {
            final Organization organization = this.userBean.getUserObject().getOrganization();
            SecurityUtil.assertIsOrganizationAdmin(organization);
            user.setOrganization(organization);
        }

        // load the chosen country
        if (user != null && user.getAddress() != null && !StringUtils.isBlank(user.getAddress().getCountry())) {
            GeographicalAreaDao dao = new GeographicalAreaDao();
            this.chosenCountry = dao.getArea(user.getAddress().getCountry());
        }
        this.apiKeyAllowed = user.isApiKeyAllowed();

    }

    public boolean isLoggedIn() {
        return this.userBean.isLoggedIn();
    }

    public User getUser() {
        return this.getEntry();
    }

    public void setUser(User user) {
        this.setEntry(user);
    }

    public String getVerifyPassword() {
        return this.verifyPassword;
    }

    public void setVerifyPassword(String verifyPassword) {
        this.verifyPassword = verifyPassword;
    }

    public List<GeographicalArea> getCountries() {
        return this.countries;
    }

    public String getPlainPassword() {
        return this.plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    /**
     * Get a list of all organizations
     *
     * @return list of all organizations
     */
    public List<Organization> getAllOrganizations() {
        return this.allOrganizations;
    }

    public List<String> getAllSectors() {
        return sectors.getSectors();
    }

    /**
     * Get the user access bean
     *
     * @return user access bean
     */
    public UserAccessBean getUserBean() {
        return this.userBean;
    }

    /**
     * Method for the injection of user access bean
     *
     * @param userBean user access bean to set
     */
    public void setUserBean(UserAccessBean userBean) {
        this.userBean = userBean;
    }

    /**
     * Create new user
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public boolean createUser() {
        final User user = this.getEntry();
        SecurityUtil.assertIsOrganizationAdmin(user.getOrganization());

        if (user.getUserName() == null) {
            this.addI18NFacesMessage("facesMsg.user.uniqueUserName", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (this.plainPassword == null || this.plainPassword.trim().isEmpty()) {
            this.addI18NFacesMessage("facesMsg.user.pwNeeded", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        if (this.verifyPassword == null || !this.verifyPassword.equals(this.plainPassword)) {
            this.addI18NFacesMessage("facesMsg.user.pwNotSame", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        User existingUser = this.dao.getUser(user.getUserName());
        if (existingUser != null) {
            this.addI18NFacesMessage("facesMsg.user.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        // let's generate a new salt
        user.setPasswordHashSalt(this.generateSalt());
        // let's encrypt the password
        user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(this.plainPassword, user.getPasswordHashSalt()));

        //api key allowed
        user.setApiKeyAllowed(apiKeyAllowed);

        // set password as expired so the user has to change it after logging in for the first time
        user.setPasswordExpired(true);

        if (!apiKeyAllowed) {
            user.setApiKey(null);
            user.setApiKeyExpiry(null);
        }


        try {
            this.dao.persist(user);
            this.addI18NFacesMessage("facesMsg.user.createAccountSuccess", FacesMessage.SEVERITY_INFO);
            return true;
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.user.createAccountError", FacesMessage.SEVERITY_ERROR);
            return false;
        }

    }

    /**
     * Change user entry
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public boolean changeUser() {
        final User user = this.getEntry();

        UserAccessBean userAccessBean = new UserAccessBean();
        if (userAccessBean == null || !userAccessBean.isLoggedIn()) {
            String msg1 = this.getI18n().getString("facesMsg.user.notLoggedIn");
            String msg2 = this.getI18n().getString("facesMsg.user.noUserManagementRights");
            String completeMsg = msg1.concat(" ").concat(msg2);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, completeMsg, null));
            return false;
        }
        SecurityUtil.assertIsOrganizationAdmin(user.getOrganization());

        List<UserGroup> grpToMerge = new ArrayList<UserGroup>();
        if (this.isOrganizationChangeRequiresGroupRemovals()) {
            for (UserGroup grp : this.initialOrganization.getGroups()) {
                if (grp.containsUser(user)) {
                    grp.removeUser(user);
                    grpToMerge.add(grp);
                }
            }
        }

        if (user.getUserName() == null) {
            this.addI18NFacesMessage("facesMsg.user.uniqueUserName", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        if (this.plainPassword == null || this.plainPassword.trim().isEmpty()) {
            // OK, we don't change the password
        }
        // check if verify entry and plain text password are the same
        else if (this.verifyPassword == null || !this.verifyPassword.equals(this.plainPassword)) {
            this.addI18NFacesMessage("facesMsg.user.pwNotSame", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        // OK, let's change the PW
        else {
            // new salt
            user.setPasswordHashSalt(this.generateSalt());
            // encrpyt new pw
            user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(this.plainPassword, user.getPasswordHashSalt()));
        }

        //set if api key is allowed
        user.setApiKeyAllowed(apiKeyAllowed);

        if (!apiKeyAllowed) {
            user.setApiKey(null);
            user.setApiKeyExpiry(null);
        }

        final Long currentId = user.getId();
        User existingUser = this.dao.getUser(user.getUserName());
        if (existingUser != null && !existingUser.getId().equals(currentId)) {
            this.addI18NFacesMessage("facesMsg.user.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return false;
        }

        try {
            UserGroupDao grpDao = new UserGroupDao();
            this.setEntry(this.dao.merge(user));
            grpDao.merge(grpToMerge);
            this.addI18NFacesMessage("facesMsg.user.accountChanged", FacesMessage.SEVERITY_INFO);
            IlcdAuthorizationInfo.permissionsChanged();
            return true;
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
            IlcdAuthorizationInfo.permissionsChanged();
            return false;
        }

    }

    /**
     * Generate a new salt (ensures correct length)
     *
     * @return new salt
     */
    private String generateSalt() {
        return RandomPassword.getPassword(20);
    }

    /**
     * Get the chosen country
     *
     * @return chosen country
     */
    public GeographicalArea getChosenCountry() {
        return this.chosenCountry;
    }

    /**
     * Set the chosen country
     *
     * @param a country to set
     */
    public void setChosenCountry(GeographicalArea a) {
        this.chosenCountry = a;
        this.getEntry().getAddress().setCountry(a != null ? a.getAreaCode() : "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected User createEmptyEntryInstance() {
        return new User();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected User loadEntryInstance(long id) throws Exception {
        return this.dao.getById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createEntry() {
        return this.createUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeAttachedEntry() {
        return this.changeUser();
    }

    /**
     * Generates the API key. (RSA 256)
     */
    public void generateApiKey() {
        logger.debug("Trying to generate api key");
        try {
            User user = this.getEntry();
            var jwtController = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);
            token = jwtController.generateToken(user);

            if (ConfigurationService.INSTANCE.isTokenTTL()) {
                var ttl = ConfigurationService.INSTANCE.getTokenTTL() * 3600 * 1000;
                user.setApiKeyExpiry(new Date(System.currentTimeMillis() + ttl)); // no need, it's in the token
            }
            if (logger.isDebugEnabled()) {
                logger.debug("apikey generated.");
                logger.debug("api key: " + token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Generates the API key from a hidden button on the UI.
     */
    @Deprecated
    public void generateApiKeyOLD() {
        logger.debug("Trying to generate api key");
        try {
            User user = this.getEntry();
            var jwtController = ApplicationContextHolder.getApplicationContext().getBean(JWTController.class);
            String random = RandomPassword.getPassword(1071);
            token = jwtController.generateTokenOLD(user.getUserName(), random);

            if (ConfigurationService.INSTANCE.isTokenTTL()) {
                var ttl = ConfigurationService.INSTANCE.getTokenTTL() * 3600 * 1000;
                user.setApiKeyExpiry(new Date(System.currentTimeMillis() + ttl));
            }
            user.setApiKey(random);
            if (logger.isDebugEnabled()) {
                logger.debug("apikey generated.");
                logger.debug("api key: " + token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getToken() {
        return token;
    }

    /**
     * Get the initial organization
     *
     * @return the initial organization
     */
    public Organization getInitialOrganization() {
        return this.initialOrganization;
    }

    /**
     * Set the initial organization
     *
     * @param initialOrganization the initial organization to set
     */
    public void setInitialOrganization(Organization initialOrganization) {
        this.initialOrganization = initialOrganization;
    }

    public boolean isApiKeyAllowed() {
        if (apiKeyAllowed != null) {
            return apiKeyAllowed;
        }
        return true;
    }

    public void setApiKeyAllowed(boolean apiKeyAllowed) {
        this.apiKeyAllowed = apiKeyAllowed;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    /**
     * Listener for successful copy to clipboard.
     *
     * @param successEvent The event of successful text copy
     */
    public void successListener(final ClipboardSuccessEvent successEvent) {
        final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                "Component id: " + successEvent.getComponent().getId() + " Action: " + successEvent.getAction()
                        + " Text: " + successEvent.getText());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * Listener for not successful copy to clipboard.
     *
     * @param successEvent The event of not successful text copy
     */
    public void errorListener(final ClipboardErrorEvent errorEvent) {
        final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Component id: " + errorEvent.getComponent().getId() + " Action: " + errorEvent.getAction());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public final String getCopyInput() {
        return copyInput;
    }

    public final void setCopyInput(final String copyInput) {
        this.copyInput = copyInput;
    }

    /**
     * Helper method to decide if hint should be displayed after changing organization (after saving the user will be
     * removed from the according groups within the {@link #getInitialOrganization() initial organization})
     *
     * @return <code>true</code> if hint should be displayed, <code>false</code> otherwise
     */
    public boolean isOrganizationChangeRequiresGroupRemovals() {
        return this.initialOrganization != null && !this.initialOrganization.equals(this.getEntry().getOrganization());
    }

    public void setSectors(Sectors sectors) {
        this.sectors = sectors;
    }

}
