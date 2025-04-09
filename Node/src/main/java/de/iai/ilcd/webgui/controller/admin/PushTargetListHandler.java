package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.PushTarget;
import de.iai.ilcd.model.dao.PushTargetDao;
import de.iai.ilcd.webgui.controller.DirtyFlagBean;
import org.primefaces.model.SortOrder;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.Map;

/**
 * This class manages the list of {@link PushTarget}. It inherits from {@link AbstractAdminListHandler} with type PushTarget.
 *
 * @author sarai
 */
@ViewScoped
@ManagedBean
public class PushTargetListHandler extends AbstractAdminListHandler<PushTarget> {

    /*
     * serial id
     */
    private static final long serialVersionUID = -7609178158766618202L;
    private final PushTargetDao dao = new PushTargetDao();
    /**
     * Dirty flag bean
     */
    @ManagedProperty(value = "#{dirty}")
    private DirtyFlagBean dirty;

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
     * Gets the selected push target entries.
     *
     * @return Selected push target entries.
     */
    public PushTarget[] getSelectedPushTargets() {
        return super.getSelectedItems();
    }

    /**
     * Sets the selected push target entries.
     *
     * @param selectedItems The selected Push Target entries.
     */
    public void setSelectedPushTargets(PushTarget[] selectedItems) {
        super.setSelectedItems(selectedItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSelected() {
        final PushTarget[] selectedItems = this.getSelectedItems();
        if (selectedItems == null) {
            return;
        }
        for (PushTarget item : selectedItems) {
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
    public List<PushTarget> lazyLoad(int first, int pageSize, String sortField, SortOrder sortOrder,
                                     Map<String, Object> filters) {
        return this.dao.get(first, pageSize);
    }

    /**
     * Get the dirty flag bean
     *
     * @return dirty flag bean
     */
    public DirtyFlagBean getDirty() {
        return this.dirty;
    }

    /**
     * Set the dirty flag bean
     *
     * @param dirty the dirty flag bean
     */
    public void setDirty(DirtyFlagBean dirty) {
        this.dirty = dirty;

    }
}
