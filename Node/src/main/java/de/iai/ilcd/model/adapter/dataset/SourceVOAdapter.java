package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.SourceDataSetVO;
import de.fzk.iai.ilcd.service.model.ISourceListVO;
import de.fzk.iai.ilcd.service.model.ISourceVO;
import de.iai.ilcd.model.adapter.GlobalReferenceAdapter;
import de.iai.ilcd.model.adapter.LStringAdapter;

/**
 * Adapter for Sources
 */
public class SourceVOAdapter extends AbstractDatasetAdapter<SourceDataSetVO, ISourceListVO, ISourceVO> {

    /**
     * Create adapter for {@link ISourceListVO source list value object}
     *
     * @param adaptee list value object to adapt
     */
    public SourceVOAdapter(ISourceListVO adaptee) {
        super(new SourceDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link ISourceVO source value object}
     *
     * @param adaptee value object to adapt
     */
    public SourceVOAdapter(ISourceVO adaptee) {
        super(new SourceDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link ISourceListVO source list value object}
     *
     * @param adaptee list value object to adapt
     */
    public SourceVOAdapter(ISourceListVO adaptee, String language) {
        super(new SourceDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link ISourceListVO source list value object}
     *
     * @param adaptee list value object to adapt
     */
    public SourceVOAdapter(ISourceListVO adaptee, String language, boolean langFallback) {
        super(new SourceDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link ISourceVO source value object}
     *
     * @param adaptee value object to adapt
     */
    public SourceVOAdapter(ISourceVO adaptee, String language) {
        super(new SourceDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link ISourceVO source value object}
     *
     * @param adaptee value object to adapt
     */
    public SourceVOAdapter(ISourceVO adaptee, String language, boolean langFallback) {
        super(new SourceDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(ISourceListVO src, SourceDataSetVO dst) {
        GlobalReferenceAdapter.copyGlobalReferences(src.getBelongsTo(), dst.getBelongsTo(), this.language);
        GlobalReferenceAdapter.copyGlobalReferences(src.getFileReferences(), dst.getFileReferences(), this.language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(ISourceVO src, SourceDataSetVO dst) {
        this.copyValues((ISourceListVO) src, dst);

        LStringAdapter.copyLStrings(src.getCitation(), dst.getCitation(), this.language);
        dst.setPublicationType(src.getPublicationType());
    }

}
