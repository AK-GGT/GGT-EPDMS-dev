package eu.europa.ec.jrc.lca.registry.delegate;

import eu.europa.ec.jrc.lca.commons.domain.RegistryCredentials;
import eu.europa.ec.jrc.lca.commons.rest.dto.KeyWrapper;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.registry.domain.DataSet;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import eu.europa.ec.jrc.lca.registry.domain.NodeChangeLog;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.spec.RSAPublicKeySpec;

public final class NodeRestServiceBD extends RestServiceBD {
    private String accessAccount;

    private NodeRestServiceBD(Node node) {
        super(node);
        if (node.getRegistryCredentials() != null) {
            this.accessAccount = node.getRegistryCredentials().getAccessAccount();
        }
    }

    private NodeRestServiceBD(Node node, RegistryCredentials rc) {
        super(node);
        this.accessAccount = rc.getAccessAccount();
    }

    public static NodeRestServiceBD getInstance(Node node) {
        return new NodeRestServiceBD(node);
    }

    public static NodeRestServiceBD getInstance(Node node, RegistryCredentials rc) {
        return new NodeRestServiceBD(node, rc);
    }

    public void informAboutChanges(NodeChangeLog log) throws RestWSUnknownException {
        try {
            WebTarget wr = getResource("/synchronization/inform/" + accessAccount);
            wr.request().put(Entity.entity(log, MediaType.APPLICATION_XML_TYPE), Response.class);
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public void informAboutDatasetDeregistration(DataSet ds) throws RestWSUnknownException {
        try {
            WebTarget wr = getResource("/synchronization/informAboutDSDeregistration/" + accessAccount);
            wr.request().put(Entity.entity(ds, MediaType.APPLICATION_XML_TYPE), Response.class);
        } catch (ProcessingException | WebApplicationException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }


    public RSAPublicKeySpec getPublicKey() throws RestWSUnknownException {
        try {
            Response cr = getResponse("/key/public");
            KeyWrapper kw = cr.readEntity(KeyWrapper.class);
            return kw.getRSAPublicKeySpec();
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }
}
