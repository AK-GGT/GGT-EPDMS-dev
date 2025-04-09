package de.iai.ilcd.service;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.AdditionalTerm;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for managing additional terms defined in config file.
 *
 * @author sarai
 */
@Service("additionalTermsService")
public class AdditionalTermsService {

    /**
     * The additional terms defined in config file.
     */
    List<AdditionalTerm> additionalTerms = new ArrayList<AdditionalTerm>();
    private Logger log = LogManager.getLogger(this.getClass());

    /**
     * Reads out config file and creates list of additional terms with
     * properties definied in config file.
     */
    AdditionalTermsService() {
        Configuration conf = ConfigurationService.INSTANCE.getProperties();
        int count = 1;
        boolean requireAcceptance = conf.getBoolean("user.registration.additionalterms.1.require", false);
        String requiredMessage = conf.getString("user.registration.additionalterms.1.requiredMessage", null);
        String message = conf.getString("user.registration.additionalterms.1.message", null);
        String title = conf.getString("user.registration.additionalterms.1.title", null);
        String defaultGroup = conf.getString("user.registration.additionalterms.1.defaultGroup");

        while (title != null) {
            createAdditionalTerm(requireAcceptance, requiredMessage, message, title, defaultGroup);

            count++;

            requireAcceptance = conf.getBoolean("user.registration.additionalterms." + count + ".require", false);
            requiredMessage = conf.getString("user.registration.additionalterms." + count + ".requiredMessage", null);
            message = conf.getString("user.registration.additionalterms." + count + ".message", null);
            title = conf.getString("user.registration.additionalterms." + count + ".title", null);
            defaultGroup = conf.getString("user.registration.additionalterms." + count + ".defaultGroup");
        }

    }

    /**
     * Created an Additional Term object with given properties.
     *
     * @param requireAcceptance The flag indicating whether the addtional term needs to be
     *                          accepted
     * @param requiredMessage   The message shown if user did not accept additional term
     * @param message           The message shown for additional term
     * @param title             The title of additional term
     */
    private void createAdditionalTerm(boolean requireAcceptance, String requiredMessage, String message, String title,
                                      String defaultGroup) {
        AdditionalTerm additionalTerm = new AdditionalTerm();

        additionalTerm.setRequireAcceptance(requireAcceptance);
        additionalTerm.setRequiredMessage(requiredMessage);
        additionalTerm.setMessage(message);
        additionalTerm.setTitle(title);
        additionalTerm.setRenderAdditionalTerms(title != null);
        additionalTerm.setDefaultGroup(defaultGroup);

        additionalTerms.add(additionalTerm);
    }

    public List<AdditionalTerm> getAdditionalTerms() {
        return additionalTerms;
    }

}
