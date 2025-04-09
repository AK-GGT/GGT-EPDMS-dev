package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.QuantitativeReferenceType;
import de.fzk.iai.ilcd.service.model.process.IQuantitativeReference;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;

import java.util.List;

/**
 * Adapter for {@link IQuantitativeReference}
 */
public class QuantitativeReferenceAdapter extends QuantitativeReferenceType {

    /**
     * Create the adapter.
     *
     * @param adaptee instance to adapt
     */
    public QuantitativeReferenceAdapter(IQuantitativeReference adaptee, String language) {
        super();
        if (adaptee != null) {

            this.setType(adaptee.getType());

            LStringAdapter.copyLStrings(adaptee.getFunctionalUnit(), this.getFunctionalUnit(), language);

            final List<IReferenceFlow> adapteeRefFlows = adaptee.getReferenceFlows();
            final List<IReferenceFlow> adapterRefFlows = this.getReferenceFlows();
            if (adapteeRefFlows != null) {
                for (IReferenceFlow refFlow : adapteeRefFlows) {
                    adapterRefFlows.add(new ReferenceFlowAdapter(refFlow, language));
                }
            }

        }
    }

}
