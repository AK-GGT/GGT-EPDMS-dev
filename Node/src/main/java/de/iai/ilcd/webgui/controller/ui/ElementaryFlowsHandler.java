package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.flow.ElementaryFlow;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for Flow list view
 */
@ManagedBean()
@ViewScoped
public class ElementaryFlowsHandler extends AbstractDataSetsHandler<ElementaryFlow, ElementaryFlowDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1191312271011876462L;

    /**
     * Initialize handler
     */
    public ElementaryFlowsHandler() {
        super(ElementaryFlow.class, new ElementaryFlowDao());
    }

    /**
     * Determine if elementary flows (convenience for JSF)
     *
     * @return <code>true</code>
     */
    public boolean isElementaryFlows() {
        return true;
    }
}

