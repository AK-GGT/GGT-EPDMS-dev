package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IProcessVO;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.process.Process;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for process links
 */
public class ProcessURLGenerator extends AbstractDataSetURLGenerator<Process> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public ProcessURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.PROCESS.name()), DatasetTypes.PROCESSES.getValue());
    }

    /**
     * Get detail view link for foreign process data sets
     *
     * @param object value object to get parameters from
     * @return URL for foreign data set detail page
     * @deprecated will be removed once data stock functionality is completed
     */
    @Deprecated
    public String getDetailForeign(IProcessVO object) {
        if (object != null) {
            return "/showProcess.xhtml?uuid=" + this.encodeURL(object.getUuidAsString()) + "&amp;sourceNode=" + this.encodeURL(object.getSourceId());
        }
        return "/showProcess.xhtml";
    }

    /**
     * Get detail view link with (optional) <code>registryUUID</code>.
     *
     * @param p            process instance
     * @param registryUUID UUID of registry
     * @return generated URL
     */
    public String getDetailWithRegistry(Process p, String registryUUID) {
        return super.getDetail(p) + (StringUtils.isNotBlank(registryUUID) ? "&registryUUID=" + registryUUID : "");
    }

    /**
     * Get URL for processes with input exchange flow
     *
     * @param f flow to link to processes with as in/output flow
     * @return generatedURL
     */
    public String getWithInputExchangeFlow(Flow f) {
        return this.getWithExchangeFlow(f, ExchangeDirectionValues.INPUT.toString());
    }

    /**
     * Get URL for processes with output exchange flow
     *
     * @param f flow to link to processes with as in/output flow
     * @return generatedURL
     */
    public String getWithOutputExchangeFlow(Flow f) {
        return this.getWithExchangeFlow(f, ExchangeDirectionValues.OUTPUT.toString());
    }

    /**
     * Get URL for processes with exchange flow
     *
     * @param f         flow to link to processes with as in/output flow
     * @param direction INPUT/OUTPUT
     * @return generatedURL
     */
    private String getWithExchangeFlow(Flow f, String direction) {
        // TODO flow version
        if (f != null && direction != null) {
            return "/processListByExchangeFlow.xhtml?fuuid=" + this.encodeURL(f.getUuidAsString()) + "&amp;direction=" + direction + "&amp;" + this.getStockURLParam(this.getCurrentStockName());
        }
        return "/processListByExchangeFlow.xhtml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<Process, ?, ?> createDaoInstance() {
        return new ProcessDao();
    }

}
