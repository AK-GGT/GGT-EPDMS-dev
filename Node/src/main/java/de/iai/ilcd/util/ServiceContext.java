package de.iai.ilcd.util;

import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author clemens.duepmeier
 */
public class ServiceContext {

    private final static Logger LOGGER = LogManager.getLogger(ServiceContext.class);

    public ServiceContext() {
    }

    public URI getBaseUri() {
        try {
            return new URI(ConfigurationService.INSTANCE.getNodeInfo().getBaseURL());
        } catch (URISyntaxException e) {
            ServiceContext.LOGGER.debug("Illegal URI syntax in node information: " + ConfigurationService.INSTANCE.getNodeInfo().getBaseURL());
            return null;
        }
    }

    public String getNodeId() {
        return ConfigurationService.INSTANCE.getNodeId();
    }

    public String getNodeName() {
        return ConfigurationService.INSTANCE.getNodeName();
    }
}
