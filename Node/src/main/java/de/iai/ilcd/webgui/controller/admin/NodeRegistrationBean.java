package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.client.impl.vo.nodeinfo.NodeInfo;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.rest.NodeRegistrationService;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.webgui.util.Consts;
import eu.europa.ec.jrc.lca.commons.domain.NodeCredentials;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeRegistrationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnexpectedStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import eu.europa.ec.jrc.lca.commons.view.util.Messages;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import jakarta.mail.internet.AddressException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@Component
@Scope("view")
public class NodeRegistrationBean implements Serializable {

    private static final long serialVersionUID = 8805276285244939010L;

    private static final Logger logger = LogManager.getLogger(NodeRegistrationBean.class);
    private final Registry registry;
    private final UserDao userDao = new UserDao();
    private Node node;
    @Autowired
    private NodeRegistrationService nodeRegistrationService;

    public NodeRegistrationBean() {
        this.registry = (Registry) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(Consts.SELECTED_REGISTRY);
        this.node = this.prepareNodeRegistrationData();
    }

    private Node prepareNodeRegistrationData() {
        User currentUser = this.userDao.getUser(SecurityUtil.getPrincipalName());
        NodeInfo nInf = ConfigurationService.INSTANCE.getNodeInfo();

        Node n = new Node();
        n.setNodeCredentials(new NodeCredentials());
        n.setAdminEmailAddress(currentUser.getEmail());
        n.setAdminName((currentUser.getFirstName() == null ? "" : currentUser.getFirstName()) + " "
                + (currentUser.getLastName() == null ? "" : currentUser.getLastName()));
        n.setAdminPhone(nInf.getAdminPhone());
        n.setAdminWebAddress(n.getAdminWebAddress());
        n.setName(nInf.getName());
        n.setNodeId(nInf.getNodeID());
        n.setDescription(nInf.getDescription() != null ? nInf.getDescription().getValue() : null);

        // for registering at the registry, URL must be without trailing "/resource/" path
        String nodeUrl = nInf.getBaseURL();
        if (nodeUrl.endsWith("/"))
            nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
        if (nodeUrl.endsWith("/resource"))
            nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 9);
        n.setBaseUrl(nodeUrl);

        return n;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void register() {
        logger.info("registering...");
        try {
            this.nodeRegistrationService.registerNode(this.registry, this.node);
            logger.info("Registration succeeded");
            FacesUtils.redirectToPage("/admin/listRegistries");
        } catch (NodeRegistrationException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "admin.registerNode.nodeRegistrationException", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            logger.info(message.getDetail() + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (RestWSUnexpectedStatusException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "nodeRegistration_restWSUnexpectedStatusException_errorMessage", new Object[]{e
                    .getResponseStatus()});
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            logger.info(message.getDetail() + " HTTP " + e.getResponseStatus(), e);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (RestWSUnknownException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "restWSUnknownException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            logger.info(message.getDetail(), e);
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (AddressException e) {
            FacesMessage message = Messages.getMessage("resources.lang", "addressException_errorMessage", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            logger.info(message.getDetail(), e);
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public Registry getRegistry() {
        return this.registry;
    }

}
