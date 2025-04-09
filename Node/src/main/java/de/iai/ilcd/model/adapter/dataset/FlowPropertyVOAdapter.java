package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.FlowPropertyDataSetVO;
import de.fzk.iai.ilcd.service.client.impl.vo.types.flowproperty.UnitGroupType;
import de.fzk.iai.ilcd.service.model.IFlowPropertyListVO;
import de.fzk.iai.ilcd.service.model.IFlowPropertyVO;
import de.fzk.iai.ilcd.service.model.flowproperty.IUnitGroupType;
import de.iai.ilcd.model.adapter.GlobalReferenceAdapter;
import de.iai.ilcd.model.adapter.LStringAdapter;

/**
 * Adapter for Flow Properties
 */
public class FlowPropertyVOAdapter extends AbstractDatasetAdapter<FlowPropertyDataSetVO, IFlowPropertyListVO, IFlowPropertyVO> {

    /**
     * Create adapter for {@link IFlowPropertyListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyListVO adaptee) {
        super(new FlowPropertyDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyVO adaptee) {
        super(new FlowPropertyDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link IFlowPropertyListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyListVO adaptee, String language) {
        super(new FlowPropertyDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link IFlowPropertyListVO flow list value object}
     *
     * @param adaptee list value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyListVO adaptee, String language, boolean langFallback) {
        super(new FlowPropertyDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyVO adaptee, String language) {
        super(new FlowPropertyDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO flow value object}
     *
     * @param adaptee value object to adapt
     */
    public FlowPropertyVOAdapter(IFlowPropertyVO adaptee, String language, boolean langFallback) {
        super(new FlowPropertyDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IFlowPropertyListVO src, FlowPropertyDataSetVO dst) {
        IUnitGroupType details = src.getUnitGroupDetails();

        UnitGroupType newDetails = new UnitGroupType();
        try {
            newDetails.setName(src.getUnitGroupDetails().getName().getValue());
        } catch (Exception e) {
            newDetails.setName("(null)");
        }
        newDetails.setDefaultUnit(details.getDefaultUnit());
        newDetails.setHref(details.getHref());

        LStringAdapter.copyLStrings(details.getName(), newDetails.getName(), this.language);

        newDetails.setReference(new GlobalReferenceAdapter(details.getReference(), this.language));

        dst.setUnitGroupDetails(newDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IFlowPropertyVO src, FlowPropertyDataSetVO dst) {
        this.copyValues((IFlowPropertyListVO) src, dst);
        LStringAdapter.copyLStrings(src.getSynonyms(), dst.getSynonyms(), this.language);
    }
}
