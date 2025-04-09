package eu.europa.ec.jrc.lca.commons.view.util;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectItemsProducer<T> {

    private List<SelectItem> items = new ArrayList<SelectItem>();

    public SelectItemsProducer() {

    }

    public SelectItemsProducer(List<T> entities) {
        setEntities(entities);
    }

    public List<SelectItem> getItems() {
        return items;
    }

    public final void setEntities(List<T> entities) {
        if (entities != null) {
            for (T entity : entities) {
                SelectItem item = new SelectItem();
                item.setValue(getValue(entity));
                item.setLabel(getLabel(entity));
                items.add(item);
            }
        }
    }

    public void addSelectItem(int position, SelectItem item) {
        items.add(position, item);
    }

    public void addNotSelectedItem() {
        SelectItem notSelected = new SelectItem("", Messages.getString(null, "notSelected", null));
        notSelected.setNoSelectionOption(true);
        this.addSelectItem(0, notSelected);
    }

    /**
     * Adds a select item "GLAD" as additional registry to select menu (if GLAD is enabled).
     */
    public void addGLADSelectedItem() {
        SelectItem gladItem = new SelectItem();
        gladItem.setLabel("GLAD");
        Long longVal = Long.valueOf(-1);
        gladItem.setValue(longVal.toString());
        items.add(gladItem);
    }

    public abstract Object getValue(T entity);

    public abstract String getLabel(T entity);
}
