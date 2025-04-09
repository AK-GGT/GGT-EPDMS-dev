package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.io.Serializable;

/**
 * Converter for data stock meta data, must be a bean due to dependency injection, use via
 * <code>converter=&quot;#{dsMetaConverter}&quot;</code>
 */
@SessionScoped
@ManagedBean(name = "dsMetaConverter")
public class DataStockMetaConverter implements Converter, Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -6898653056457977172L;

    /**
     * Available stocks bean
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocks;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent ui, String value) {
        return this.availableStocks.getStock(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent ui, Object value) {
        return value != null ? ((IDataStockMetaData) value).getName() : null;
    }

    /**
     * Get the available stocks bean
     *
     * @return available stocks bean
     */
    public AvailableStockHandler getAvailableStocks() {
        return this.availableStocks;
    }

    /**
     * Set available stocks bean
     *
     * @param availableStocks available stocks bean to set
     */
    public void setAvailableStocks(AvailableStockHandler availableStocks) {
        this.availableStocks = availableStocks;
    }
}
