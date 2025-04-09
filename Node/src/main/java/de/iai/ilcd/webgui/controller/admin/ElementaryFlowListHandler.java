package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin elementary flow list handler TODO check difference to {@link ProductFlowListHandler}
 */
@ManagedBean
@ViewScoped
public class ElementaryFlowListHandler extends AbstractDataSetListHandler<ElementaryFlow> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -1386485091199071001L;

    /**
     * Create the contact list handler
     */
    public ElementaryFlowListHandler() {
        super(ElementaryFlow.class, new ElementaryFlowDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public ElementaryFlow[] getSelectedFlows() {
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
    public void setSelectedFlows(ElementaryFlow[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("flowTableForm:flowTable");
    }

}
