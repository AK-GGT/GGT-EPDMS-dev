package de.iai.ilcd.webgui.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.util.FacesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Logger logger = LogManager.getLogger(LocaleBean.class);

    // application available locales
    private static Map<String, Locale> available_locales = new LinkedHashMap<String, Locale>();

    private Locale locale;

    private String currentLanguage;

    public LocaleBean() {
        super();
        logger.debug("constructing LocaleBean");
    }

    @PostConstruct
    public void postConstruct() {
        init();
    }

    private void init() {
        Iterator<Locale> localeIterator = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        while (localeIterator.hasNext()) {
            Locale locale = localeIterator.next();
            if (logger.isDebugEnabled())
                logger.debug("found available locale: " + locale);
            available_locales.put(locale.toString(), locale);
        }

        this.locale = getApplicationLocale();

        if (logger.isDebugEnabled())
            logger.debug("application locale is " + this.locale);

        String language = this.locale.getLanguage().toLowerCase();
        this.currentLanguage = language;
        if (logger.isDebugEnabled())
            logger.debug("setting language to " + language);

    }

    @PreDestroy
    public void preDestroy() {
        destroy();
    }

    private void destroy() {
        this.locale = null;
        this.currentLanguage = null;
    }

    public void changeLocale() {
        this.locale = available_locales.get(this.currentLanguage);

        if (this.locale == null) {
            logger.info("locale for " + this.currentLanguage + " is contained in available locales, continuing anyway");
            this.locale = new Locale(this.currentLanguage);
        }

        setApplicationLocale(this.locale);
    }

    public Locale getApplicationLocale() {
        HttpServletRequest request = FacesUtil.getHttpServletRequest();
        UIViewRoot uiViewRoot = FacesUtil.getViewRoot();
        Locale locale = null;

        locale = getLocale();
        if ((uiViewRoot != null) && (locale == null)) {
            locale = uiViewRoot.getLocale();
        }
        if ((request != null) && (locale == null)) {
            locale = request.getLocale();
        }

        if (locale == null) {
            if (logger.isDebugEnabled())
                logger.debug("creating new locale with default language " + ConfigurationService.INSTANCE.getDefaultLanguage());
            locale = new Locale(ConfigurationService.INSTANCE.getDefaultLanguage());
        }

        if (logger.isDebugEnabled())
            logger.debug("current locale is " + locale);

        return locale;
    }

    public void setApplicationLocale(Locale locale) {
        logger.debug("setting locale to " + locale);

        UIViewRoot uiViewRoot = FacesUtil.getViewRoot();

        if (uiViewRoot != null) {
            uiViewRoot.setLocale(locale);
        }
    }

    public Object[] getAvailableLocaleKeys() {
        return available_locales.keySet().toArray();
    }

    public Locale getLocale() {
        String reqParamLang = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lang");

        if (reqParamLang != null && !reqParamLang.equals(this.currentLanguage)) {
            this.currentLanguage = reqParamLang;
            changeLocale();
            return this.locale;
        } else
            return this.locale;
    }

    public String getSelectedLocaleKey() {
        return this.currentLanguage;
    }

    public void setSelectedLocaleKey(String selectedLocaleKey) {
        this.currentLanguage = selectedLocaleKey;
    }

}