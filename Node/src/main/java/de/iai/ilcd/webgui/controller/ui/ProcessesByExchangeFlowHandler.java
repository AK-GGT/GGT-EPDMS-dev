package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Process;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Handler for Processes by Exchange Flow
 */
@ManagedBean
@ViewScoped
public class ProcessesByExchangeFlowHandler extends AbstractDataSetsHandler<Process, ProcessDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 5226339176432015120L;

    /**
     * Key for name filter
     */
    private final static String UUID_FILTER_KEY = "exchangeFlow";

    /**
     * Filter key for classes.
     */
    private final static String DIRECTION_FILTER_KEY = "exchangeFlowDirection";

    /**
     * Default name of the flow
     */
    private String flowDefaultName;

    /**
     * Handler for Searches for Processes by exchange flow
     */
    public ProcessesByExchangeFlowHandler() {
        super(Process.class, new ProcessDao());
    }

    /**
     * Get the uuid of the flow
     *
     * @return uuid of the flow
     */
    public String getFlowUuid() {
        return super.getFilter(ProcessesByExchangeFlowHandler.UUID_FILTER_KEY);
    }

    /**
     * Set the uuid of the flow
     *
     * @param flowUuid flow uuid to set
     */
    public void setFlowUuid(String flowUuid) {
        super.setFilter(ProcessesByExchangeFlowHandler.UUID_FILTER_KEY, flowUuid);
        ProductFlowDao fDao = new ProductFlowDao();
        ProductFlow pFlow = fDao.getByUuid(flowUuid);
        if (pFlow != null) {
            this.flowDefaultName = pFlow.getDefaultName();
        }
    }

    /**
     * Get the direction
     *
     * @return direction
     */
    public String getDirection() {
        Object o = super.getFilterObject(ProcessesByExchangeFlowHandler.DIRECTION_FILTER_KEY);
        if (o instanceof ExchangeDirectionValues) {
            return ((ExchangeDirectionValues) o).name(); // else value would be used via toString()!
        } else {
            return null;
        }
    }

    /**
     * Set the direction
     *
     * @param direction direction to set
     */
    public void setDirection(String direction) {
        try {
            super.setFilter(ProcessesByExchangeFlowHandler.DIRECTION_FILTER_KEY, ExchangeDirectionValues.valueOf(direction));
        } catch (Exception e) {
            super.setFilter(ProcessesByExchangeFlowHandler.DIRECTION_FILTER_KEY, (Object) null);
        }
    }

    /**
     * Get the default name of the flow
     *
     * @return default name of the flow
     */
    public String getFlowDefaultName() {
        return this.flowDefaultName;
    }

    /**
     * Check if the exchange direction is input
     *
     * @return <code>true</code> for input, else <code>false</code>
     */
    public boolean isInputDirection() {
        return ExchangeDirectionValues.INPUT.equals(super.getFilterObject(ProcessesByExchangeFlowHandler.DIRECTION_FILTER_KEY));
    }

    /**
     * Check if the exchange direction is output
     *
     * @return <code>true</code> for output, else <code>false</code>
     */
    public boolean isOutputDirection() {
        return ExchangeDirectionValues.OUTPUT.equals(super.getFilterObject(ProcessesByExchangeFlowHandler.DIRECTION_FILTER_KEY));
    }
}
