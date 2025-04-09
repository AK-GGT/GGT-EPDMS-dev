package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.FlowDaoWrapper;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for flow links
 */
public class FlowURLGenerator extends AbstractDataSetURLGenerator<Flow> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public FlowURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.FLOW.name()), DatasetTypes.FLOWS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<Flow, ?, ?> createDaoInstance() {
        return new FlowDaoWrapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetail(Flow object, String stockName) {
        String prefix = object instanceof ElementaryFlow ? "Elementary" : "Product";
        if (object != null) {
            return "/show" + prefix + "Flow.xhtml?uuid=" + this.encodeURL(object.getUuidAsString())
                    + (object.isMostRecentVersion() ? "" : "&version=" + this.encodeURL(object.getDataSetVersion())) + "&"
                    + this.getStockURLParam(stockName);
        }
        return "/show" + prefix + "Flow.xhtml" + this.getStockURLParam(stockName, true);
    }

    /**
     * Get flow list URL (type dependent)
     *
     * @param f flow in question
     * @return flow list URL (type dependent)
     */
    public String getWrappedList(Flow f) {
        return "/" + (f instanceof ElementaryFlow ? "elementary" : "product") + "FlowList.xhtml" + super.getStockURLParam(this.getCurrentStockName(), true);
    }

    /**
     * {@inheritDoc} <b>DO NOT USE!</b>
     *
     * @throws RuntimeException always
     */
    @Override
    public String getList(String stockName) {
        throw new RuntimeException("Not possible with wrapper class!");
    }

}
