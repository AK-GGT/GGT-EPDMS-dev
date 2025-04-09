package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.flow.ElementaryFlow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Handler for elementary flows
 */
@ViewScoped
@ManagedBean(name = "eFlowHandler")
public class ElementaryFlowHandler extends FlowHandler<ElementaryFlow> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6292017822438926609L;

    /**
     * Create handler
     */
    public ElementaryFlowHandler() {
        super(new ElementaryFlowDao());
    }

}
