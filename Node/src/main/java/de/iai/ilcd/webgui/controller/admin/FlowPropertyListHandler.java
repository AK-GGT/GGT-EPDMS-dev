package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.flowproperty.FlowProperty;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin flow property list handler
 */
@ManagedBean
@ViewScoped
public class FlowPropertyListHandler extends AbstractDataSetListHandler<FlowProperty> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8581509638490266475L;

    /**
     * Create the flow property list handler
     */
    public FlowPropertyListHandler() {
        super(FlowProperty.class, new FlowPropertyDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public FlowProperty[] getSelectedFlowProperties() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(FlowProperty[])
     * @deprecated use {@link #setSelectedItems(FlowProperty[])}
     */
    @Deprecated
    public void setSelectedFlowProperties(FlowProperty[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("flowpropertyTableForm:flowpropertyTable");
    }

}
