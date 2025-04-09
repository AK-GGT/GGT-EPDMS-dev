package de.iai.ilcd.webgui.controller.admin;

import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
public class GuiListManagerItem<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2510443804235599302L;

    // item contained in the list
    private T item;

    // the item is flagged for deletion
    private boolean shouldBeDeleted = false;

    public GuiListManagerItem(T item) {
        this.item = item;
        shouldBeDeleted = false;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public boolean isShouldBeDeleted() {
        return shouldBeDeleted;
    }

    public void setShouldBeDeleted(boolean shouldBeDeleted) {
        this.shouldBeDeleted = shouldBeDeleted;
    }
}
