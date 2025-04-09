package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.security.sql.IlcdSecurityRealm;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.admin.RandomPassword;
import de.iai.ilcd.webgui.controller.admin.RegistrationEmailGenerator;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * The handler for reset password view.
 *
 * @author sarai
 */
@ManagedBean
@ViewScoped
public class ResetPasswordHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = 1019943510884234413L;

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(ResetPasswordHandler.class);

    private String userName;

    private User user;

    private UserDao userDao = new UserDao();

    private boolean reset = false;

    /**
     * Initializes view and checks clicked link.
     */
    @PostConstruct
    public void init() {
        String resetKey;
        // check if the page was called with a request parameter specifying the
        // current user and the activation key
        this.userName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("user");
        resetKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get("resetKey");

        if (StringUtils.isNotBlank(this.userName))
            this.userName = URLDecoder.decode(this.userName, StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(resetKey))
            resetKey = URLDecoder.decode(resetKey, StandardCharsets.UTF_8);

        if (this.userName == null) {
            this.addI18NFacesMessage("facesMsg.activation.invalidUser", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (resetKey == null) {
            this.addI18NFacesMessage("facesMsg.reset.invalidKey", FacesMessage.SEVERITY_ERROR);
            return;
        }
        this.user = this.userDao.getUser(this.userName);
        if (this.user == null) {
            this.addI18NFacesMessage("facesMsg.activation.noSuchUser", FacesMessage.SEVERITY_ERROR, this.userName);
            return;
        }
        if (this.user.getResetKey() == null) {
            this.addI18NFacesMessage("facesMsg.resetPass.alreadyReset", FacesMessage.SEVERITY_ERROR,
                    this.userName);
            return;
        }
        if (!this.user.getResetKey().equals(resetKey)) {
            this.addI18NFacesMessage("facesMsg.resetPass.invalidKey", FacesMessage.SEVERITY_ERROR);
            return;
        }

        Date date = new Date();
        Date resetDate = this.user.getResetTimestamp();
        long acceptedTimeDifference = 24 * 60 * 60 * 1000;
        if (date.getTime() - resetDate.getTime() > acceptedTimeDifference) {
            this.addI18NFacesMessage("facesMsg.resetPass.linkExpired", FacesMessage.SEVERITY_ERROR);
            return;
        }
        this.reset = true;
    }

    /**
     * Resets password.
     */
    public void resetPassword() {
        if (this.user != null) {

            // if the user account has not been activated yet but self activation is enabled, we'll gracefully implicitly activate it
            if (!this.user.isActivated()
                    && ConfigurationService.INSTANCE.getProperties().getBoolean("user.registration.selfActivation", Boolean.FALSE)) {
                user.setRegistrationKey(null);
            }

            String newPassword = RandomPassword.getPassword(10);
            // new salt
            user.setPasswordHashSalt(this.generateSalt());
            // encrypt new pw
            user.setPasswordHash(IlcdSecurityRealm.getEncryptedPassword(newPassword, user.getPasswordHashSalt()));

            try {
                // generate Email message
                Message userMessage;
                RegistrationEmailGenerator emailGenerator = new RegistrationEmailGenerator();
                user = this.userDao.merge(user);
                userMessage = emailGenerator.genUserNoticeCredentials(user, newPassword, false);
                Transport.send(userMessage);
                this.user.setResetKey(null);
                this.user.setPasswordExpired(true);
                try {
                    this.user = this.userDao.merge(user);
                } catch (MergeException ex) {
                    this.addI18NFacesMessage("facesMsg.activation.stateError", FacesMessage.SEVERITY_ERROR);
                    return;
                }
                this.addI18NFacesMessage("facesMsg.user.retrieveSuccess", FacesMessage.SEVERITY_INFO);
            } catch (Exception e) {
                logger.error("There was an error while saving new user password " + user, e);
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public User getUser() {
        return this.user;
    }

    public boolean isReset() {
        return reset;
    }

    /**
     * Generate a new salt (ensures correct length)
     *
     * @return new salt
     */
    protected String generateSalt() {
        return RandomPassword.getPassword(20);
    }

}
