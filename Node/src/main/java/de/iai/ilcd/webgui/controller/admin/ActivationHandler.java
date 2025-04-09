package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Handler for activation
 */
@ManagedBean
@RequestScoped
public class ActivationHandler extends AbstractHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 4817228315945265354L;

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(ActivationHandler.class);
    /**
     * DAO for the access of users
     */
    private final UserDao userDao = new UserDao();
    /**
     * Current user name
     */
    private String userName = null;
    /**
     * Current activation key
     */
    private String activationKey = null;
    /**
     * Current user object
     */
    private User user = null;

    /**
     * Create the handler
     */
    public ActivationHandler() {
    }

    @PostConstruct
    public void init() {

        // check if the page was called with a request parameter specifying the
        // current user and the activation key
        try {
            this.userName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("user");
            this.activationKey = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                    .get("activationKey");
            if (StringUtils.isNotBlank(this.userName))
                this.userName = URLDecoder.decode(this.userName, "UTF-8");
            if (StringUtils.isNotBlank(this.activationKey))
                this.activationKey = URLDecoder.decode(this.activationKey, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            logger.error("decoding error", e1);
            this.addI18NFacesMessage("facesMsg.activation.stateError", FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (this.userName == null) {
            this.addI18NFacesMessage("facesMsg.activation.invalidUser", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (this.activationKey == null) {
            this.addI18NFacesMessage("facesMsg.activation.invalidKey1", FacesMessage.SEVERITY_ERROR);
            return;
        }
        this.user = this.userDao.getUser(this.userName);
        if (this.user == null) {
            this.addI18NFacesMessage("facesMsg.activation.noSuchUser", FacesMessage.SEVERITY_ERROR, this.userName);
            return;
        }
        if (this.user.getRegistrationKey() == null) {
            this.addI18NFacesMessage("facesMsg.activation.alreadyActivated", FacesMessage.SEVERITY_ERROR,
                    this.userName);
            return;
        }
        if (!this.user.getRegistrationKey().equals(this.activationKey)) {
            this.addI18NFacesMessage("facesMsg.activation.invalidKey2", FacesMessage.SEVERITY_ERROR);
            return;
        }
        // clear registration key to activate user
        this.user.setRegistrationKey(null);
        try {
            this.user = this.userDao.merge(this.user);
        } catch (MergeException ex) {
            this.addI18NFacesMessage("facesMsg.activation.stateError", FacesMessage.SEVERITY_ERROR);
            return;
        }

        Configuration properties = ConfigurationService.INSTANCE.getProperties();
        boolean selfActivation = properties.getBoolean("user.registration.selfActivation", Boolean.FALSE);

        if (selfActivation) {
            FacesUtils.redirectToPage("login.xhtml?" + LoginHandler.ACCOUNT_ACTIVATED_PARAM_KEY + "=true");
        } else {
            Message msg = null;
            RegistrationEmailGenerator emailGenerator = new RegistrationEmailGenerator();
            try {
                msg = emailGenerator.genActivationConfirmationForUser(this.user);
            } catch (MessagingException ex) {
                // OK, cannot generate email, let's show the error message to
                // the user
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
                logger.error(ex.getMessage(), ex);
            }

            try {
                if (msg != null) {
                    Transport.send(msg);
                }
            } catch (Exception ex) {
                logger.error("There was an error while sending the confirmation email", ex);
                this.addI18NFacesMessage("facesMsg.user.registrationError", FacesMessage.SEVERITY_ERROR);
            }

            this.addI18NFacesMessage("facesMsg.activation.accountActivated", FacesMessage.SEVERITY_INFO);
        }

    }

    /**
     * Get the current activation key
     *
     * @return current activation key
     */
    public String getActivationKey() {
        return this.activationKey;
    }

    /**
     * Get the user name of the current user
     *
     * @return user name of the current user
     */
    public String getUserName() {
        return this.userName;
    }
}
