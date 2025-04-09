package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.util.VelocityUtil;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import eu.europa.ec.jrc.lca.commons.mail.MailAuthenticator;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;

import java.util.Properties;

public class RegistrationEmailGenerator {

    public final static String FALLBACK_SITENAME = "KIT SODA4LCA test site";
    private final Logger logger = LogManager.getLogger(RegistrationEmailGenerator.class);
    private final Configuration properties;

    private final ConfigurationBean confBean = new ConfigurationBean();

    private final String siteName;

    public RegistrationEmailGenerator() {
        this.properties = ConfigurationService.INSTANCE.getProperties();
        this.siteName = this.properties.getString("mail.sitename", RegistrationEmailGenerator.FALLBACK_SITENAME);
    }

    /**
     * Generate an activation information mail for the provider of the website.
     * Mail will be sent to mail address configured in properties file
     * (key: <code>user.registration.registrationAddress</code>)
     *
     * @param user user object to generate activation mail for
     * @return the generated message
     * @throws MessagingException if an error occurs
     */
    public Message genActivationMailForProvider(IUser user) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        String providerRegistrationAddress = this.properties.getString("user.registration.registrationAddress");
        // can only be called if address is set right
        if (providerRegistrationAddress == null || providerRegistrationAddress.isEmpty()) {
            // throw new MessagingException( "Address for registration confirmation of provider is not set" );
            // this setting is optional, do not throw an exception, just do nothing
            logger.warn("Address for registration confirmation by node operator is not set");
            return null;
        }
        InternetAddress to;
        try {
            to = new InternetAddress(providerRegistrationAddress);
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Email address " + providerRegistrationAddress + " for registration confirmation seems to be wrong", ex);
        }

        // set subject of message
        message.setSubject("A new user " + user.getUserName() + " has registered at your " + siteName + ". Please check the email body for activation details");
        // set content of message
        VelocityContext context = this.getContext(user, siteName);
        setContentForActivationEmail(message, context, "/providerActivationMail.vm");

        return message;
    }

    /**
     * Generate an activation information mail for the provider of the website.
     * Mail will be sent to mail address configured in properties file
     * (key: <code>user.registration.registrationAddress</code>)
     *
     * @param user user object to generate activation mail for
     * @return the generated message
     * @throws MessagingException if an error occurs
     */
    public Message genNotificationMailForProvider(IUser user) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = this.getInitializedMessage();

        String providerRegistrationAddress = this.properties.getString("user.registration.registrationAddress");
        // can only be called if address is set right
        if (providerRegistrationAddress == null || providerRegistrationAddress.isEmpty()) {
            // throw new MessagingException( "Address for registration confirmation of provider is not set" );
            // this setting is optional, do not throw an exception, just do nothing
            logger.warn("Address for registration confirmation by node operator is not set");
            return null;
        }
        InternetAddress to;
        try {
            to = new InternetAddress(providerRegistrationAddress);
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Email address " + providerRegistrationAddress + " for registration confirmation seems to be wrong", ex);
        }

        // set subject of message
        message.setSubject("A new user " + user.getUserName() + " has registered at " + siteName + ".");
        // set content of message
        VelocityContext context = this.getContext(user, siteName);
        setContentForActivationEmail(message, context, "/providerNotificationMail.vm");

        return message;
    }

    /**
     * Generate an activation information mail for the user (self-activation).
     *
     * @param user          user object to generate activation mail for
     * @param plainPassword the plain text password that is only stored in an encrypted manner in the user object
     * @return the generated message
     * @throws MessagingException if an error occurs
     */
    public Message genActivationEmailForUser(IUser user, String plainPassword) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        // set to address
        InternetAddress to;
        try {
            to = new InternetAddress(user.getEmail());
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Error sending email to address " + user.getEmail() + ". Please check your email address.", ex);
        }

        // set subject of message
        message.setSubject("User registration at " + siteName);
        // set content of message
        VelocityContext context = this.getContext(user, siteName);
        context.put("plainPassword", plainPassword);
        String changePwUrl = this.confBean.getBaseUri().toString() + "/login.xhtml?src=%2FchangePassword.xhtml";
        String profileUrl = this.confBean.getBaseUri().toString() + "/login.xhtml?src=%2Fprofile.xhtml";
        context.put("changePwUrl", changePwUrl);
        context.put("profileUrl", profileUrl);

        setContentForActivationEmail(message, context, "/activationMail.vm");

        return message;
    }

    /**
     * Generate an activation information mail for the user (manual activation).
     *
     * @param user          user object to generate activation mail for
     * @return the generated message
     * @throws MessagingException if an error occurs
     */
    public Message genActivationConfirmationForUser(User user) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        // set to address
        InternetAddress to;
        try {
            to = new InternetAddress(user.getEmail());
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Error sending email to address " + user.getEmail() + ". Please check your email address.", ex);
        }

        // set subject of message
        message.setSubject("Your " + siteName + " account has been activated.");
        // set content of message
        VelocityContext context = this.getContext(user, siteName);
        setContentForActivationEmail(message, context, "/providerActivationConfirmationMail.vm");

        return message;
    }

    /**
     * Generate user information mail for created account
     *
     * @param user          user object to generate message for
     * @param plainPassword the plain text password that is only stored in an encrypted manner in the user object
     * @return the generated message
     * @throws MessagingException if an error occurs
     */
    public Message genUserNotice(IUser user, String plainPassword) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        // set to address
        InternetAddress to;
        try {
            to = new InternetAddress(user.getEmail());
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Error sending email to address " + user.getEmail() + ". Please check your email address.", ex);
        }

        // set subject of message
        message.setSubject("Your user registration at " + this.siteName);

        // set content of message
        String providerRegistrationAddress = properties.getString("user.registration.registrationAddress");

        VelocityContext context = this.getContext(user, this.siteName);
        context.put("plainPassword", plainPassword);
        context.put("registrationAddress", providerRegistrationAddress);

        this.setContentForActivationEmail(message, context, "/registrationMail.vm");

        return message;
    }

    /**
     * Generate user information mail for retrieve mail or login credential
     *
     * @param user  user object to generate message for
     * @param newPlainPassword  the new plain text password that is only stored in an encrypted manner in the user object
     * @param login indicates if the login will be retrieved or only the mail
     * @return the generated message
     * @throws MessagingException if a message cannot be built and sent
     */
    public Message genUserNoticeCredentials(User user, String newPlainPassword, boolean login) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        // set to address
        InternetAddress to;
        try {
            to = new InternetAddress(user.getEmail());
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Error sending email to address " + user.getEmail() + ". Please check your email address.", ex);
        }

        // set content of message
        String providerRegistrationAddress = properties.getString("user.registration.registrationAddress");

        VelocityContext context = this.getContext(user, this.siteName);
        context.put("registrationAddress", providerRegistrationAddress);
        if (login) {
            message.setSubject("Your login recovery request");
            this.setContentForActivationEmail(message, context, "/loginRecoveryMail.vm");
        } else {
            context.put("newPlainPassword", newPlainPassword);
            String loginUrl = this.confBean.getBaseUri().toString() + "/login.xhtml";
            String changePwUrl = this.confBean.getBaseUri().toString() + "/login.xhtml?src=%2FchangePassword.xhtml";
            String profileUrl = this.confBean.getBaseUri().toString() + "/login.xhtml?src=%2Fprofile.xhtml";
            context.put("loginUrl", loginUrl);
            context.put("changePwUrl", changePwUrl);
            context.put("profileUrl", profileUrl);
            message.setSubject("Your new password");
            this.setContentForActivationEmail(message, context, "/newPasswordMail.vm");
        }

        return message;
    }

    /**
     * Generates  a message containing information for user password reset.
     *
     * @param user The user who wants to reset password
     * @return A message containing information for password reset
     * @throws MessagingException  if a message cannot be built and sent
     */
    public Message genResetNotice(User user) throws MessagingException {
        // initialize message, should set from address and mailhost information
        Message message = getInitializedMessage();

        // set to address
        InternetAddress to;
        try {
            to = new InternetAddress(user.getEmail());
            message.setRecipient(Message.RecipientType.TO, to);
        } catch (MessagingException ex) {
            throw new MessagingException("Error sending email to address " + user.getEmail() + ". Please check your email address.", ex);
        }
        message.setSubject("Your request to reset your password");
        VelocityContext context = this.getContext(user, this.siteName);
        this.setContentForResetEmail(message, context, "/resetPasswordMail.vm");
        return message;
    }

    /**
     * Create
     *
     * @return the message
     * @throws MessagingException  if a message cannot be built and sent
     */
    private Message getInitializedMessage() throws MessagingException {
        String sender = this.properties.getString("mail.sender", null);
        if (sender == null) {
            throw new MessagingException("Please set a sender email address in configuration file (key: mail.sender).");
        }

        String mailhost = this.properties.getString("mail.hostname", null);
        if (mailhost == null) {
            throw new MessagingException("Please set a smtp host name in configuration file (key: mail.hostname).");
        }

        String port = this.properties.getString("mail.port", "25");

        String starttls = this.properties.getString("mail.starttls", "false");

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", mailhost);
        mailProps.put("mail.smtp.port", port);
        mailProps.put("mail.smtp.starttls.enable", starttls);

        Session session;
        Message message;
        String auth = this.properties.getString("mail.auth", null);
        if (auth != null && auth.contains("true")) {
            mailProps.setProperty("mail.smtp.auth", "true");

            String user = this.properties.getString("mail.user", null);
            if (user == null) {
                throw new MessagingException("Please set a valid User value in configuration file (key: mail.user).");
            }

            String pwd = this.properties.getString("mail.password", null);
            if (pwd == null) {
                throw new MessagingException("Please set a valid Password value for the User in configuration file (key: mail.password).");
            }

            MailAuthenticator authenticator = new MailAuthenticator(user, pwd);
            mailProps.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
            session = Session.getDefaultInstance(mailProps, authenticator);
        } else {
            session = Session.getDefaultInstance(mailProps);
        }
        message = new MimeMessage(session);

        InternetAddress from;
        try {
            from = new InternetAddress(sender);
            message.setFrom(from);
        } catch (MessagingException ex) {
            throw new MessagingException(
                    "Cannot initialize registration email message with sender and host information; Please contact the adminstration of the website", ex);
        }

        return message;
    }

    private VelocityContext getContext(IUser user, String siteName) {
        // we generate the message with a velocity template
        VelocityContext context = VelocityUtil.getContext();
        context.put("user", user);

        context.put("siteName", siteName);

        return context;
    }

    private void setContentForActivationEmail(Message message, VelocityContext context, String templateName) throws MessagingException {

        String activationUrl = this.confBean.getBaseUri().toString() + "/activation.xhtml";
        context.put("activationUrl", activationUrl);
        String text = VelocityUtil.parseTemplate(templateName, context);
        // now let's set the body of the message
        try {
            message.setContent(text, "text/html; charset=UTF-8");
        } catch (MessagingException ex) {
            throw new MessagingException("Cannot set content of activation email", ex);
        }
    }

    /**
     * Sets mail content for reset password e-mail.
     *
     * @param message      The message the e-mail shall contain
     * @param context      the e-mail context
     * @param templateName The name of e-mail template
     * @throws MessagingException  if a message cannot be built and sent
     */
    private void setContentForResetEmail(Message message, VelocityContext context, String templateName) throws MessagingException {
        String resetUrl = this.confBean.getBaseUri().toString() + "/resetPassword.xhtml";
        context.put("resetUrl", resetUrl);
        String text = VelocityUtil.parseTemplate(templateName, context);
        message.setContent(text, "text/html; charset=UTF-8");
    }
}