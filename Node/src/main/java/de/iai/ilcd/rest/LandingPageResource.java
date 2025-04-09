package de.iai.ilcd.rest;

import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST web service which simply redirects to the landing page of the database (which is not necessarily soda4LCA's own web interface).
 * This can be configured in soda4LCA.properties by setting the service.node.landingpage property. If not set, the auto-detected or
 * configured application base URI will be used.
 */
@Component
@Path("landingpage")
public class LandingPageResource {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(LandingPageResource.class);

    @GET
    public Response getLandingPage() {
        try {
            return Response.temporaryRedirect(new URI(ConfigurationService.INSTANCE.getLandingPageURL())).build();
        } catch (URISyntaxException e) {
            LOGGER.error("invalid landingpage URL " + ConfigurationService.INSTANCE.getLandingPageURL(), e);
        }
        return Response.status(404).build();
    }
}