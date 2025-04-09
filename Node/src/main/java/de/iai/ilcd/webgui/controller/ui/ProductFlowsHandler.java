package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.ProductFlow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for Flow list view
 */
@ManagedBean(name = "productFlowsHandler")
@ViewScoped
public class ProductFlowsHandler extends AbstractDataSetsHandler<ProductFlow, ProductFlowDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1199873571011876462L;

    /**
     * Initialize handler
     */
    public ProductFlowsHandler() {
        super(ProductFlow.class, new ProductFlowDao());
    }

    /**
     * Determine if elementary flows (convenience for JSF)
     *
     * @return <code>false</code>
     */
    public boolean isElementaryFlows() {
        return false;
    }
}
