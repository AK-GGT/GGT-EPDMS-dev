package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.PushConfig;
import de.iai.ilcd.model.dao.PushConfigDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.Map;

/**
 * Manages The list of PushConfig entries. This class inherits from
 * AbstractAdminHandler with type PushConfig.
 *
 * @author sarai
 */
@ViewScoped
@ManagedBean
public class PushConfigListHandler extends AbstractAdminListHandler<PushConfig> {

    /*
     *
     */
    private static final long serialVersionUID = -6511053259734634705L;

    /*
     *
     */
    protected final Logger log = LogManager.getLogger(this.getClass());

    /*
     * The Data Access Object (DAO) for getting {@link PushConfig}
     */
    private final PushConfigDao dao = new PushConfigDao();

    /**
     * {@inheritDoc}
     */
    @Override
    protected long loadElementCount() {
        return this.dao.getAllCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void postConstruct() {

    }

    /**
     * Gets all selected Push Configs as an array.
     *
     * @return The selected PushConfigs
     */
    public PushConfig[] getSelectedPushConfigs() {
        return super.getSelectedItems();
    }

    /**
     * Sets the selected PushConfigs.
     *
     * @param selectedItems
     */
    public void setSelectedPushConfigs(PushConfig[] selectedItems) {
        super.setSelectedItems(selectedItems);
    }

    /**
     * Gets all Push Configs marked as favourites as a list.
     *
     * @return The PushConfigs marked as favourites
     */
    public List<PushConfig> getFavouritePushConfigs() {
        return this.dao.getFavouritePushConfigs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSelected() {
        final PushConfig[] selectedItems = this.getSelectedItems();
        if (selectedItems == null) {
            log.debug("no items selected.");
            return;
        }

        for (PushConfig item : selectedItems) {
            try {
                this.dao.remove(item);
                this.addI18NFacesMessage("facesMsg.removeSuccess", FacesMessage.SEVERITY_INFO, item.getName());
            } catch (Exception e) {
                this.addI18NFacesMessage("facesMsg.removeError", FacesMessage.SEVERITY_ERROR, item.getName());
            }
        }
        this.clearSelection();
        this.reloadCount();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PushConfig> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder,
                                     Map<String, Object> filters) {
        return this.dao.get(first, pageSize);
    }

}
