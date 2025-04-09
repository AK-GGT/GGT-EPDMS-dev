package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.ReferenceFlowType;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;

/**
 * Adapter for {@link IReferenceFlow}
 */
public class ReferenceFlowAdapter extends ReferenceFlowType {

    /**
     * Create the adapter.
     *
     * @param adaptee instance to adapt
     */
    public ReferenceFlowAdapter(IReferenceFlow adaptee, String language) {
        super();
        if (adaptee != null) {
            this.setFlowName(new LStringAdapter(adaptee.getFlowName(), language, true).getLStrings());
            this.setFlowPropertyName(new LStringAdapter(adaptee.getFlowPropertyName(), language).getLStrings());
            this.setMeanValue(adaptee.getMeanValue());
            this.setReference(new GlobalReferenceAdapter(adaptee.getReference(), language, true));
            this.setUnit(adaptee.getUnit());
        }
    }

}
