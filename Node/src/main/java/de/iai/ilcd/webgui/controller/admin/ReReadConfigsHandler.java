package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.service.CustomReportService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

@ApplicationScoped
@ManagedBean
public class ReReadConfigsHandler extends AbstractHandler {

    private static final long serialVersionUID = 377188822246066043L;
    CustomReportService customReportService;
    @ManagedProperty(value = "#{conf}")
    private ConfigurationBean configurationBean;

    public ReReadConfigsHandler() {
        super();

        WebApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        this.customReportService = ctx.getBean(CustomReportService.class);
    }

    public void reReadConfigs() {
        configurationBean.reReadConfigs();
        customReportService.reInit();
    }

    public ConfigurationBean getConfigurationBean() {
        return configurationBean;
    }

    public void setConfigurationBean(ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }

}

