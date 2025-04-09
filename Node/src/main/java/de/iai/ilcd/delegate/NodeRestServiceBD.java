package de.iai.ilcd.delegate;

import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.model.registry.RegistryStatus;
import eu.europa.ec.jrc.lca.commons.domain.NodeCredentials;
import eu.europa.ec.jrc.lca.commons.domain.RegistryCredentials;
import eu.europa.ec.jrc.lca.commons.rest.CustomStatus;
import eu.europa.ec.jrc.lca.commons.rest.dto.KeyWrapper;
import eu.europa.ec.jrc.lca.commons.rest.dto.SecuredRequestWrapper;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeRegistrationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnexpectedStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import eu.europa.ec.jrc.lca.registry.domain.NodeStatus;
import jakarta.mail.internet.AddressException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/*
 * Business Delegate class - encapsulates remote service methods invocations
 */

public class NodeRestServiceBD extends RestServiceBD {

    private static final String REST_PATH = "rest";
    private static Map<String, RSAPublicKeySpec> keys = new HashMap<String, RSAPublicKeySpec>();
    private final Logger logger = LogManager.getLogger(NodeRestServiceBD.class);

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder builder;

    {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private NodeRestServiceBD(String serviceBasicUrl) {
        super(serviceBasicUrl);
    }

    public static NodeRestServiceBD getInstance(Registry registry) {
        return new NodeRestServiceBD(registry.getBaseUrl() + REST_PATH);
    }

    public RegistryCredentials register(Node node) throws NodeRegistrationException, RestWSUnexpectedStatusException, RestWSUnknownException,
            AddressException {
        try {
            WebTarget wr = getResource("/nodeservice/register");

            Response cr = wr.request(MediaType.APPLICATION_XML).post(Entity.entity(node, MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new NodeRegistrationException();
            }
            if (cr.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                throw new AddressException();
            }
            if (!(cr.getStatus() == Response.Status.OK.getStatusCode())) {
                throw new RestWSUnexpectedStatusException(cr.getStatus());
            }
            return cr.readEntity(RegistryCredentials.class);
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public RegistryStatus checkAcceptance(Node node) throws RestWSUnknownException {
        try {
            WebTarget wr = getResource("/nodeservice/checkAcceptance");
            Response cr = wr.request(MediaType.APPLICATION_XML).put(Entity.entity(node, MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == CustomStatus.ENTITY_NOT_FOUND.getCode()) {
                return RegistryStatus.NOT_REGISTERED;
            }

            String statusJaxbEl = cr.readEntity(String.class);

            NodeStatus status = NodeStatus.valueOf(parseStatus(statusJaxbEl));
            if (status == NodeStatus.NOT_APPROVED) {
                return RegistryStatus.PENDING_REGISTRATION;
            }
            return RegistryStatus.REGISTERED;
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public List<Node> wake(NodeCredentials credentials) throws AuthenticationException, RestWSUnknownException, RestWSUnexpectedStatusException {
        try {
            WebTarget wr = getResource("/nodeservice/wake");

            Response cr = wr.request(MediaType.APPLICATION_XML).put(Entity.entity(new SecuredRequestWrapper(credentials), MediaType.APPLICATION_XML), Response.class);
            if (logger.isDebugEnabled()) {
                logger.debug("called " + wr.getUri().toString());
                logger.debug("response status: " + cr.getStatus());
                logger.debug("response: " + cr.toString());
            }
            if (cr.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException();
            } else if (!(cr.getStatus() == Response.Status.OK.getStatusCode())) {
                throw new RestWSUnexpectedStatusException(cr.getStatus());
            }
            return cr.readEntity(new GenericType<List<Node>>() {
            });
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public Node getNodeData(String nodeId) throws RestWSUnknownException {
        try {
            Response cr = getResponse("/nodeservice/node/" + URLEncoder.encode(nodeId, StandardCharsets.UTF_8));
            if (cr.getStatus() == CustomStatus.ENTITY_NOT_FOUND.getCode()) {
                return null;
            }
            return cr.readEntity(Node.class);
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public void deregister(NodeCredentials credentials) throws AuthenticationException, RestWSUnknownException, RestWSUnexpectedStatusException {
        try {
            WebTarget wr = getResource("/nodeservice/deregister");

            Response cr = wr.request(MediaType.APPLICATION_XML).put(Entity.entity(new SecuredRequestWrapper(credentials), MediaType.APPLICATION_XML), Response.class);
            if (cr.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException();
            }
            if (!(cr.getStatus() == Response.Status.OK.getStatusCode())) {
                throw new RestWSUnexpectedStatusException(cr.getStatus());
            }
        } catch (ProcessingException ex) {
            throw new RestWSUnknownException(ex.getMessage());
        }
    }

    public RSAPublicKeySpec getPublicKey() throws RestWSUnknownException {
        if (keys.get(restServletUrl) == null) {
            try {
                Response cr = getResponse("/key/public");
                KeyWrapper kw = cr.readEntity(KeyWrapper.class);
                keys.put(restServletUrl, kw.getRSAPublicKeySpec());
            } catch (ProcessingException ex) {
                throw new RestWSUnknownException(ex.getMessage());
            }
        }
        return keys.get(restServletUrl);
    }

    private String parseStatus(String xmlString) {
        final Document document;
        final String nodeStatusValue;
        final String tagName = "nodeStatus";
        try {
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        NodeList nodeList = document.getElementsByTagName(tagName);

        if (nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            nodeStatusValue = element.getTextContent();
        } else {
            throw new NoSuchElementException(String.format("nodeStatus element not found in %s", xmlString));
        }
        return nodeStatusValue;
    }
}
