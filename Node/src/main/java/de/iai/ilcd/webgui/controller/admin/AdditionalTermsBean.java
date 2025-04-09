package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.AdditionalTerm;
import de.iai.ilcd.service.AdditionalTermsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * The bean for managing additional terms defined in config file.
 *
 * @author sarai
 */
@ManagedBean(name = "additionalTermsBean")
@ApplicationScoped
public class AdditionalTermsBean {

    AdditionalTermsService additionalTermsService;
    @SuppressWarnings("unused")
    private Logger log = LogManager.getLogger(this.getClass());

    /**
     * Initializes all used services.
     */
    public AdditionalTermsBean() {
        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.additionalTermsService = ctx.getBean(AdditionalTermsService.class);
    }

    /**
     * Gets all additional terms.
     *
     * @return A list of all additional terms
     */
    public List<AdditionalTerm> getAdditionalTerms() {
        return additionalTermsService.getAdditionalTerms();
    }

    /**
     * Checks if any additional terms are defined in config file.
     *
     * @return true if at leat one additional term is defined in config file
     */
    public boolean isRenderAdditionalTerms() {
        return (additionalTermsService.getAdditionalTerms() != null)
                && !additionalTermsService.getAdditionalTerms().isEmpty();
    }

}
