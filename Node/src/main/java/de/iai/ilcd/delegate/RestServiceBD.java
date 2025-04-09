package de.iai.ilcd.delegate;

import de.iai.ilcd.xml.read.JAXBContextResolver;
import eu.europa.ec.jrc.lca.commons.delegate.ProxyEnabledRestServiceBD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public abstract class RestServiceBD extends ProxyEnabledRestServiceBD {

    private final Logger logger = LogManager.getLogger(RestServiceBD.class);

    protected Client client;

    protected String restServletUrl;

    protected RestServiceBD(String serviceBasicUrl) {

        ClientConfig cc = new ClientConfig();
        cc.register(JAXBContextResolver.class);
        cc.property(ClientProperties.FOLLOW_REDIRECTS, true);

        cc.register(JacksonFeature.class);
        client = createClient(cc);
        this.restServletUrl = serviceBasicUrl;
    }

    protected WebTarget getResource(String servicePath) {
        return client.target(restServletUrl + servicePath);
    }

    protected Response getResponse(String servicePath) {
        return getResource(servicePath).request().get(Response.class);
    }
}
