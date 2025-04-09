package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.ProductFlow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin handler for product flow lists TODO check difference to {@link ElementaryFlowListHandler}
 */
@ManagedBean
@ViewScoped
public class ProductFlowListHandler extends AbstractDataSetListHandler<ProductFlow> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -602885503932425446L;

    /**
     * Create the contact list handler
     */
    public ProductFlowListHandler() {
        super(ProductFlow.class, new ProductFlowDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public ProductFlow[] getSelectedFlows() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(Flow[])
     * @deprecated use {@link #setSelectedItems(Flow[])}
     */
    @Deprecated
    public void setSelectedFlows(ProductFlow[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("flowTableForm:flowTable");
    }

}
