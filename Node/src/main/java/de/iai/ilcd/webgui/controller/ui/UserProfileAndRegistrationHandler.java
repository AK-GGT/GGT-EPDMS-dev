package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.AdditionalTerm;
import de.iai.ilcd.model.common.GeographicalArea;
import de.iai.ilcd.model.common.exception.InvalidGroupException;
import de.iai.ilcd.model.dao.GeographicalAreaDao;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.dao.UserGroupDao;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserGroup;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.UserAccessBean;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import de.iai.ilcd.service.AdditionalTermsService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.admin.RandomPassword;
import de.iai.ilcd.webgui.controller.admin.RegistrationEmailGenerator;
import de.iai.ilcd.webgui.controller.admin.UserHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@ManagedBean(name = "uparHandler")
public class UserProfileAndRegistrationHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = -7114972353108917936L;

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(UserProfileAndRegistrationHandler.class);
    /**
     * User DAO
     */
    private final UserDao dao = new UserDao();
    /**
     * List of countries
     */
    private final List<GeographicalArea> countries;
    /**
     * User access bean
     */
    @ManagedProperty(value = "#{user}")
    private UserAccessBean userBean;
    @ManagedProperty(value = "#{userHandler}")
    private UserHandler userHandler;
    /**
     * The plain password
     */
    private String plainPassword;
    /**
     * The repeated password (verification)
     */
    private String verifyPassword;
    /**
     * The current password
     */
    private String currentPassword;
    /**
     * User instance
     */
    private IUser user = new User();
    /**
     * Value of spam slider
     */
    private int spamSliderValue = 0;

    @Autowired
    private AdditionalTermsService additionalTermsService;

    /**
     * Chosen country of address
     */
    private GeographicalArea chosenCountry;

    public UserProfileAndRegistrationHandler() {
        GeographicalAreaDao dao = new GeographicalAreaDao();
        this.countries = dao.getCountries();
    }

    public void postViewParamInitProfile() {

        if (!this.userBean.isLoggedIn()) {
            this.addI18NFacesMessage("facesMsg.user.changeProfile", FacesMessage.SEVERITY_ERROR);
            return;
        }

        IUser currentUser = SecurityUtil.getCurrentUser(); // this.dao.getUser( this.userBean.getUserName() );
        if (currentUser == null) {
//			this.addI18NFacesMessage( "facesMsg.user.noProfile", FacesMessage.SEVERITY_ERROR, currentUser.getUserName());
            this.addI18NFacesMessage("facesMsg.user.noProfile", FacesMessage.SEVERITY_ERROR, "Unknown User");
            return;
        }

        // throws exception if permission not present
        SecurityUtil.assertCanWrite(currentUser, this.getGenericRoleError());

        this.user = currentUser;
        this.userHandler.setUser((User) currentUser);

        // load the chosen country
        if (currentUser.getAddress() != null && !StringUtils.isBlank(currentUser.getAddress().getCountry())) {
            GeographicalAreaDao dao = new GeographicalAreaDao();
            this.chosenCountry = dao.getArea(currentUser.getAddress().getCountry());
        }

    }

    /**
     * Register a new user
     *
     * @return target view
     * @throws Exception
     */
    public String doRegister() throws Exception {
        Configuration conf = ConfigurationService.INSTANCE.getProperties();

        if (ConfigurationService.INSTANCE.isAcceptPrivacyPolicy() && !user.isPrivacyPolicyAccepted()) {
            this.addI18NFacesMessage("facesMsg.user.acceptPrivacyPolicy", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        if (conf.getBoolean("user.registration.spam.protection.slider", false) && this.spamSliderValue != 100) {
            this.addI18NFacesMessage("facesMsg.registration.captcha.slider.invalidValue", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        // check properties first
        if (!conf.getBoolean("user.registration.activated", Boolean.FALSE)) {
            this.addI18NFacesMessage("facesMsg.user.noRegistration", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        UserDao dao = new UserDao();

        if (this.user.getUserName() == null) {
            this.addI18NFacesMessage("facesMsg.user.uniqueUserName", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        User existingUser = dao.getUser(this.user.getUserName());
        if (existingUser != null) {
            this.addI18NFacesMessage("facesMsg.user.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        // generate password, salt and encrypted password and activation key
        String plainPassword = RandomPassword.getPassword(10);
        this.user.setPasswordHashSalt(this.generateSalt());
        this.user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(plainPassword, this.user.getPasswordHashSalt()));

        String registrationKey = RandomPassword.getPassword(20);
        this.user.setRegistrationKey(registrationKey);

        // generate Email messages
        Message userMessage = null;
        Message providerMessage = null;
        RegistrationEmailGenerator emailGenerator = new RegistrationEmailGenerator();
        boolean userSelfActivation = false;
        // get property, how user activation should be performed
        userSelfActivation = conf.getBoolean("user.registration.selfActivation", Boolean.FALSE);
        try {
            if (userSelfActivation) {
                // the user can activate his account himself
                userMessage = emailGenerator.genActivationEmailForUser(this.user, plainPassword);
                // if configured, also notify admin about user registration
                if (conf.getBoolean("user.registration.selfActivation.notify", false))
                    providerMessage = emailGenerator.genNotificationMailForProvider(this.user);
            } else {
                // user only get's a notification about the completed registration
                userMessage = emailGenerator.genUserNotice(this.user, plainPassword);
                // provider get's an email to activate the user
                providerMessage = emailGenerator.genActivationMailForProvider(this.user);
            }
        } catch (MessagingException ex) {
            // OK, cannot generate email, let's show the error message to the user
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            UserProfileAndRegistrationHandler.logger.error(ex.getMessage(), ex);
            return null;
        }

        try {
            dao.persist((User) this.user);

            addDefaultGroup(conf);
            if (user.getAcceptedAdditionalTermsMap() != null && !user.getAcceptedAdditionalTermsMap().isEmpty()) {
//				addAdditionalGroup(conf);
                List<AdditionalTerm> additionalTerms = new ArrayList<AdditionalTerm>();
                for (AdditionalTerm additionalTerm : additionalTerms) {
                    addGroup(additionalTerm.getDefaultGroup());
                }

            }

            if (userMessage != null) {
                Transport.send(userMessage);
            }
            if (providerMessage != null) {
                Transport.send(providerMessage);
            }
        } catch (InvalidGroupException ex) {
            this.addI18NFacesMessage("facesMsg.user.groupError", FacesMessage.SEVERITY_ERROR);
            dao.remove((User) this.user);
            return null;
        } catch (Exception ex) {
//			UserProfileAndRegistrationHandler.logger.error( "There was an error while saving registration information or sending registration email", ex );
//			dao.remove( this.user );
//			this.addI18NFacesMessage( "facesMsg.user.registrationError", FacesMessage.SEVERITY_ERROR );
//			return null;
        }

        return "registrated";

    }

    /**
     * Adds new user to default group.
     *
     * @param conf The configuration the default group is defined
     * @throws InvalidGroupException
     * @throws MergeException
     */
    private void addDefaultGroup(Configuration conf) throws InvalidGroupException, MergeException {
        String groupName = conf.getString("user.registration.defaultGroup", null);
        addGroup(groupName);
    }

    /**
     * Adds user to group with given name.
     *
     * @param groupName The group user shall be added to.
     * @throws InvalidGroupException
     * @throws MergeException
     */
    private void addGroup(String groupName) throws InvalidGroupException, MergeException {
        if (StringUtils.isNotEmpty(groupName)) {
            if (logger.isDebugEnabled())
                logger.debug("adding new user to default group " + groupName);
            UserGroupDao groupDao = new UserGroupDao();
            UserGroup group = groupDao.getGroup(groupName);
            if (group == null) {
                logger.error("group " + groupName + " does not exist");
                throw new InvalidGroupException("group " + groupName + " does not exist");
            }
            group.addUser((User) user);
            groupDao.merge(group);
        }
    }

    /**
     * Change the profile
     */
    public void changeProfile() {

        SecurityUtil.assertCanWrite(this.user, "You are not permitted to edit this profile.");

        UserAccessBean userAccessBean = new UserAccessBean();
        if (!userAccessBean.isLoggedIn()) {
            String msg1 = this.getI18n().getString("facesMsg.user.notLoggedIn");
            String msg2 = this.getI18n().getString("facesMsg.user.noUserManagementRights");
            String completeMsg = msg1.concat(" ").concat(msg2);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, completeMsg, null));
            return;
        }

        if (this.user.getUserName() == null) {
            this.addI18NFacesMessage("facesMsg.user.uniqueUserName", FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (this.plainPassword == null || this.plainPassword.trim().isEmpty()) {
            // OK, we don't change the password
        }
        // check if verify entry and plain text password are the same
        else if (this.verifyPassword == null || !this.verifyPassword.equals(this.plainPassword)) {
            this.addI18NFacesMessage("facesMsg.user.pwNotSame", FacesMessage.SEVERITY_ERROR);
            return;
        }
        // OK, let's change the PW
        else {
            // new salt
            this.user.setPasswordHashSalt(this.generateSalt());
            // encrpyt new pw
            this.user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(this.plainPassword, this.user.getPasswordHashSalt()));
        }

        final Long currentId = this.user.getId();
        User existingUser = this.dao.getUser(this.user.getUserName());
        if (existingUser != null && !existingUser.getId().equals(currentId)) {
            this.addI18NFacesMessage("facesMsg.user.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return;
        }

        try {
            this.user = this.dao.merge((User) this.user);
            this.addI18NFacesMessage("facesMsg.user.accountChanged", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
        }

    }

    /**
     * Change the profile
     */
    public void changePassword() {

        SecurityUtil.assertCanWrite(this.user, "You are not permitted to edit this profile.");

        UserAccessBean userAccessBean = new UserAccessBean();
        if (userAccessBean == null || !userAccessBean.isLoggedIn()) {
            String msg1 = this.getI18n().getString("facesMsg.user.notLoggedIn");
            String msg2 = this.getI18n().getString("facesMsg.user.noUserManagementRights");
            String completeMsg = msg1.concat(" ").concat(msg2);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, completeMsg, null));
            return;
        }

        User existingUser = this.dao.getUser(this.user.getUserName());

        if (existingUser.getPasswordHash().compareTo(IlcdSecurityRealm.getEncryptedPassword(this.currentPassword, this.user.getPasswordHashSalt())) != 0) {
            this.addI18NFacesMessage("facesMsg.user.incorrectPassword", FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (this.plainPassword == null || this.plainPassword.trim().isEmpty()) {
            // OK, we don't change the password
        }
        // check if verify entry and plain text password are the same
        else if (this.verifyPassword == null || !this.verifyPassword.equals(this.plainPassword)) {
            this.addI18NFacesMessage("facesMsg.user.pwNotSame", FacesMessage.SEVERITY_ERROR);
            return;
        }
        // OK, let's change the PW
        else {
            // new salt
            this.user.setPasswordHashSalt(this.generateSalt());
            // encrpyt new pw
            this.user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(this.plainPassword, this.user.getPasswordHashSalt()));
        }


        try {
            this.user = this.dao.merge((User) this.user);
            this.addI18NFacesMessage("facesMsg.user.passwordChanged", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
        }
    }

    public IUser getUser() {
        return this.user;
    }

    public String getPlainPassword() {
        return this.plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
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
        this.user.getAddress().setCountry(a != null ? a.getAreaCode() : "");
    }

    /**
     * Generate a new salt (ensures correct length)
     *
     * @return new salt
     */
    protected String generateSalt() {
        return RandomPassword.getPassword(20);
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
     * Check if create view (PW field required)
     *
     * @return <code>false</code>
     */
    public boolean isCreateView() {
        return false;
    }

    /**
     * Get the value for the spam slider
     *
     * @return value of the spam slider
     */
    public int getSpamSliderValue() {
        return this.spamSliderValue;
    }

    /**
     * Set the value for the spam slider
     *
     * @param spamSliderValue value to set
     */
    public void setSpamSliderValue(int spamSliderValue) {
        this.spamSliderValue = spamSliderValue;
    }

    public IUser getEntry() {
        return this.user;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public void setUserHandler(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    public boolean isPrivacyPolicyAccepted() {
        return user.isPrivacyPolicyAccepted();
    }

    public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted) {
        this.user.setPrivacyPolicyAccepted(privacyPolicyAccepted);
    }

    public String getToken() {
        return this.userHandler.getToken();
    }

    public void generateApiKey() {
        this.userHandler.generateApiKey();
    }

    public boolean isApiKeyAllowed() {
        return this.userHandler.isApiKeyAllowed();
    }

    public void setApiKeyAllowed(boolean value) {
        this.userHandler.setApiKeyAllowed(value);
    }

}
