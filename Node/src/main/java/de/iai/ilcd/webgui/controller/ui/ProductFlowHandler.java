package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.ProductFlow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Handler for elementary flows
 */
@ViewScoped
@ManagedBean(name = "pFlowHandler")
public class ProductFlowHandler extends FlowHandler<ProductFlow> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6292017365478926609L;

    /**
     * Create handler
     */
    public ProductFlowHandler() {
        super(new ProductFlowDao());
    }

}
