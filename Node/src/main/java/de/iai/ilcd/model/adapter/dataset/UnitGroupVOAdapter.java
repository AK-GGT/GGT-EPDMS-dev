package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.UnitGroupDataSetVO;
import de.fzk.iai.ilcd.service.model.ISourceListVO;
import de.fzk.iai.ilcd.service.model.ISourceVO;
import de.fzk.iai.ilcd.service.model.IUnitGroupListVO;
import de.fzk.iai.ilcd.service.model.IUnitGroupVO;

/**
 * Adapter for Sources
 */
public class UnitGroupVOAdapter extends AbstractDatasetAdapter<UnitGroupDataSetVO, IUnitGroupListVO, IUnitGroupVO> {

    /**
     * Create adapter for {@link ISourceListVO unit group list value object}
     *
     * @param adaptee list value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupListVO adaptee) {
        super(new UnitGroupDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link ISourceVO unit group value object}
     *
     * @param adaptee value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupVO adaptee) {
        super(new UnitGroupDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link ISourceListVO unit group list value object}
     *
     * @param adaptee list value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupListVO adaptee, String language) {
        super(new UnitGroupDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link ISourceListVO unit group list value object}
     *
     * @param adaptee list value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupListVO adaptee, String language, boolean langFallback) {
        super(new UnitGroupDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link ISourceVO unit group value object}
     *
     * @param adaptee value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupVO adaptee, String language) {
        super(new UnitGroupDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link ISourceVO unit group value object}
     *
     * @param adaptee value object to adapt
     */
    public UnitGroupVOAdapter(IUnitGroupVO adaptee, String language, boolean langFallback) {
        super(new UnitGroupDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IUnitGroupListVO src, UnitGroupDataSetVO dst) {
        dst.setDefaultUnit(src.getDefaultUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IUnitGroupVO src, UnitGroupDataSetVO dst) {
        this.copyValues((IUnitGroupListVO) src, dst);
    }

}
