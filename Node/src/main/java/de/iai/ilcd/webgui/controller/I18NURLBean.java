package de.iai.ilcd.webgui.controller;

import de.iai.ilcd.webgui.util.LocaleBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

/**
 * Manages the custom content page URL, the imprint URL and privacy policy URL
 * if given URLs are available in several languages.
 *
 * @author sarai
 */
@ManagedBean(name = "langContentHandler")
@SessionScoped
public class I18NURLBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -9059148979684468691L;

    @SuppressWarnings("unused")
    private static Logger logger = LogManager.getLogger(I18NURLBean.class);

    @ManagedProperty(value = "#{localeBean}")
    LocaleBean localeBean;

    @ManagedProperty(value = "#{conf}")
    ConfigurationBean conf;

    private String localeCode;

    private String privacyPolicyURL;

    private String customIndexPageContentURL;

    private String imprintURL;

    /**
     * Replaces all occurrences of $lang key word by language code of current default language.
     */
    @PostConstruct
    public void init() {
        localeCode = localeBean.getSelectedLocaleKey();
        customIndexPageContentURL = conf.getCustomIndexPageContentURL();
        privacyPolicyURL = conf.getPrivacyPolicyURL();
        imprintURL = conf.getImprintURL();

        if (privacyPolicyURL != null) {
            privacyPolicyURL = privacyPolicyURL.replace("$lang", localeCode);
        }
        if (customIndexPageContentURL != null) {
            customIndexPageContentURL = customIndexPageContentURL.replace("$lang", localeCode);
        }
        if (imprintURL != null) {
            imprintURL = imprintURL.replace("$lang", localeCode);
        }
    }

    /**
     * Gets the custom index page content URL of current default language of application.
     *
     * @return The custom index content page URL of current default language
     */
    public String getCustomIndexPageContentURL() {
        return customIndexPageContentURL;
    }

    /**
     * Gets the imprint URL of current default language of application.
     *
     * @return The imprint URL of current default language
     */
    public String getImprintURL() {
        return imprintURL;
    }

    /**
     * Gets the privacy policy URL of current default language of application.
     *
     * @return The privacy policy URL of current default language
     */
    public String getPrivacyPolicyURL() {
        return privacyPolicyURL;
    }

    public ConfigurationBean getConf() {
        return this.conf;
    }

    public void setConf(ConfigurationBean conf) {
        this.conf = conf;
    }

    public LocaleBean getLocaleBean() {
        return this.localeBean;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }
}
