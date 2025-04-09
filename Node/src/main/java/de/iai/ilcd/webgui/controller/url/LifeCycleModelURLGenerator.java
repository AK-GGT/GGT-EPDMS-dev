package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LifeCycleModelDao;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import org.apache.commons.lang.StringUtils;

public class LifeCycleModelURLGenerator extends AbstractDataSetURLGenerator<LifeCycleModel> {

    public LifeCycleModelURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.LIFECYCLEMODEL.name()), DatasetTypes.LIFECYCLEMODELS.getValue());
    }

    @Override
    public DataSetDao<LifeCycleModel, ?, ?> createDaoInstance() {
        return new LifeCycleModelDao();
    }

    public String getDataSetDetailHTML(LifeCycleModel lcm) {
        String uuid = lcm.getUuidAsString(), version = lcm.getDataSetVersion();
        if (uuid != null) {
            return "/datasetdetail/lifecyclemodel.xhtml?uuid=" + uuid
                    + (StringUtils.isNotBlank(version) ? "&amp;version=" + version : "")
                    + (StringUtils.isNotBlank(null) ? "&amp;" + super.getStockURLParam(null, false) : "");
        }
        return null;
    }
}
