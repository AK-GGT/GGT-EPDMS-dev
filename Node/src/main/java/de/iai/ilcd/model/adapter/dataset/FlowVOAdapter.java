package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.FlowDataSetVO;
import de.fzk.iai.ilcd.service.client.impl.vo.types.flow.ReferenceFlowPropertyType;
import de.fzk.iai.ilcd.service.model.IFlowListVO;
import de.fzk.iai.ilcd.service.model.IFlowVO;
import de.iai.ilcd.model.adapter.FlowCategorizationAdapter;
import de.iai.ilcd.model.adapter.GlobalReferenceAdapter;
import de.iai.ilcd.model.adapter.LStringAdapter;

/**
 * Adapter for Flows
 */
public class FlowVOAdapter extends AbstractDatasetAdapter<FlowDataSetVO, IFlowListVO, IFlowVO> {

    /**
     * Create adapter for {@link IFlowListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowVOAdapter(IFlowListVO adaptee) {
        super(new FlowDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link IFlowVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowVOAdapter(IFlowVO adaptee) {
        super(new FlowDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link IFlowListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowVOAdapter(IFlowListVO adaptee, String language) {
        super(new FlowDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link IFlowListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowVOAdapter(IFlowListVO adaptee, String language, boolean langFallback) {
        super(new FlowDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link IFlowVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowVOAdapter(IFlowVO adaptee, String language) {
        super(new FlowDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link IFlowVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowVOAdapter(IFlowVO adaptee, String language, boolean langFallback) {
        super(new FlowDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IFlowListVO src, FlowDataSetVO dst) {
        try {
            dst.setFlowCategorization(new FlowCategorizationAdapter(src.getFlowCategorization()));
        } catch (Exception e) {
            // Ignore
        }

        ReferenceFlowPropertyType flowProperty = new ReferenceFlowPropertyType();

        if (src.getReferenceFlowProperty() != null) {
            flowProperty.setDefaultUnit(src.getReferenceFlowProperty().getDefaultUnit());
            flowProperty.setReference(new GlobalReferenceAdapter(src.getReferenceFlowProperty().getReference(), this.language));
            flowProperty.setHref(src.getReferenceFlowProperty().getHref());
            LStringAdapter.copyLStrings(src.getReferenceFlowProperty().getName(), flowProperty.getName(), this.language);
        }

        if (src.getLocationOfSupply() != null)
            LStringAdapter.copyLStrings(src.getLocationOfSupply(), dst.getLocationOfSupply(), this.language);

        dst.setReferenceFlowProperty(flowProperty);

        dst.setType(src.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IFlowVO src, FlowDataSetVO dst) {
        this.copyValues((IFlowListVO) src, dst);
        LStringAdapter.copyLStrings(src.getSynonyms(), dst.getSynonyms(), this.language);
        dst.setCasNumber(src.getCasNumber());
        dst.setSumFormula(src.getSumFormula());
    }
}
