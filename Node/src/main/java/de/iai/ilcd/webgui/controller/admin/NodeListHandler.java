package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.NetworkNodeDao;
import de.iai.ilcd.model.nodes.NetworkNode;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.Map;

/**
 * Admin node list handler
 */
@ViewScoped
@ManagedBean(name = "nodeListHandler")
public class NodeListHandler extends AbstractAdminListHandler<NetworkNode> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -2078948867527652211L;

    private final NetworkNodeDao dao = new NetworkNodeDao();

    public NodeListHandler() {
    }

    public void deleteSelected() {

        final NetworkNode[] selected = this.getSelectedItems();
        if (selected == null) {
            return;
        }

        for (NetworkNode node : selected) {
            try {
                this.dao.remove(node);
                this.addI18NFacesMessage("facesMsg.removeSuccess", FacesMessage.SEVERITY_INFO, node.getName());
            } catch (Exception e) {
                this.addI18NFacesMessage("facesMsg.removeError", FacesMessage.SEVERITY_ERROR, node.getName());
            }
        }

        this.clearSelection();
    }

    public NetworkNode[] getSelectedNodes() {
        return super.getSelectedItems();
    }

    public void setSelectedNodes(NetworkNode[] selected) {
        super.setSelectedItems(selected);
    }

    public boolean isNothingSelected() {
        return this.getSelectedItems() == null || this.getSelectedItems().length == 0;
    }

    @Override
    protected long loadElementCount() {
        return this.dao.getAllCount();
    }

    @Override
    protected void postConstruct() {
    }

    @Override
    public List<NetworkNode> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return this.dao.get(first, pageSize);
    }

}
