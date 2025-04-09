package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class LifeCycleModelListHandler extends AbstractDataSetListHandler<LifeCycleModel> {

    private static final long serialVersionUID = 5614196958391476138L;

    public LifeCycleModelListHandler() {
        super(LifeCycleModel.class, new LifeCycleModelDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public LifeCycleModel[] getSelectedLifeCycleModels() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(LifeCycleModel[])
     * @deprecated use {@link #setSelectedItems(LifeCycleModel[])}
     */
    @Deprecated
    public void setSelectedLifeCycleModels(LifeCycleModel[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("lifeCycleModelTableForm:lifeCycleModelTable");
    }
}