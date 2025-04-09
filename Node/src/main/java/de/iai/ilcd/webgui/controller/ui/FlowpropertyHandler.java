package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.flowproperty.FlowPropertyDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IFlowPropertyVO;
import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.flowproperty.FlowProperty;

import javax.faces.bean.ManagedBean;

/**
 * Backing bean for flow property detail view
 */
@ManagedBean
public class FlowpropertyHandler extends AbstractDataSetHandler<IFlowPropertyVO, FlowProperty, FlowPropertyDao, FlowPropertyDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6721372426024117074L;

    /**
     * Initialize handler
     */
    public FlowpropertyHandler() {
        super(new FlowPropertyDao(), DatasetTypes.FLOWPROPERTIES.getValue(), ILCDTypes.FLOWPROPERTY);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return get represented flow property
     */
    public IFlowPropertyVO getFlowproperty() {
        return this.getDataSet();
    }

}
