package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.exception.ExpiredPasswordNotChangedException;
import de.iai.ilcd.model.common.exception.PrivacyPolicyNotAcceptedException;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.security.UserOIDC;
import de.iai.ilcd.security.ProtectableType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.UserLoginActions;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import de.iai.ilcd.service.UserService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

/**
 * Handler for login and logout actions
 */
@ManagedBean
@SessionScoped
public class LoginHandler extends AbstractHandler {

    /**
     * If a 'login required' FacesMessage is required, one can simply use
     * the request param 'loginRequired=area'
     */
    public static final String LOGIN_REQUIRED_PARAM_KEY = "loginRequired";
    /**
     * If a 'login required' FacesMessage is required, one can simply use
     * the request param 'loginRequired=area'
     */
    public static final String LOGIN_REQUIRED_AREA = "area";
    /**
     * If a 'login required' FacesMessage is required, one can simply use
     * the request param 'loginRequired=area'
     */
    public static final String LOGIN_REQUIRED_RESOURCE = "resource";
    /**
     * If an 'account activated' FacesMessage is required, one can simply use
     * the request param 'accountActivated=true'
     */
    public static final String ACCOUNT_ACTIVATED_PARAM_KEY = "accountActivated";
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -1378066230750034948L;
    /**
     * Logger instance
     */
    private static final Logger logger = LogManager.getLogger(LoginHandler.class);
    /**
     * User service for managing user
     */
    private final UserService userService;
    /**
     * DAO for user objects
     */
    private final UserDao userDao = new UserDao();
    /**
     * Current user logged in flag
     */
    boolean loggedIn = false;
    /**
     * Source view to redirect
     */
    private String source;
    /**
     * Configuration (injected)
     */
    @ManagedProperty(value = "#{conf}")
    private ConfigurationBean conf;
    /**
     * Current input for login name
     */
    private String loginName;
    /**
     * Current input for password
     */
    private String password;
    /**
     * The plain password
     */
    private String plainPassword;
    /**
     * The repeated password (verification)
     */
    private String verifyPassword;
    /**
     * Current remembered flag
     */
    private boolean remembered = false;
    /**
     * Bound to checkbox in UI
     */
    private boolean privacyPolicyAccepted = false;
    /**
     * Handler with available stocks
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocks;

    /**
     * Creates a new instance of LoginHandler
     */
    public LoginHandler() {
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.userService = ctx.getBean(UserService.class);
    }

    /**
     * Get the current login name
     *
     * @return current login name
     */
    public String getLoginName() {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated() || currentUser.isRemembered())
            return SecurityUtil.getPrincipalName();
        else
            return this.loginName;
    }

    /**
     * Set the login name from user input
     *
     * @param loginName current input value
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * Get the current password
     *
     * @return current password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password from user input
     *
     * @param password current input value
     */
    public void setPassword(String password) {
        this.password = password;
    }


    /**
     * Get the plain password
     *
     * @return plain password
     */
    public String getPlainPassword() {
        return plainPassword;
    }

    /**
     * Set the plain password
     *
     * @param plainPassword password
     */
    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    /**
     * Get the repeated password
     *
     * @return repeated password
     */
    public String getVerifyPassword() {
        return verifyPassword;
    }

    /**
     * Set the repeated password
     *
     * @para verifyPassword repeated password
     */
    public void setVerifyPassword(String verifyPassword) {
        this.verifyPassword = verifyPassword;
    }

    /**
     * Get the source view to redirect to
     *
     * @return path of the source view to redirect to
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set the source view to redirect to
     *
     * @param source path of the source view to redirect to
     */
    public void setSource(String source) {
        try {
            this.source = URLDecoder.decode(source, "UTF-8");
        } catch (Exception e) {
            this.source = source;
        }
    }

    public boolean isRemembered() {
        return this.remembered;
    }

    public void setRemembered(boolean remembered) {
        this.remembered = remembered;
    }

    /**
     * Set the configuration bean (via dependency injection)
     *
     * @param conf configuration to set
     */
    public void setConf(ConfigurationBean conf) {
        this.conf = conf;
    }

    /**
     * Set / inject available stocks handler
     *
     * @param availableStocks handler to set / inject
     */
    public void setAvailableStocks(AvailableStockHandler availableStocks) {
        this.availableStocks = availableStocks;
    }

    /**
     * Do login by user provided credentials
     */
    public void login() {
        logger.debug("login");

        // we use the Shiro framework here
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated() || currentUser.isRemembered()) {
            this.addI18NFacesMessage("facesMsg.login.alreadyLoggedIn", FacesMessage.SEVERITY_ERROR);
            if (logger.isDebugEnabled()) {
                logger.debug("already logged in, authenticated: " + currentUser.isAuthenticated() + ", remembered: " + currentUser.isRemembered());
            }
            return;
        }

        // no user currently logged in
        if (this.loginName == null || this.password == null) {
            logger.debug("invalid credentials - missing username or password");
            this.addI18NFacesMessage("facesMsg.login.invalidCredentials", FacesMessage.SEVERITY_ERROR);
            return;
        }

        String salt = this.userDao.getSalt(this.loginName);
        if (salt == null) {
            logger.debug("invalid credentials - missing salt");
            this.addI18NFacesMessage("facesMsg.login.incorrectCredentials", FacesMessage.SEVERITY_ERROR);
            return;
        }

        try {
            User user = userDao.getUser(loginName);

            HttpServletResponse res = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            if (conf.isAcceptPrivacyPolicy() && !user.isPrivacyPolicyAccepted()) {
                logger.debug("user has yet to accept privacy policy");
                // if the pp hasn't been accepted yet, we'll only validate the credentials and forward to the "accept pp" page, but not yet log them in
                if (!user.getPasswordHash().equals(IlcdSecurityRealm.getEncryptedPassword(this.password, salt))) {
                    logger.debug("invalid credentials");
                    throw new IncorrectCredentialsException();
                }
                if (logger.isDebugEnabled())
                    logger.debug("redirecting to " + this.conf.getContextPath() + "/acceptPrivacyPolicy.xhtml");

                res.sendRedirect(this.conf.getContextPath() + "/acceptPrivacyPolicy.xhtml");
            } else if (user.isPasswordExpired() != null && user.isPasswordExpired()) {
                logger.debug("user password expired");
                // if password is expired validate credentials and forward to change password
                if (!user.getPasswordHash().equals(IlcdSecurityRealm.getEncryptedPassword(this.password, salt))) {
                    logger.debug("invalid credentials");
                    throw new IncorrectCredentialsException();
                }
                if (logger.isDebugEnabled())
                    logger.debug("redirecting to " + this.conf.getContextPath() + "/passwordExpired.xhtml");

                res.sendRedirect(this.conf.getContextPath() + "/passwordExpired.xhtml");
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("trying to login user " + this.loginName);
                new UserLoginActions().login(this.loginName, IlcdSecurityRealm.getEncryptedPassword(this.password, salt), remembered);
                // if the user logs in for the first time, the user will be logged in but expire the password for the next time
                if (user.isPasswordExpired() == null) {
                    user.setPasswordExpired(true);
                    userDao.merge(user);
                }

                updateSessionAndRedirect();
            }
        } catch (UnknownAccountException uae) {
            logger.debug("unknown account", uae);
            this.addI18NFacesMessage("facesMsg.login.incorrectCredentials", FacesMessage.SEVERITY_ERROR);
        } catch (IncorrectCredentialsException ice) {
            logger.debug("incorrect credentials", ice);
            this.addI18NFacesMessage("facesMsg.login.incorrectCredentials", FacesMessage.SEVERITY_ERROR);
        } catch (LockedAccountException lae) {
            logger.debug("account locked", lae);
            this.addI18NFacesMessage("facesMsg.accountLocked", FacesMessage.SEVERITY_ERROR);
        } catch (AuthenticationException au) {
            logger.debug("authentication failed", au);
            LoginHandler.logger.info(au.getMessage());
            this.addI18NFacesMessage("facesMsg.login.incorrectCredentials", FacesMessage.SEVERITY_ERROR);
        } catch (Exception e) {
            logger.debug("other error", e);
            this.addI18NFacesMessage("facesMsg.error", e);
        }

    }

    /**
     * attaches changes of user when they selected button in "accept privacy policy" page.
     */
    public void acceptPrivacyPolicy() {
        User user = userDao.getUser(loginName);
        user.setPrivacyPolicyAccepted(privacyPolicyAccepted);
        String salt = userDao.getSalt(loginName);
        try {
            userService.privacyPolicyAccepted(user);
            new UserLoginActions().login(this.loginName, IlcdSecurityRealm.getEncryptedPassword(this.password, salt), remembered);
            userService.gotToRequestedPage(this.conf, this.source, "/admin/index.xhtml");
        } catch (PrivacyPolicyNotAcceptedException e) {
            this.addI18NFacesMessage("facesMsg.user.acceptPrivacyPolicy", FacesMessage.SEVERITY_ERROR);
            if (logger.isDebugEnabled())
                e.printStackTrace();
        } catch (MergeException | IOException e) {
            this.addI18NFacesMessage("facesMsg.error", FacesMessage.SEVERITY_ERROR);
            if (logger.isDebugEnabled())
                e.printStackTrace();
        }
    }

    /**
     * attaches changes of user when changing password and after successful change logs user in
     */
    public void changeExpiredPassword() {
        User user = userDao.getUser(loginName);
        if (this.verifyPassword == null || !this.verifyPassword.equals(this.plainPassword)) {
            this.addI18NFacesMessage("facesMsg.user.pwNotSame", FacesMessage.SEVERITY_ERROR);
            return;
        }
        // if the plain password and repeated password match, change the password
        else {
            // new salt
            user.setPasswordHashSalt(this.generateSalt());
            // encrypt new pw
            user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(this.verifyPassword, user.getPasswordHashSalt()));
            // set password as not expired
            user.setPasswordExpired(false);
        }
        try {
            // log in user
            userService.expiredPasswordChanged(user);
            String salt = userDao.getSalt(loginName);
            this.addI18NFacesMessage("facesMsg.user.passwordChanged", FacesMessage.SEVERITY_INFO);
            new UserLoginActions().login(this.loginName, IlcdSecurityRealm.getEncryptedPassword(this.verifyPassword, salt), remembered);

            updateSessionAndRedirect();
        } catch (ExpiredPasswordNotChangedException e) {
            this.addI18NFacesMessage("facesMsg.user.acceptPrivacyPolicy", FacesMessage.SEVERITY_ERROR);
            if (logger.isDebugEnabled())
                e.printStackTrace();
        } catch (MergeException | IOException e) {
            this.addI18NFacesMessage("facesMsg.error", FacesMessage.SEVERITY_ERROR);
            if (logger.isDebugEnabled())
                e.printStackTrace();
        }
    }

    public void showMessage(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (LOGIN_REQUIRED_AREA.equals(fc.getExternalContext().getRequestParameterMap().get(LOGIN_REQUIRED_PARAM_KEY)))
            this.addI18NFacesMessage("facesMsg.login.loginRequired.area", FacesMessage.SEVERITY_INFO);
        else if (LOGIN_REQUIRED_RESOURCE.equals(fc.getExternalContext().getRequestParameterMap().get(LOGIN_REQUIRED_PARAM_KEY)))
            this.addI18NFacesMessage("facesMsg.login.loginRequired.resource", FacesMessage.SEVERITY_INFO);
        else if ("true".equals(fc.getExternalContext().getRequestParameterMap().get(ACCOUNT_ACTIVATED_PARAM_KEY)))
            this.addI18NFacesMessage("facesMsg.activation.accountActivated", FacesMessage.SEVERITY_INFO);
    }

    /**
     * Generate a new salt (ensures correct length)
     *
     * @return new salt
     */
    protected String generateSalt() {
        return RandomPassword.getPassword(20);
    }

    public User getUser() {
        return userDao.getUser(loginName);
    }

    /**
     * Logout the current user
     */
    public void logout() {
        String tempLoginName;
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated() || currentUser.isRemembered())
            tempLoginName = SecurityUtil.getPrincipalName();
        else
            tempLoginName = this.loginName;
        new UserLoginActions().logout();
        this.availableStocks.reloadAllDataStocks();
        this.addI18NFacesMessage("facesMsg.login.logoutSuccess", FacesMessage.SEVERITY_INFO, tempLoginName);
    }

    private void updateSessionAndRedirect() throws IOException {
        Subject currentUser = SecurityUtils.getSubject();
        this.availableStocks.reloadAllDataStocks();
        // redirect to source view specified by src param (except login.xhtml) else to (admin/)index.xhtml
        final String indexXhtmlPath = (currentUser.isPermitted(ProtectableType.ADMIN_AREA.name()) ? "/admin" : "") + "/index.xhtml";
        userService.gotToRequestedPage(this.conf, this.source, indexXhtmlPath); // throws IOException
    }

    /**
     * Determine if a user is currently logged in. Delegates to
     * {@link UserLoginActions#isLoggedIn()}.
     *
     * @return <code>true</code> if user is logged in, else <code>false</code>
     */
    public boolean isLoggedIn() {
        return new UserLoginActions().isLoggedIn();
    }

    /**
     * Grab user profile (including defined attributes) from pac4j principle.
     * <p>
     * Displayed on /oidc.xhtml. mostly used for debugging.
     *
     * @return OIDC profile
     */

    public Map<String, Object> OidcAttrs() {
        // availableStocks have to be manually called
        this.availableStocks.reloadAllDataStocks();
        UserOIDC user = (UserOIDC) SecurityUtil.getCurrentUser();
        return user.getProfile().getAttributes();
    }


    public Set<String> OidcRoles() {
        this.availableStocks.reloadAllDataStocks();
        UserOIDC user = (UserOIDC) SecurityUtil.getCurrentUser();
        return user.getProfile().getRoles();
    }

    public Set<String> OidcPermissions() {
        this.availableStocks.reloadAllDataStocks();
        UserOIDC user = (UserOIDC) SecurityUtil.getCurrentUser();
        return user.getProfile().getPermissions();
    }

    public boolean isPrivacyPolicyAccepted() {
        return privacyPolicyAccepted;
    }

    public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted) {
        this.privacyPolicyAccepted = privacyPolicyAccepted;
    }
}
