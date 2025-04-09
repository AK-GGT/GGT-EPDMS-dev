package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.LCIAMethodDataSetVO;
import de.fzk.iai.ilcd.service.client.impl.vo.types.lciamethod.TimeInformationType;
import de.fzk.iai.ilcd.service.model.IFlowPropertyVO;
import de.fzk.iai.ilcd.service.model.ILCIAMethodListVO;
import de.fzk.iai.ilcd.service.model.ILCIAMethodVO;
import de.iai.ilcd.model.adapter.LStringAdapter;

/**
 * Adapter for LCIA methods
 */
public class LCIAMethodVOAdapter extends AbstractDatasetAdapter<LCIAMethodDataSetVO, ILCIAMethodListVO, ILCIAMethodVO> {

    /**
     * Create adapter for {@link ILCIAMethodListVO LCIA method list value object}
     *
     * @param adaptee list value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodListVO adaptee) {
        super(new LCIAMethodDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO LCIA method value object}
     *
     * @param adaptee value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodVO adaptee) {
        super(new LCIAMethodDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link ILCIAMethodListVO LCIA method list value object}
     *
     * @param adaptee list value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodListVO adaptee, String language) {
        super(new LCIAMethodDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link ILCIAMethodListVO LCIA method list value object}
     *
     * @param adaptee list value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodListVO adaptee, String language, boolean langFallback) {
        super(new LCIAMethodDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO LCIA method value object}
     *
     * @param adaptee value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodVO adaptee, String language) {
        super(new LCIAMethodDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link IFlowPropertyVO LCIA method value object}
     *
     * @param adaptee value object to adapt
     */
    public LCIAMethodVOAdapter(ILCIAMethodVO adaptee, String language, boolean langFallback) {
        super(new LCIAMethodDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(ILCIAMethodListVO src, LCIAMethodDataSetVO dst) {
        TimeInformationType time = new TimeInformationType();

        LStringAdapter.copyLStrings(src.getTimeInformation().getDuration(), time.getDuration(), this.language);
        dst.setTimeInformation(time);

        dst.setType(src.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(ILCIAMethodVO src, LCIAMethodDataSetVO dst) {
        this.copyValues((ILCIAMethodListVO) src, dst);

        dst.setImpactIndicator(src.getImpactIndicator());
        dst.getAreaOfProtection().addAll(src.getAreaOfProtection());
        dst.getImpactCategory().addAll(src.getImpactCategory());
        dst.getMethodology().addAll(src.getMethodology());
    }

}
