package eu.europa.ec.jrc.lca.registry.delegate;

import eu.europa.ec.jrc.lca.commons.delegate.ProxyEnabledRestServiceBD;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import eu.europa.ec.jrc.lca.registry.rest.JAXBContextResolver;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public abstract class RestServiceBD extends ProxyEnabledRestServiceBD {
    protected Client client;

    protected String restServletUrl;

    private static final String REST_PATH = "/resource";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected RestServiceBD(Node node) {
        ClientConfig cc = new ClientConfig();
        cc.register(JAXBContextResolver.class);
        cc.property(ClientProperties.FOLLOW_REDIRECTS, true);

        client = createClient(cc);
        this.restServletUrl
                = node.getBaseUrl() + REST_PATH;

        //TODO remove
//        if (logger.isDebugEnabled()) {
//            // test connection
//            ClientResponse response = client.target("https://okworx.lca-data.com/resource/nodeinfo").request().get(ClientResponse.class);
//            String result = response.readEntity(String.class);
//            logger.debug("response status is " + response.getStatus() + " / " + result);
//        }
    }

    protected WebTarget getResource(String servicePath) {
        return client.target(restServletUrl + servicePath);
    }

    protected Response getResponse(String servicePath) {
        return getResource(servicePath).request().get(Response.class);

    }

}
