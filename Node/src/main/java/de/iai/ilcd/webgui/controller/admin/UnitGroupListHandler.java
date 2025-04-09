package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.unitgroup.UnitGroup;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin handler for unit group lists
 */
@ManagedBean
@ViewScoped
public class UnitGroupListHandler extends AbstractDataSetListHandler<UnitGroup> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -7573123404473521778L;

    /**
     * Create the unit group handler
     */
    public UnitGroupListHandler() {
        super(UnitGroup.class, new UnitGroupDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public UnitGroup[] getSelectedUnitGroups() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(UnitGroup[])
     * @deprecated use {@link #setSelectedItems(UnitGroup[])}
     */
    @Deprecated
    public void setSelectedUnitGroups(UnitGroup[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("unitgroupTableForm:unitgroupTable");
    }

}
