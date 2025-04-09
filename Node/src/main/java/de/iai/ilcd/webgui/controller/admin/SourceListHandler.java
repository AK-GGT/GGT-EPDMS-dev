package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin handler for source lists
 */
@ManagedBean
@ViewScoped
public class SourceListHandler extends AbstractDataSetListHandler<Source> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 4869198441468232669L;

    /**
     * Create the source list handler
     */
    public SourceListHandler() {
        super(Source.class, new SourceDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public Source[] getSelectedSources() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(Source[])
     * @deprecated use {@link #setSelectedItems(Source[])}
     */
    @Deprecated
    public void setSelectedSources(Source[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("sourceTableForm:sourceTable");
    }

}
