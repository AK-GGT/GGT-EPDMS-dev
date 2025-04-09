package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.flow.ProductFlow;

/**
 * URL Generator for flow links
 */
public class ProductFlowURLGenerator extends AbstractDataSetURLGenerator<ProductFlow> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public ProductFlowURLGenerator(URLGeneratorBean bean) {
        super(bean, "productFlow", DatasetTypes.FLOWS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<ProductFlow, ?, ?> createDaoInstance() {
        return new ProductFlowDao();
    }
}
