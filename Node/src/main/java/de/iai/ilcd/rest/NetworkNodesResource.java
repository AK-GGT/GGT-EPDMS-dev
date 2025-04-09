package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.service.client.impl.vo.StringList;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.NetworkNodeDao;
import de.iai.ilcd.model.nodes.NetworkNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST Web Service
 *
 * @author oliver.kusche
 */
@Component
@Path("networknodes")
public class NetworkNodesResource extends AbstractResource {

    private static final Logger logger = LogManager.getLogger(NetworkNodesResource.class);

    /**
     * Creates a new instance of NodeInfoResource
     */
    public NetworkNodesResource() {
    }

    @GET
    @Path("nodeids")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNodeIds(@QueryParam(AbstractResource.PARAM_FORMAT) String format) {

        NetworkNodeDao dao = new NetworkNodeDao();

        List<NetworkNode> nodes = dao.getNetworkNodes();

        StringList result = new StringList();
        result.setIdentifier("nodeids");

        if (!ConfigurationService.INSTANCE.isProxyMode())
            result.addString(ConfigurationService.INSTANCE.getNodeId());

        for (NetworkNode n : nodes) {
            result.addString(n.getNodeId());
        }

        result.flush();

        return getListResponse(result, format);
    }
}
