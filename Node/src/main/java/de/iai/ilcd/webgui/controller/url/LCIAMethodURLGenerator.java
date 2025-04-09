package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.lciamethod.LCIAMethod;

/**
 * URL Generator for LCIA Method links
 */
public class LCIAMethodURLGenerator extends AbstractDataSetURLGenerator<LCIAMethod> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public LCIAMethodURLGenerator(URLGeneratorBean bean) {
        super(bean, "LCIAMethod", "LCIAMethod", DatasetTypes.LCIAMETHODS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<LCIAMethod, ?, ?> createDaoInstance() {
        return new LCIAMethodDao();
    }
}
