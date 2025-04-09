package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for unit group links
 */
public class UnitGroupURLGenerator extends AbstractDataSetURLGenerator<UnitGroup> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public UnitGroupURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.UNITGROUP.name()), DatasetTypes.UNITGROUPS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<UnitGroup, ?, ?> createDaoInstance() {
        return new UnitGroupDao();
    }
}
