package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.DataSetVO;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.fzk.iai.ilcd.service.model.IDataSetVO;
import de.iai.ilcd.model.adapter.ClassificationAdapter;
import de.iai.ilcd.model.adapter.LStringAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractDatasetAdapter<T extends DataSetVO, LVO extends IDataSetListVO, VO extends IDataSetVO> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(AbstractDatasetAdapter.class);

    private final T dataset;

    protected String language = null;

    protected boolean langFallback = false;

    private AbstractDatasetAdapter(T adapterObject) {
        this.dataset = adapterObject;
    }

    public AbstractDatasetAdapter(T adapterObject, LVO adaptee) {
        this(adapterObject);
        this.copyValues(adaptee);
        this.copyValues(adaptee, this.dataset);
    }

    public AbstractDatasetAdapter(T adapterObject, VO adaptee) {
        this(adapterObject);
        this.copyValues(adaptee);
        this.copyValues(adaptee, this.dataset);
    }

    private AbstractDatasetAdapter(T adapterObject, String language) {
        this(adapterObject, language, false);
    }

    private AbstractDatasetAdapter(T adapterObject, String language, boolean langFallback) {
        this.dataset = adapterObject;
        this.language = language;
        this.langFallback = langFallback;
    }

    public AbstractDatasetAdapter(T adapterObject, LVO adaptee, String language) {
        this(adapterObject, adaptee, language, false);
    }

    public AbstractDatasetAdapter(T adapterObject, LVO adaptee, String language, boolean langFallback) {
        this(adapterObject, language, langFallback);
        this.copyValues(adaptee);
        this.copyValues(adaptee, this.dataset);
    }

    public AbstractDatasetAdapter(T adapterObject, VO adaptee, String language) {
        this(adapterObject, language);
        this.copyValues(adaptee);
        this.copyValues(adaptee, this.dataset);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Copy the values from the value <b>AND</b> the list value object
     * to the adapter data set.
     *
     * @param src source object
     * @param dst destination data set
     */
    protected abstract void copyValues(VO src, T dst);

    /**
     * Copy the values from the list value object
     * to the adapter data set
     *
     * @param src source object
     * @param dst destination data set
     */
    protected abstract void copyValues(LVO src, T dst);

    private final void copyValues(IDataSetVO src) {
        this.copyValues((IDataSetListVO) src);
        LStringAdapter.copyLStrings(src.getDescription(), this.dataset.getDescription(), this.language, this.langFallback);
    }

    private final void copyValues(IDataSetListVO src) {

        this.dataset.setUuid(src.getUuidAsString());
        this.dataset.setDataSetVersion(src.getDataSetVersion());
        this.dataset.setPermanentUri(src.getPermanentUri());

        LStringAdapter.copyLStrings(src.getName(), this.dataset.getName(), this.language, this.langFallback);

        try {
            this.dataset.setClassification(new ClassificationAdapter(src.getClassification()));
        } catch (Exception e) {
        }

        this.dataset.setSourceId(src.getSourceId());
        this.dataset.setHref(src.getHref());

        if (src.getDuplicates().size() > 0)
            this.dataset.setDuplicates(src.getDuplicates());
    }

    public T getDataSet() {
        return dataset;
    }

    public boolean isLangFallback() {
        return langFallback;
    }

    public void setLangFallback(boolean langFallback) {
        this.langFallback = langFallback;
    }

}
