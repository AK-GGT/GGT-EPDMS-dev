package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for flow property links
 */
public class FlowPropertyURLGenerator extends AbstractDataSetURLGenerator<FlowProperty> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public FlowPropertyURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.FLOWPROPERTY.name()), DatasetTypes.FLOWPROPERTIES.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<FlowProperty, ?, ?> createDaoInstance() {
        return new FlowPropertyDao();
    }

}
