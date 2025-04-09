package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.client.FailedAuthenticationException;
import de.fzk.iai.ilcd.service.client.FailedConnectionException;
import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.client.impl.vo.DataStockVO;
import de.fzk.iai.ilcd.service.model.INodeInfo;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.NetworkNodeDao;
import de.iai.ilcd.model.dao.PersistType;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */

@ManagedBean
@ViewScoped
public class NodeHandler extends AbstractHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3864872300701299028L;

    private static final Logger logger = LogManager.getLogger(NodeHandler.class);

    private static final String REST_SERVLET_PREFIX = "resource/";
    private final NetworkNode localhostNodeInstance = new NetworkNode();
    private NetworkNode node = new NetworkNode();
    private String savedPassword;

    private List<DataStockVO> remoteDataStocks = new ArrayList<>();


    /**
     * Creates a new instance of NodeHandler
     */
    public NodeHandler() {
        this.copyProperties(this.localhostNodeInstance, ConfigurationService.INSTANCE.getNodeInfo());
    }

    /**
     * Given an URL we initialize our fields..
     */

    @PostConstruct
    public void init() {

        // check if the page was called with a request parameter specifying the current
        // node
        String nodeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nodeId");
        if (nodeId != null) {
            NetworkNodeDao nodeDao = new NetworkNodeDao();
            NetworkNode existingNode = nodeDao.getNetworkNode(Long.parseLong(nodeId));
            if (existingNode != null) {
                this.node = existingNode;
                NodeHandler.logger.info("found node with node ID {} and database ID {}", this.node.getNodeId(),
                        this.node.getId());
                this.savedPassword = node.getAccessPassword();

                String fixedUrl = addRestPrefix(node.getBaseUrl());
                try {
                    ILCDNetworkClient client = new ILCDNetworkClient(fixedUrl);
                    remoteDataStocks = client.getDataStocks().getDataStocks();
                } catch (IOException e) {
                    // TODO Auto-generated catch block e.printStackTrace(); } }
                } catch (ILCDServiceClientException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public NetworkNode getNode() {
        return this.node;
    }

    public void setNode(NetworkNode node) {
        this.node = node;
    }

    public NetworkNode getCurrentNode() {
        return this.localhostNodeInstance;
    }

    private void copyProperties(NetworkNode node, INodeInfo nodeInfo) {
        // copy properties to internal entity bean
        node.setNodeId(nodeInfo.getNodeID());
        node.setName(nodeInfo.getName());
        node.setOperator(nodeInfo.getOperator());
        node.setBaseUrl(nodeInfo.getBaseURL());
        node.setDescription(nodeInfo.getDescription().getValue());
        node.setAdminName(nodeInfo.getAdminName());
        node.setAdminPhone(nodeInfo.getAdminPhone());
        node.setAdminEmailAddress(nodeInfo.getAdminEMail());
        node.setAdminWwwAddress(nodeInfo.getAdminWWW());
    }

    public void createNode() {

        // if (registry == null) {
        // FacesMessage message = Messages.getMessage("resources.lang",
        // "registryIsReguired", null);
        // message.setSeverity(FacesMessage.SEVERITY_ERROR);
        // FacesContext.getCurrentInstance().addMessage(null, message);
        // return;
        // }

        NetworkNodeDao dao = new NetworkNodeDao();

        NodeHandler.logger.debug("trying to retrieve node info from " + this.node.getBaseUrl());

        ILCDNetworkClient client = null;

        try {
            String baseUrl = new String(this.node.getBaseUrl());
            baseUrl = addRestPrefix(baseUrl);

            // retrieve nodeinfo from foreign node
            client = new ILCDNetworkClient(baseUrl);
            client.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());
            INodeInfo nodeInfo = client.getNodeInfo();

            NodeHandler.logger.debug("retrieved node info for " + nodeInfo.getNodeID());

            this.copyProperties(this.node, nodeInfo);

        } catch (FailedConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FailedAuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof UnknownHostException) {
                NodeHandler.logger.info("host not found");
                this.addI18NFacesMessage("facesMsg.node.connectError", FacesMessage.SEVERITY_ERROR);
                return;
            }
        } catch (Exception e) {
            NodeHandler.logger.warn("unknown error retrieving node info");
            NodeHandler.logger.warn(e.getMessage());
            this.addI18NFacesMessage("facesMsg.node.infoError", FacesMessage.SEVERITY_ERROR);
            return;
        }

        NetworkNode existingNode = dao.getNetworkNode(this.node.getNodeId());
        if (existingNode != null) {
            this.addI18NFacesMessage("facesMsg.node.alreadyExists", FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (this.node.getNodeId() == null) {
            this.addI18NFacesMessage("facesMsg.node.noID", FacesMessage.SEVERITY_ERROR);
            return;
        }
        String nodeUrl = node.getBaseUrl();
        if (nodeUrl.endsWith(REST_SERVLET_PREFIX)) {
            node.setBaseUrl(StringUtils.substringBeforeLast(nodeUrl, "resource/"));
        }

        if (dao.checkAndPersist(this.node, PersistType.ONLYNEW, null)) {
            this.addI18NFacesMessage("facesMsg.node.addSuccess", FacesMessage.SEVERITY_INFO);
        } else {
            this.addI18NFacesMessage("facesMsg.node.saveError", FacesMessage.SEVERITY_ERROR);
        }

    }

    public void changeNode() {
        NetworkNodeDao dao = new NetworkNodeDao();

        if (node.getAccessPassword() == null || node.getAccessPassword().equals("")) {
            // OK, we don't change the password
            node.setAccessPassword(savedPassword);
        }

        NetworkNode existingNode = dao.getNetworkNode(this.node.getNodeId());
        if (existingNode == null) {
            this.addI18NFacesMessage("facesMsg.node.noExist", FacesMessage.SEVERITY_ERROR, this.node.getNodeId());
            return;
        }
        try {
            NodeHandler.logger.info("merge node with node name {} and id {}", this.node.getNodeId(), this.node.getId());
            this.node = dao.merge(this.node);
            this.addI18NFacesMessage("facesMsg.node.saveSuccess", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.saveDataError", FacesMessage.SEVERITY_ERROR);
        }

    }

    public List<DataStockVO> getRemoteDataStocks() {
        return remoteDataStocks;
    }

    public void setRemoteDataStocks(List<DataStockVO> remoteDataStocks) {
        this.remoteDataStocks = remoteDataStocks;
    }

    /**
     * Produces a simple label for a given DataStockVO object
     * that can be used in the UI.
     *
     * @param dataStock
     * @return
     */
    public String getLabelForDataStock(DataStockVO dataStock) {
        StringBuilder sb = new StringBuilder(dataStock.getShortName());
        sb.append(" (" + dataStock.getUuid() + ")");
        return sb.toString();
    }


    /**
     * Making sure the URL ends with "/resource/"
     *
     * @param baseUrl
     * @return
     */
    public String addRestPrefix(String baseUrl) {
        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
        if (!baseUrl.endsWith(REST_SERVLET_PREFIX))
            baseUrl += REST_SERVLET_PREFIX;
        return baseUrl;
    }


    // public Long getRegistry() {
    // return registry;
    // }
    //
    // public void setRegistry(Long registry) {
    // this.registry = registry;
    // }

}
