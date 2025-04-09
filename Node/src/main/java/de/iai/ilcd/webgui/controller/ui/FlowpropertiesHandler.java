package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.flowproperty.FlowProperty;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for flow property list view
 */
@ManagedBean()
@ViewScoped
public class FlowpropertiesHandler extends AbstractDataSetsHandler<FlowProperty, FlowPropertyDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -4221400320067993534L;

    /**
     * Initialize handler
     */
    public FlowpropertiesHandler() {
        super(FlowProperty.class, new FlowPropertyDao());
    }

}
