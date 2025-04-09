package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for source links
 */
public class SourceURLGenerator extends AbstractDataSetURLGenerator<Source> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public SourceURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.SOURCE.name()), DatasetTypes.SOURCES.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<Source, ?, ?> createDaoInstance() {
        return new SourceDao();
    }

    /**
     * Get resource URL for digital file of a source data set
     *
     * @param source the source data set to get digital file from
     * @return resource URL for digital file of a source data set
     */
    public String getResourceDigitalFile(Source source) {
        if (source != null) {
            return AbstractDataSetURLGenerator.REST_BASE_DIR + this.getResourceDir() + "/" + source.getUuidAsString() + "/digitalfile";
        }
        return null;
    }
}
