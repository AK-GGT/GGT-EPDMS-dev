package de.iai.ilcd.webgui.controller.ui;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;

import javax.annotation.PostConstruct;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Managed bean for language selection of the UI
 */
@SessionScoped
@ManagedBean(name = "languageSelection")
public class LanguageSelectionHandler implements Serializable {

    private static final long serialVersionUID = 6023388596899922962L;
    private static final Logger logger = LogManager.getLogger(LanguageSelectionHandler.class);
    private final List<String> supportedLanguages;
    /**
     * Query string to manipulate for other language
     */
    private String queryString;
    /**
     * Current language
     */
    private String currentLanguage;

    /**
     * Initialize language
     */
    public LanguageSelectionHandler() {
        // initialize supported languages
        supportedLanguages = Arrays.asList("en", "de", "es", "it", "nl", "da",
                "ru", "pl", // some themes don't support cyrillic characters (ex: nova-light)
                "br", "ja", "sv", "fr", "fi");

        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest servletRequest = (HttpServletRequest) ctx.getExternalContext().getRequest();
        this.queryString = servletRequest.getQueryString();
    }

    @PostConstruct
    public void init() {
        try {
            FacesContext facescontext = FacesContext.getCurrentInstance();

            // read supported languages
            Map<String, String> requestHeaders = facescontext.getExternalContext().getRequestHeaderMap();
            String languages = requestHeaders.get("Accept-Language");

            if (languages == null) return;

            for (String lang : supportedLanguages)
                if (languages.contains(lang)) {
                    this.setCurrentLanguage(lang);
                    return;
                }

            this.setCurrentLanguage("en"); // default
        } catch (Exception e) {
            logger.debug("error setting language: ", e);
            if (e instanceof AuthorizationException) {
                throw (AuthorizationException) e;
            }
        }
    }

    /**
     * Get the current language
     *
     * @return current language
     */
    public String getCurrentLanguage() {
        logger.debug("current language is: " + currentLanguage);
        return this.currentLanguage;
    }


    /**
     * Set the current language
     *
     * @param language language 2-letter code
     */
    public void setCurrentLanguage(String language) {
        this.currentLanguage = language;
    }

    /**
     * Called by selectOneMenu after setting new language
     * Redirect is done by {@link NavigationHandler}.
     */
    public void navigate() {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer sb = new StringBuffer(context.getViewRoot().getViewId());
        sb.append("?");
        sb.append(this.queryString);
        sb.append("&faces-redirect=true");

        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(context, null, sb.toString());
    }

    /**
     * Method to wait for new values from language selection selectOneMenu
     *
     * @param event event with old and new values (note: stock name is passed)
     */
    public void languageChangeEventHandler(ValueChangeEvent event) {
        final String languagePattern = "lang=" + event.getOldValue();
        final String languageReplacement = "lang=" + event.getNewValue();
        // no query string (e.g. index.xhtml) ==> create query string
        if (StringUtils.isBlank(this.queryString)) {
            this.queryString = languageReplacement;
        }
        // old query string contains a stock ==> replace with new stock
        else if (this.queryString.contains(languagePattern)) {
            this.queryString = this.queryString.replace(languagePattern, languageReplacement);
        }
        // query string is there, but contains no stock ==> append new stock
        else {
            this.queryString += "&" + languageReplacement;
        }
    }

    /**
     * Gets the list of all supported languages.
     */
    public List<String> getSupportedLanguages() {
        return this.supportedLanguages;
    }

}

