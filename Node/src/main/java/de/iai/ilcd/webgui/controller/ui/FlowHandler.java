package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.api.binding.generated.flow.FlowDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.model.IFlowListVO;
import de.fzk.iai.ilcd.service.model.IFlowVO;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.dao.FlowDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.flow.Flow;

/**
 * Backing bean for flow detail view
 *
 * @param <FT> flow type
 */
public abstract class FlowHandler<FT extends Flow> extends AbstractDataSetHandler<IFlowVO, FT, FlowDao<FT>, FlowDataSetType> {

    /**
     * Serialization Id
     */
    private static final long serialVersionUID = 5926993778095928321L;

    /**
     * Flag to determine if processes with this flow as input flow exist
     */
    private boolean processesWithInputFlowExist = true;

    /**
     * Flag to determine if processes with this flow as output flow exist
     */
    private boolean processesWithOutputFlowExist = true;

    /**
     * Flag to determine if processes with this flow as in- or output flow exist
     */
    private boolean processesWithInOrOutputFlowExist = true;

    /**
     * Flag to determine if processes with this flow as in- <strong>x</strong>or output flow exist
     */
    private boolean processesWithInXorOutputFlowExist = true;

    /**
     * Initialize handler
     */
    protected FlowHandler(FlowDao<FT> daoInstance) {
        super(daoInstance, IFlowListVO.URL_SUFFIX, ILCDTypes.FLOW);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return represented flow
     */
    public IFlowVO getFlow() {
        return this.getDataSet();
    }

    /**
     * Determine if elementary flow
     *
     * @return <code>true</code> if represented flow is elementary flow, else <code>false</code>
     */
    public boolean isElementaryFlow() {
        if (this.getFlow() != null) {
            return TypeOfFlowValue.ELEMENTARY_FLOW.equals(this.getFlow().getType());
        } else {
            return false;
        }
    }

    /**
     * Set the input / output flow exist boolean properties
     */
    @Override
    protected void datasetLoaded(Flow dataset) {

        if (ConfigurationService.INSTANCE.getDisplayConfig().isPreloadFlowInOutLinks()) {
            // if not globally disabled (set by default), set the booleans
            if (dataset != null) {
                final ProcessDao d = new ProcessDao();
                this.processesWithInputFlowExist = d.getProcessesForExchangeFlowCount(dataset.getUuidAsString(), ExchangeDirectionValues.INPUT) > 0;
                this.processesWithOutputFlowExist = d.getProcessesForExchangeFlowCount(dataset.getUuidAsString(), ExchangeDirectionValues.OUTPUT) > 0;
            } else {
                this.processesWithInputFlowExist = false;
                this.processesWithOutputFlowExist = false;
            }
            this.processesWithInOrOutputFlowExist = this.processesWithInputFlowExist || this.processesWithOutputFlowExist;
            this.processesWithInXorOutputFlowExist = this.processesWithInputFlowExist ^ this.processesWithOutputFlowExist;
        }
    }

    /**
     * Determine if processes with this flow as in- or output flow exist
     *
     * @return <code>true</code> if processes with this flow as in. or output flow exist, else <code>false</code>
     */
    public boolean isProcessesWithInOrOutputFlowExist() {
        return this.processesWithInOrOutputFlowExist;
    }

    /**
     * Determine if processes with this flow as input flow exist
     *
     * @return <code>true</code> if processes with this flow as input flow exist, else <code>false</code>
     */
    public boolean isProcessesWithInputFlowExist() {
        return this.processesWithInputFlowExist;
    }

    /**
     * Determine if processes with this flow as output flow exist
     *
     * @return <code>true</code> if processes with this flow as output flow exist, else <code>false</code>
     */
    public boolean isProcessesWithOutputFlowExist() {
        return this.processesWithOutputFlowExist;
    }

    /**
     * Determine if processes with this flow as in- <strong>x</strong>or output flow exist
     *
     * @return <code>true</code> if processes with this flow as in- <strong>x</strong>or output flow exist, else
     * <code>false</code>
     */
    public boolean isProcessesWithInXorOutputFlowExist() {
        return this.processesWithInXorOutputFlowExist;
    }
}
