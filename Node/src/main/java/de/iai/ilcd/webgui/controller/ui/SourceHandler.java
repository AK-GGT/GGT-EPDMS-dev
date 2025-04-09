package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.source.SourceDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ISourceVO;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.util.SodaUtil;

import javax.faces.bean.ManagedBean;

/**
 * Backing bean for source detail view
 */
@ManagedBean
public class SourceHandler extends AbstractDataSetHandler<ISourceVO, Source, SourceDao, SourceDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 8501172387748377828L;

    /**
     * Initialize handler
     */
    public SourceHandler() {
        super(new SourceDao(), DatasetTypes.SOURCES.getValue(), ILCDTypes.SOURCE);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return represented source instance
     */
    public ISourceVO getSource() {
        return this.getDataSet();
    }

    /**
     * Checks if given filename has image extension
     *
     * @param fileName filename to check for image extension
     * @return <code>true</code> if given file name has image extension, else <code>false</code>
     */
    public boolean hasImageExtension(String fileName) {
        return SodaUtil.hasImageExtension(fileName);
    }

    /**
     * Checks if given filename has valid extension
     *
     * @param fileName filename to check for valid extension
     * @return <code>true</code> if given file name has valid extension, else <code>false</code>
     */
    public boolean hasValidExtension(String fileName) {
        return SodaUtil.hasValidExtension(fileName);
    }

}
