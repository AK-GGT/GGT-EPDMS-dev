package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.util.SodaUtil;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

/**
 * Base for process quality assurance handlers
 */
public abstract class AbstractProcessQAHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8746731864462070863L;

    /**
     * The language
     */
    private String lang;

    /**
     * Invoked by {@link #init()}, override if required
     */
    public void postConstruct() {

    }

    /**
     * Initialization after dependency injection
     */
    @PostConstruct
    public void init() {
        Locale reqLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        if (reqLocale != null) {
            this.lang = reqLocale.getLanguage();
        }
        this.postConstruct();
    }

    /**
     * Get value for provided language with fall back to default
     *
     * @param lang      desired language
     * @param multilang multi language string
     * @return value for provided language (or fall back)
     */
    public ILString getLStrWithFallback(String lang, IMultiLangString multilang) {
        return SodaUtil.getLStringWithFallback(lang, multilang);
    }

    /**
     * Get current language
     *
     * @return current language
     */
    public String getLang() {
        return this.lang;
    }
}
