package de.iai.ilcd.gui.utils;

import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.configuration.Configuration;

/**
 * TODO: delete when velocity was removed
 *
 * @author clemens.duepmeier
 */
public class ConfigurationTool {

    private Configuration config = ConfigurationService.INSTANCE.getProperties();

    public String getLayoutDirectory() {
        String directory = config.getString("theme.layout.directory");
        if (directory != null)
            return directory;
        return "/layout/default";
    }

    public String getResourceDirectory() {
        String directory = config.getString("theme.resource.directory");
        if (directory != null)
            return directory;
        return "res/default";
    }

    public boolean isRegistrationActivated() {
        boolean registrationActivated = config.getBoolean("user.registration.activated", false);

        return registrationActivated;
    }

    public boolean isAccessRestricted() {
        boolean accessRestricted = config.getBoolean("security.guest.metadataOnly", true);

        return accessRestricted;
    }

}
