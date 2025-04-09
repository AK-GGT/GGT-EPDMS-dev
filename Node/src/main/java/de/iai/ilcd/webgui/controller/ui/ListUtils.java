package de.iai.ilcd.webgui.controller.ui;

import org.primefaces.component.datatable.DataTable;

import javax.faces.context.FacesContext;

public class ListUtils {

    /**
     * Temporäre (?!) Hilfsmethode, solange es noch Bugs bei DataTable gibt …
     *
     * @param dataTableName Name der DataTable (Bsp.: tableForm:processTable)
     */
    public static void fixDataTable(String dataTableName) {
        Object dataTableObj = FacesContext.getCurrentInstance().getViewRoot().findComponent(dataTableName);
        if (dataTableObj instanceof DataTable) {
            ((DataTable) dataTableObj).loadLazyData();
        }
    }

}
