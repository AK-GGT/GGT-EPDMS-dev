package de.iai.ilcd.webgui.controller.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class GuiListManagerUtils<T> {

    public List<GuiListManagerItem<T>> createGuiItemList(List<T> items) {
        List<GuiListManagerItem<T>> guiItemList = new ArrayList<GuiListManagerItem<T>>();

        for (T item : items) {
            GuiListManagerItem<T> guiItem = new GuiListManagerItem<T>(item);
            guiItemList.add(guiItem);
        }
        return guiItemList;
    }
}
