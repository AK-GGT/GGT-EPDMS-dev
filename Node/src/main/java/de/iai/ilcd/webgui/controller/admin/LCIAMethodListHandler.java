package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.lciamethod.LCIAMethod;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Handler for admin LCIAMethod List
 */
@ManagedBean(name = "lciaMethodListHandler")
@ViewScoped
public class LCIAMethodListHandler extends AbstractDataSetListHandler<LCIAMethod> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -6771633832246107270L;

    /**
     * Create the handler
     */
    public LCIAMethodListHandler() {
        super(LCIAMethod.class, new LCIAMethodDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public LCIAMethod[] getSelectedLCIAMethods() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(LCIAMethod[])
     * @deprecated use {@link #setSelectedItems(LCIAMethod[])}
     */
    @Deprecated
    public void setSelectedLCIAMethods(LCIAMethod[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("lciamethodTableForm:lciamethodTable");
    }

}
