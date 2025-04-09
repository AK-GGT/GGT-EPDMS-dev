package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ElementaryFlowDao;
import de.iai.ilcd.model.flow.ElementaryFlow;

/**
 * URL Generator for flow links
 */
public class ElementaryFlowURLGenerator extends AbstractDataSetURLGenerator<ElementaryFlow> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public ElementaryFlowURLGenerator(URLGeneratorBean bean) {
        super(bean, "elementaryFlow", DatasetTypes.FLOWS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<ElementaryFlow, ?, ?> createDaoInstance() {
        return new ElementaryFlowDao();
    }
}
