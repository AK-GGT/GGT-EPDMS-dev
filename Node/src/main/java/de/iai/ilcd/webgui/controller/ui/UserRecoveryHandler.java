package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.admin.RandomPassword;
import de.iai.ilcd.webgui.controller.admin.RegistrationEmailGenerator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Date;
import java.util.List;


/**
 * The handler for login recovery view.
 * Contains functionalities for retrieving username and for requesting password reset,
 *
 * @author sarai
 */
@ViewScoped
@ManagedBean(name = "userRecoveryHandler")
public class UserRecoveryHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = -830307486811137829L;

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(UserRecoveryHandler.class);

    private final UserDao dao = new UserDao();

    private final User user = new User();

    /**
     * Retrieve the logins that belong to email insert by user. The email
     * are mandatory.
     *
     * @author Wellington Stanley - IBICT
     */
    public void loginRecovery() {

        try {
            List<User> existingUsers = this.dao.getNonPrivilegedUsersByEmail(this.user.getEmail());

            // generate Email messages
            Message userMessage = null;
            RegistrationEmailGenerator emailGenerator = new RegistrationEmailGenerator();

            if (existingUsers == null || existingUsers.isEmpty()) {
                this.addI18NFacesMessage("facesMsg.user.loginNotFound", FacesMessage.SEVERITY_ERROR);
            } else {
                for (User user : existingUsers) {
                    userMessage = emailGenerator.genUserNoticeCredentials(user, null, true);
                    if (userMessage != null) {
                        Transport.send(userMessage);
                    }
                }
                this.addI18NFacesMessage("facesMsg.user.loginSend", FacesMessage.SEVERITY_INFO);
            }

        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.user.loginRecoveryError", FacesMessage.SEVERITY_ERROR);
            logger.error("There was an error while sending email to retrieve access", e);
        }

    }

    /**
     * Requests to reset password. Therefore, the e-mail is mandatory
     * to send mail with reset information to user.
     *
     * @author Wellington Stanley - IBICT
     */
    public void requestPasswordReset() {

        List<User> existingUsers = this.dao.getNonPrivilegedUsersByEmail(this.user.getEmail());

        if (existingUsers == null || existingUsers.isEmpty()) {
            this.addI18NFacesMessage("facesMsg.user.loginNotFound", FacesMessage.SEVERITY_ERROR);
        } else {
            try {
                for (User user : existingUsers) {
                    // password reset is only possible if the user account been activated (or if self activation is enabled)
                    if (!user.isActivated() && !ConfigurationService.INSTANCE.getProperties().getBoolean("user.registration.selfActivation", Boolean.FALSE)) {
                        this.addI18NFacesMessage("facesMsg.activation.notActivated", FacesMessage.SEVERITY_ERROR, user);
                        return;
                    }

                    String resetKey = RandomPassword.getPassword(20);
                    user.setResetKey(resetKey);
                    user.setResetTimestamp(new Date());

                    dao.merge(user);

                    RegistrationEmailGenerator emailGenerator = new RegistrationEmailGenerator();
                    Message userMessage;

                    userMessage = emailGenerator.genResetNotice(user);
                    if (userMessage != null) {
                        Transport.send(userMessage);
                    }
                    this.addI18NFacesMessage("facesMsg.user.loginSend", FacesMessage.SEVERITY_INFO);

                }
            } catch (MessagingException e) {
                this.addI18NFacesMessage("facesMsg.user.accessRecoveryError", FacesMessage.SEVERITY_ERROR);
                if (logger.isDebugEnabled())
                    e.printStackTrace();
            } catch (MergeException e) {
                if (logger.isDebugEnabled())
                    e.printStackTrace();
                this.addI18NFacesMessage("facesMsg.error", FacesMessage.SEVERITY_ERROR);
            }
        }
    }

    public User getUser() {
        return this.user;
    }

}
