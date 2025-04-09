package de.iai.ilcd.webgui.controller;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.service.exception.UserNotExistingException;
import de.iai.ilcd.service.queue.TaskingHub;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Base implementation of a handler with support for internationalized Faces messages.
 *
 * @see #addI18NFacesMessage(String, Severity)
 * @see #addI18NFacesMessage(String, Exception)
 * @see #addI18NFacesMessage(String, String, Severity, Exception, Object...)
 */
public abstract class AbstractHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 2409478268598177954L;

    /**
     * Message bundle, set via dependency injection
     */
    @ManagedProperty(value = "#{i18n}")
    private ResourceBundle i18n;

    /**
     * Get the bundle (used via dependency injection)
     *
     * @return the bundle
     */
    public ResourceBundle getI18n() {
        return this.i18n;
    }

    /**
     * Set the bundle (used via dependency injection)
     *
     * @param i18n the bundle to use
     */
    public void setI18n(ResourceBundle i18n) {
        this.i18n = i18n;
    }

    /**
     * Add a message to Faces context context with clientId and Exception details <code>null</code>
     *
     * @param i18nkey  the key to get message in language bundle from (injected via {@link #setI18n(ResourceBundle)})
     * @param severity severity of the message
     */
    public void addI18NFacesMessage(String i18nkey, Severity severity) {
        this.addI18NFacesMessage(null, i18nkey, severity, null);
    }

    /**
     * Add a message to Faces context with clientId <code>null</code> and details of the Exception
     *
     * @param i18nkey          the key to get message in language bundle from (injected via {@link #setI18n(ResourceBundle)})
     * @param exceptionDetails the details of the Exception, may be <code>null</code> and <i>will not</i> be loaded from language
     *                         bundle
     */
    public void addI18NFacesMessage(String i18nkey, Exception exceptionDetails) {
        this.addI18NFacesMessage(null, i18nkey, FacesMessage.SEVERITY_ERROR, exceptionDetails);
    }

    /**
     * Add a message to Faces context with clientId and Exception details <code>null</code>
     *
     * @param i18nkey   the key to get message in language bundle from (injected via {@link #setI18n(ResourceBundle)})
     * @param severity  severity of the message
     * @param msgParams message param(s) (not loaded from language bundle)
     */
    public void addI18NFacesMessage(String i18nkey, Severity severity, Object... msgParams) {
        this.addI18NFacesMessage(null, i18nkey, severity, null, msgParams);
    }

    /**
     * Add a message to Faces context
     *
     * @param clientId         the client id, may be <code>null</code>
     * @param i18nkey          the key to get message in language bundle from (injected via {@link #setI18n(ResourceBundle)})
     * @param severity         severity of the message
     * @param exceptionDetails the details of the Exception, may be <code>null</code> and <i>will not</i> be loaded from language
     *                         bundle
     * @param msgParams        message param(s) (not loaded from language bundle)
     */
    public void addI18NFacesMessage(String clientId, String i18nkey, Severity severity, Exception exceptionDetails, Object... msgParams) {
        FacesMessage message = buildI18NFacesMessage(i18nkey, severity, exceptionDetails, msgParams);
        FacesContext.getCurrentInstance().addMessage(clientId, message);
    }

    protected FacesMessage buildI18NFacesMessage(String i18nkey, Severity severity, Exception exceptionDetails, Object... msgParams) {
        FacesMessage message = new FacesMessage();
        message.setSeverity(severity);
        String completeMsg = this.i18n.getString(i18nkey);
        if (msgParams != null) {
            completeMsg = MessageFormat.format(completeMsg, msgParams);
        }
        message.setSummary(completeMsg);
        if (exceptionDetails != null) {
            message.setDetail(exceptionDetails.getMessage());
        }

        return message;
    }

    /**
     * Get the generic role error string from message bundle
     *
     * @return generic role error string from message bundle
     */
    protected String getGenericRoleError() {
        return this.getI18n().getString("public.error.role.msg");
    }

    /**
     * For URL encoding of file names and the like
     *
     * @param value the string to encode
     * @return the encoded string
     * @throws UnsupportedEncodingException
     */
    public String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    /**
     * Gets the current {@link User}.
     *
     * @return The current User.
     * @throws UserNotExistingException
     */
    public User getCurrentUser() throws UserNotExistingException {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser != null) {
            String userName = SecurityUtil.getPrincipalName();
            if (userName != null) {
                UserDao uDao = new UserDao();
                User user = uDao.getUser(userName);
                if (user != null) {
                    return user;
                }
            }
        }
        throw new UserNotExistingException();
    }

    protected User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (UserNotExistingException unee) {
            return null;
        }
    }

    protected TaskingHub getGlobalQueue() {
        TaskingHub hub;
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        hub = (TaskingHub) ctx.getBean("globalQueueHub");
        return hub;
    }

}
