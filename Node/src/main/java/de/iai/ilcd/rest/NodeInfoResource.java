package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.service.client.impl.vo.nodeinfo.NodeInfo;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author oliver.kusche
 */
@Component
@Path("nodeinfo")
public class NodeInfoResource {

    private static final Logger logger = LogManager.getLogger(NodeInfoResource.class);

    /**
     * Creates a new instance of NodeInfoResource
     */
    public NodeInfoResource() {
    }

    @GET
    @Produces("application/xml")
    public NodeInfo status() {
        logger.debug("nodeinfo");
        return ConfigurationService.INSTANCE.getNodeInfo();
    }

}
